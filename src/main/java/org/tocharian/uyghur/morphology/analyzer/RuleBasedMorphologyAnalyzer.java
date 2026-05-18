/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tocharian.uyghur.morphology.analyzer;

import org.tocharian.uyghur.morphology.affix.AffixDefinition;
import org.tocharian.uyghur.morphology.affix.AffixKind;
import org.tocharian.uyghur.morphology.affix.UyghurAffixInventory;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;
import org.tocharian.uyghur.morphology.model.OovBoundaryPredictor;
import org.tocharian.uyghur.morphology.model.SegmentationPath;
import org.tocharian.uyghur.morphology.model.ViterbiMorphologySegmenter;
import org.tocharian.uyghur.morphology.model.WeightedModelCompiler;
import org.tocharian.uyghur.morphology.model.WeightedMorphologyModel;
import org.tocharian.uyghur.morphology.utils.HarmonyClassifier;
import org.tocharian.uyghur.morphology.utils.HarmonyClassifier.HarmonyClass;
import org.tocharian.uyghur.morphology.utils.UyghurCharacterUtils;

import java.io.IOException;
import java.util.*;

/**
 * 基于规则的维吾尔语形态分析器
 * 整合词典查找、规则分析和统计预测
 * 使用统一词典管理器作为数据源
 */
public class RuleBasedMorphologyAnalyzer {
    
    private final UnifiedDictionaryManager unifiedDictionaryManager;
    private ViterbiMorphologySegmenter weightedSegmenter;
    private OovBoundaryPredictor oovBoundaryPredictor;
    private WeightedMorphologyModel weightedModel;

    // F3/F4a: TSV-driven suffix and clitic surface lists (replace hardcoded array)
    private String[] tsvSuffixSurfaces = new String[0];   // kind=SUFFIX, sorted longest-first
    private String[] tsvCliticSurfaces = new String[0];   // kind=CLITIC, sorted longest-first

    private boolean initialized = false;
    
    // 分析策略权重
    private static final double DICTIONARY_EXACT_CONFIDENCE = 0.95;
    private static final double DICTIONARY_PARTIAL_CONFIDENCE = 0.85;
    private static final double RULE_BASED_CONFIDENCE = 0.75;
    private static final double STATISTICAL_CONFIDENCE = 0.65;
    private static final double FALLBACK_CONFIDENCE = 0.30;
    
    public RuleBasedMorphologyAnalyzer() {
        this.unifiedDictionaryManager = UnifiedDictionaryManager.shared();
    }
    
    /**
     * 初始化分析器
     */
    public void initialize() throws IOException {
        if (!initialized || !unifiedDictionaryManager.isInitialized()) {
            // 初始化统一词典管理器
            unifiedDictionaryManager.initialize();

            // F3/F4a/F4b: 加载 TSV 词缀库，构建后缀列表和 slot 约束
            UyghurAffixInventory affixInventory = UyghurAffixInventory.loadDefault();
            tsvSuffixSurfaces = buildSortedSurfaces(affixInventory, AffixKind.SUFFIX);
            tsvCliticSurfaces = buildSortedSurfaces(affixInventory, AffixKind.CLITIC);

            weightedModel = new WeightedModelCompiler().compile(unifiedDictionaryManager);
            // F4b: pass affixInventory so Viterbi gains slot-order constraints
            weightedSegmenter = new ViterbiMorphologySegmenter(weightedModel, affixInventory);
            oovBoundaryPredictor = OovBoundaryPredictor.suffixBackoff(weightedModel);
            initialized = true;
        }
    }

    /**
     * F3 helper: collect all surface forms of a given AffixKind from the TSV,
     * deduplicate, then sort longest-first for greedy matching.
     *
     * <p>Surfaces marked with a WARNING in their note (e.g. N_PRED_2SG_RESP / -la)
     * are excluded because they are homomorphic with derivational affixes and
     * require dictionary-backed disambiguation rather than blind stripping.
     */
    private static String[] buildSortedSurfaces(UyghurAffixInventory inventory, AffixKind kind) {
        Set<String> seen = new LinkedHashSet<>();
        for (AffixDefinition def : inventory.all()) {
            if (def.getKind() != kind) continue;
            if (def.getNote().contains("WARNING")) continue;  // skip ambiguous entries
            seen.addAll(def.getSurfaces());
        }
        List<String> list = new ArrayList<>(seen);
        list.sort(Comparator.comparingInt(String::length).reversed());
        return list.toArray(new String[0]);
    }
    
    /**
     * 分析单词的形态结构
     */
    public MorphologyAnalysisResult analyze(String word) {
        return analyze(word, DictionaryView.SPLIT);
    }

    /**
     * 按指定词典视图分析单词的形态结构
     */
    public MorphologyAnalysisResult analyze(String word, DictionaryView viewType) {
        if (!initialized) {
            throw new IllegalStateException("分析器未初始化，请先调用initialize()方法");
        }
        
        if (word == null || word.trim().isEmpty()) {
            return new MorphologyAnalysisResult(word, Arrays.asList(word), 
                FALLBACK_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.FALLBACK, "空输入");
        }
        
        word = UyghurCharacterUtils.normalizeText(word);
        
        // 策略1: 词典精确匹配（优先使用自定义词典）
        MorphologyAnalysisResult exactMatch = tryDictionaryExactMatch(word, viewType);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 策略2: 词典部分匹配
        MorphologyAnalysisResult partialMatch = tryDictionaryPartialMatch(word, viewType);
        if (partialMatch != null) {
            return partialMatch;
        }
        
        // 策略3: 基于规则分析（Viterbi + 形态规则）
        MorphologyAnalysisResult ruleBasedResult = tryRuleBasedAnalysis(word);
        if (ruleBasedResult != null) {
            // F2: OOV 层结果不经过词典，需要事后查词干规范形
            return applyStemCanonical(ruleBasedResult, viewType);
        }

        // 策略4: OOV 后缀回退预测
        MorphologyAnalysisResult statisticalResult = tryStatisticalAnalysis(word);
        if (statisticalResult != null) {
            // F2: 同上
            return applyStemCanonical(statisticalResult, viewType);
        }
        
        // 策略5: 回退策略
        return new MorphologyAnalysisResult(word, Arrays.asList(word), 
            FALLBACK_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.FALLBACK, "无法分析");
    }
    
    /**
     * 策略1: 词典精确匹配（使用统一词典管理器，优先使用自定义词典）
     */
    private MorphologyAnalysisResult tryDictionaryExactMatch(String word, DictionaryView viewType) {
        // 优先查找自定义词典
        String[] customResult = unifiedDictionaryManager.lookup(word, DictionaryView.CUSTOM);
        if (customResult != null) {
            return new MorphologyAnalysisResult(word, Arrays.asList(customResult), 
                DICTIONARY_EXACT_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, 
                "统一词典-自定义视图精确匹配");
        }
        
        // 根据分析器配置查找对应词典视图，避免 original/split 分析器行为混同
        String[] viewResult = unifiedDictionaryManager.lookup(word, viewType);
        if (viewResult != null) {
            return new MorphologyAnalysisResult(word, Arrays.asList(viewResult), 
                DICTIONARY_EXACT_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, 
                "统一词典-" + viewType + "视图精确匹配");
        }
        
        return null;
    }
    
    /**
     * 策略2: 词典部分匹配（使用统一词典管理器）
     */
    private MorphologyAnalysisResult tryDictionaryPartialMatch(String word, DictionaryView viewType) {
        List<String> longestMatches = unifiedDictionaryManager.findLongestMatches(word, viewType);
        
        if (!longestMatches.isEmpty()) {
            String longestMatch = longestMatches.get(0);
            
            if (longestMatch.length() >= word.length() * 0.6) { // 至少匹配60%
                String remainder = word.substring(longestMatch.length());
                
                List<String> segments = new ArrayList<>();

                // If the longest prefix is itself a dictionary entry, expand it first.
                // This keeps open suffix chains such as ئىشلىگەننىڭ as ئىشلى/ئىشلە + گەن + نىڭ
                // instead of flattening the known prefix into a single token.
                String[] prefixSegments = unifiedDictionaryManager.lookup(longestMatch, DictionaryView.CUSTOM);
                if (prefixSegments == null) {
                    prefixSegments = unifiedDictionaryManager.lookup(longestMatch, viewType);
                }
                if (prefixSegments != null) {
                    segments.addAll(Arrays.asList(prefixSegments));
                } else {
                    segments.add(longestMatch);
                }
                
                if (!remainder.isEmpty()) {
                    // 使用规则分析剩余部分
                    List<String> remainderSegments = analyzeRemainderByRules(remainder);
                    segments.addAll(remainderSegments);
                }
                
                return new MorphologyAnalysisResult(word, segments, 
                    DICTIONARY_PARTIAL_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_PARTIAL, 
                    "统一词典部分匹配: " + longestMatch);
            }
        }
        
        return null;
    }
    
    /**
     * 策略3: 基于规则分析
     */
    private MorphologyAnalysisResult tryRuleBasedAnalysis(String word) {
        SegmentationPath path = weightedSegmenter.segment(word);
        if (path.hasSplit()) {
            return new MorphologyAnalysisResult(word, path.getSegments(),
                path.toConfidence(), MorphologyAnalysisResult.AnalysisMethod.WEIGHTED_MODEL,
                path.getNotes());
        }

        List<String> segments = analyzeByMorphologicalRules(word);
        
        if (segments.size() > 1) {
            double confidence = calculateRuleBasedConfidence(segments);
            return new MorphologyAnalysisResult(word, segments, 
                confidence, MorphologyAnalysisResult.AnalysisMethod.RULE_BASED, 
                "基于形态学规则分析");
        }
        
        return null;
    }
    
    /**
     * 策略4: 统计模型预测
     */
    private MorphologyAnalysisResult tryStatisticalAnalysis(String word) {
        SegmentationPath path = oovBoundaryPredictor.predict(word);
        
        if (path.hasSplit()) {
            return new MorphologyAnalysisResult(word, path.getSegments(), 
                path.toConfidence(), MorphologyAnalysisResult.AnalysisMethod.OOV_BOUNDARY, 
                path.getNotes());
        }
        
        return null;
    }
    
    /**
     * 基于形态学规则分析词汇
     *
     * <p>F4c: 先剥离 TSV SUFFIX 词尾，再剥离 CLITIC 附接词（附接词出现在所有词尾之后）。
     */
    private List<String> analyzeByMorphologicalRules(String word) {
        return analyzeByCommonPatterns(word);
    }

    /**
     * F3: TSV 驱动的后缀剥离，替换原来的硬编码列表。
     *
     * <p>剥离顺序：
     * 1. 从右到左贪心剥离 CLITIC（附接词，如 مۇ، چى）—— 附接词位于最外层
     * 2. 从右到左贪心剥离 SUFFIX（词尾），带元音和谐律校验
     *
     * <p>不含派生词缀（构词词缀如 لىق、سىز 已从 TSV 中排除）。
     * 不含 WARNING 条目（如 -la，与派生词缀同形）。
     */
    private List<String> analyzeByCommonPatterns(String word) {
        List<String> suffixSegments = new ArrayList<>();
        List<String> cliticSegments = new ArrayList<>();
        String remaining = word;

        // Step 1: 先从最右侧剥离 CLITIC（附接词不参与元音和谐，直接匹配）
        if (tsvCliticSurfaces.length > 0) {
            boolean found = true;
            while (found && remaining.length() > 2) {
                found = false;
                for (String clitic : tsvCliticSurfaces) {
                    if (clitic.length() < remaining.length() && remaining.endsWith(clitic)) {
                        cliticSegments.add(0, clitic);
                        remaining = remaining.substring(0, remaining.length() - clitic.length());
                        found = true;
                        break;
                    }
                }
            }
        }

        // Step 2: 剥离 SUFFIX 词尾，带元音和谐律校验
        String[] suffixList = tsvSuffixSurfaces.length > 0 ? tsvSuffixSurfaces : LEGACY_SUFFIX_FALLBACK;
        boolean foundSuffix = true;
        while (foundSuffix && remaining.length() > 2) {
            foundSuffix = false;
            for (String suffix : suffixList) {
                if (suffix.length() < remaining.length() && remaining.endsWith(suffix)) {
                    String potentialRoot = remaining.substring(0, remaining.length() - suffix.length());
                    if (checkVowelHarmony(potentialRoot, suffix)) {
                        suffixSegments.add(0, suffix);
                        remaining = potentialRoot;
                        foundSuffix = true;
                        break;
                    }
                }
            }
        }

        // 组装：词干 + 词尾序列 + 附接词序列
        List<String> segments = new ArrayList<>();
        if (!remaining.isEmpty()) {
            segments.add(remaining);
        }
        segments.addAll(suffixSegments);
        segments.addAll(cliticSegments);
        return segments;
    }

    /**
     * 应急回退：TSV 未加载时使用的纯词尾列表（已移除派生词缀 لىق、سىز、چان、گەر）。
     */
    private static final String[] LEGACY_SUFFIX_FALLBACK = {
        "لىرى", "لەر", "لار", "نىڭ", "دىن", "دەن", "تىن", "تەن",
        "غا", "گە", "قا", "كە", "نى", "دا", "دە", "تا", "تە",
        "ىدە", "ىنى", "ىغا", "ىش", "ەر", "ان", "ەن", "ىم", "سى", "ى"
    };

    /**
     * F2 + Phase α: 将 OOV 层（L3/L4）分析结果里的书写词干和书写后缀替换为规范形。
     *
     * <p>仅在 viewType == ORIGINAL 时生效。两步还原：
     * <ol>
     *   <li>词干：查 stemCanonicalIndex (F1) — 例 ئائىلى → ئائىلە</li>
     *   <li>后缀：查 suffixCanonicalIndex (Phase α)，按词干和谐类区分
     *       — 例 (لىر, FRONT) → لەر, (لىر, BACK) → لار</li>
     * </ol>
     * 任何一步若无记录则原样保留。
     */
    private MorphologyAnalysisResult applyStemCanonical(MorphologyAnalysisResult result, DictionaryView viewType) {
        if (result == null || viewType != DictionaryView.ORIGINAL) {
            return result;
        }
        List<String> morphemes = result.getMorphemes();
        if (morphemes == null || morphemes.isEmpty()) {
            return result;
        }

        List<String> restored = new ArrayList<>(morphemes);
        StringBuilder noteSuffix = new StringBuilder();

        // 第一步：词干还原（F2）
        String writtenStem = morphemes.get(0);
        String canonicalStem = unifiedDictionaryManager.lookupCanonicalStem(writtenStem);
        if (canonicalStem != null && !canonicalStem.equals(writtenStem)) {
            restored.set(0, canonicalStem);
            noteSuffix.append(" [stem:").append(writtenStem).append("→").append(canonicalStem).append("]");
        }

        // 第二步：后缀还原（Phase α）—— 用 canonical 词干判和谐类（更接近真实形态）
        String stemForHarmony = (canonicalStem != null) ? canonicalStem : writtenStem;
        HarmonyClass harmony = HarmonyClassifier.classifyWithFrontDefault(stemForHarmony);

        for (int i = 1; i < restored.size(); i++) {
            String writtenSuffix = restored.get(i);
            String canonicalSuffix = unifiedDictionaryManager.lookupCanonicalSuffix(writtenSuffix, harmony);
            if (canonicalSuffix != null && !canonicalSuffix.equals(writtenSuffix)) {
                restored.set(i, canonicalSuffix);
                noteSuffix.append(" [suf:").append(writtenSuffix).append("→").append(canonicalSuffix).append("]");
            }
        }

        if (noteSuffix.length() == 0) {
            return result;  // 没有任何还原发生
        }

        return new MorphologyAnalysisResult(
            result.getOriginalWord(),
            restored,
            result.getConfidence(),
            result.getMethod(),
            result.getNotes() + noteSuffix.toString()
        );
    }
    
    /**
     * 分析剩余部分（用于部分匹配）
     */
    private List<String> analyzeRemainderByRules(String remainder) {
        if (remainder.length() <= 2) {
            return Arrays.asList(remainder);
        }

        // In dictionary-partial analysis the remainder may already be one suffix
        // after a known prefix, e.g. ئىشلىگەن + نىڭ. Keep that boundary intact.
        if (containsSurface(tsvSuffixSurfaces, remainder) || containsSurface(tsvCliticSurfaces, remainder)) {
            return Arrays.asList(remainder);
        }
        
        return analyzeByMorphologicalRules(remainder);
    }

    private static boolean containsSurface(String[] surfaces, String target) {
        for (String surface : surfaces) {
            if (surface.equals(target)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查元音和谐律
     */
    private boolean checkVowelHarmony(String root, String suffix) {
        char rootLastVowel = UyghurCharacterUtils.getLastVowel(root);
        char suffixFirstVowel = UyghurCharacterUtils.getFirstVowel(suffix);
        
        return UyghurCharacterUtils.isVowelHarmony(rootLastVowel, suffixFirstVowel);
    }
    
    /**
     * 计算基于规则分析的置信度
     */
    private double calculateRuleBasedConfidence(List<String> segments) {
        double confidence = RULE_BASED_CONFIDENCE;
        
        // 根据段数调整置信度
        if (segments.size() == 2) {
            confidence += 0.05; // 简单分词更可信
        } else if (segments.size() > 4) {
            confidence -= 0.10; // 过度分词降低可信度
        }
        
        // 检查后缀长度合理性
        for (int i = 1; i < segments.size(); i++) {
            String segment = segments.get(i);
            if (segment.length() >= 2 && segment.length() <= 4) {
                confidence += 0.02; // 合理长度的后缀
            } else if (segment.length() > 6) {
                confidence -= 0.05; // 过长的后缀降低可信度
            }
        }
        
        return Math.max(0.1, Math.min(0.9, confidence));
    }
    
    /**
     * 计算统计分析的置信度
     */
    private double calculateStatisticalConfidence(List<String> segments) {
        double confidence = STATISTICAL_CONFIDENCE;
        
        // 基于段的合理性调整
        for (String segment : segments) {
            if (segment.length() >= 2 && segment.length() <= 5) {
                confidence += 0.02; // 合理长度的段
            } else if (segment.length() == 1) {
                confidence -= 0.05; // 单字符段降低可信度
            }
        }
        
        return Math.max(0.1, Math.min(0.8, confidence));
    }
    
    /**
     * 获取分析器统计信息
     */
    public Map<String, Object> getAnalyzerStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("initialized", initialized);
        stats.put("confidence_thresholds", Map.of(
            "dictionary_exact", DICTIONARY_EXACT_CONFIDENCE,
            "dictionary_partial", DICTIONARY_PARTIAL_CONFIDENCE,
            "rule_based", RULE_BASED_CONFIDENCE,
            "statistical", STATISTICAL_CONFIDENCE,
            "fallback", FALLBACK_CONFIDENCE
        ));
        
        if (initialized) {
            stats.putAll(unifiedDictionaryManager.getStatistics());
            stats.put("weighted_model", Map.of(
                "roots", weightedModel.rootCount(),
                "suffixes", weightedModel.suffixCount(),
                "transitions", weightedModel.transitionCount()
            ));
        }
        
        return stats;
    }
    
    /**
     * 检查分析器是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 获取统一词典管理器实例
     */
    public UnifiedDictionaryManager getUnifiedDictionaryManager() {
        return unifiedDictionaryManager;
    }
} 