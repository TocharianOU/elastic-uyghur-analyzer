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

package org.uyghur.morphology.analyzer;

import org.uyghur.morphology.dictionary.UnifiedDictionaryManager;
import org.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;
import org.uyghur.morphology.utils.UyghurCharacterUtils;

import java.io.IOException;
import java.util.*;

/**
 * 基于规则的维吾尔语形态分析器
 * 整合词典查找、规则分析和统计预测
 * 使用统一词典管理器作为数据源
 */
public class RuleBasedMorphologyAnalyzer {
    
    private final UnifiedDictionaryManager unifiedDictionaryManager;
    private boolean initialized = false;
    
    // 分析策略权重
    private static final double DICTIONARY_EXACT_CONFIDENCE = 0.95;
    private static final double DICTIONARY_PARTIAL_CONFIDENCE = 0.85;
    private static final double RULE_BASED_CONFIDENCE = 0.75;
    private static final double STATISTICAL_CONFIDENCE = 0.65;
    private static final double FALLBACK_CONFIDENCE = 0.30;
    
    public RuleBasedMorphologyAnalyzer() {
        this.unifiedDictionaryManager = new UnifiedDictionaryManager();
    }
    
    /**
     * 初始化分析器
     */
    public void initialize() throws IOException {
        if (!initialized) {
            System.out.println("正在初始化维吾尔语形态分析器...");
            
            // 初始化统一词典管理器
            unifiedDictionaryManager.initialize();
            System.out.println("统一词典加载完成: " + unifiedDictionaryManager.getStatistics());
            
            initialized = true;
            System.out.println("维吾尔语形态分析器初始化完成");
        }
    }
    
    /**
     * 分析单词的形态结构
     */
    public MorphologyAnalysisResult analyze(String word) {
        if (!initialized) {
            throw new IllegalStateException("分析器未初始化，请先调用initialize()方法");
        }
        
        if (word == null || word.trim().isEmpty()) {
            return new MorphologyAnalysisResult(word, Arrays.asList(word), 
                FALLBACK_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.FALLBACK, "空输入");
        }
        
        word = UyghurCharacterUtils.normalizeText(word);
        
        // 策略1: 词典精确匹配（优先使用自定义词典）
        MorphologyAnalysisResult exactMatch = tryDictionaryExactMatch(word);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // 策略2: 词典部分匹配
        MorphologyAnalysisResult partialMatch = tryDictionaryPartialMatch(word);
        if (partialMatch != null) {
            return partialMatch;
        }
        
        // 策略3: 基于规则分析
        MorphologyAnalysisResult ruleBasedResult = tryRuleBasedAnalysis(word);
        if (ruleBasedResult != null) {
            return ruleBasedResult;
        }
        
        // 策略4: 统计模型预测
        MorphologyAnalysisResult statisticalResult = tryStatisticalAnalysis(word);
        if (statisticalResult != null) {
            return statisticalResult;
        }
        
        // 策略5: 回退策略
        return new MorphologyAnalysisResult(word, Arrays.asList(word), 
            FALLBACK_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.FALLBACK, "无法分析");
    }
    
    /**
     * 策略1: 词典精确匹配（使用统一词典管理器，优先使用自定义词典）
     */
    private MorphologyAnalysisResult tryDictionaryExactMatch(String word) {
        // 优先查找自定义词典
        String[] customResult = unifiedDictionaryManager.lookup(word, DictionaryView.CUSTOM);
        if (customResult != null) {
            return new MorphologyAnalysisResult(word, Arrays.asList(customResult), 
                DICTIONARY_EXACT_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, 
                "统一词典-自定义视图精确匹配");
        }
        
        // 然后查找原始词典视图
        String[] originalResult = unifiedDictionaryManager.lookup(word, DictionaryView.ORIGINAL);
        if (originalResult != null) {
            return new MorphologyAnalysisResult(word, Arrays.asList(originalResult), 
                DICTIONARY_EXACT_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, 
                "统一词典-原始视图精确匹配");
        }
        
        // 最后查找分割词典视图
        String[] splitResult = unifiedDictionaryManager.lookup(word, DictionaryView.SPLIT);
        if (splitResult != null) {
            return new MorphologyAnalysisResult(word, Arrays.asList(splitResult), 
                DICTIONARY_EXACT_CONFIDENCE, MorphologyAnalysisResult.AnalysisMethod.DICTIONARY_EXACT, 
                "统一词典-分割视图精确匹配");
        }
        
        return null;
    }
    
    /**
     * 策略2: 词典部分匹配（使用统一词典管理器）
     */
    private MorphologyAnalysisResult tryDictionaryPartialMatch(String word) {
        // 首先尝试分割视图的最长匹配
        List<String> longestMatches = unifiedDictionaryManager.findLongestMatches(word, DictionaryView.SPLIT);
        
        // 如果分割视图没有好的匹配，尝试原始视图
        if (longestMatches.isEmpty()) {
            longestMatches = unifiedDictionaryManager.findLongestMatches(word, DictionaryView.ORIGINAL);
        }
        
        if (!longestMatches.isEmpty()) {
            String longestMatch = longestMatches.get(0);
            
            if (longestMatch.length() >= word.length() * 0.6) { // 至少匹配60%
                String remainder = word.substring(longestMatch.length());
                
                // 尝试分析剩余部分
                List<String> segments = new ArrayList<>();
                segments.add(longestMatch);
                
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
        // 简化的统计分析：基于常见后缀模式
        List<String> segments = analyzeByCommonPatterns(word);
        
        if (segments.size() > 1) {
            double confidence = calculateStatisticalConfidence(segments);
            return new MorphologyAnalysisResult(word, segments, 
                confidence, MorphologyAnalysisResult.AnalysisMethod.STATISTICAL, 
                "基于统计模式预测");
        }
        
        return null;
    }
    
    /**
     * 基于形态学规则分析词汇
     */
    private List<String> analyzeByMorphologicalRules(String word) {
        return analyzeByCommonPatterns(word);
    }
    
    /**
     * 基于常见模式分析词汇
     */
    private List<String> analyzeByCommonPatterns(String word) {
        List<String> segments = new ArrayList<>();
        String remaining = word;
        
        // 常见维吾尔语后缀（按长度递减）
        String[] commonSuffixes = {
            "لىرى", "لار", "نىڭ", "دىن", "غا", "نى", "دا", "تا", "كە", 
            "لىق", "سىز", "چان", "گەر", "لەر", "ىدە", "ىنى", "ىغا",
            "ىش", "ەر", "ان", "ەن", "ىم", "سى", "ى", "لا", "نا", 
            "دا", "تا", "گە", "كە", "نى", "نا", "ما", "با"
        };
        
        boolean foundSuffix = true;
        while (foundSuffix && remaining.length() > 2) {
            foundSuffix = false;
            
            for (String suffix : commonSuffixes) {
                if (remaining.endsWith(suffix) && remaining.length() > suffix.length()) {
                    String potentialRoot = remaining.substring(0, remaining.length() - suffix.length());
                    
                    // 检查元音和谐律
                    if (checkVowelHarmony(potentialRoot, suffix)) {
                        segments.add(0, suffix); // 添加到开头
                        remaining = potentialRoot;
                        foundSuffix = true;
                        break;
                    }
                }
            }
        }
        
        if (!remaining.isEmpty()) {
            segments.add(0, remaining); // 词根添加到最前面
        }
        
        return segments;
    }
    
    /**
     * 分析剩余部分（用于部分匹配）
     */
    private List<String> analyzeRemainderByRules(String remainder) {
        if (remainder.length() <= 2) {
            return Arrays.asList(remainder);
        }
        
        return analyzeByMorphologicalRules(remainder);
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