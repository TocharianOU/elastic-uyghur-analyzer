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

package org.tocharian.uyghur.morphology.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.tocharian.uyghur.morphology.utils.HarmonyClassifier;
import org.tocharian.uyghur.morphology.utils.HarmonyClassifier.HarmonyClass;

/**
 * 统一词典管理器
 * 以THU格式字典为唯一数据源，生成多个视图：
 * - originalView: 元音修复形式 (ئائىلە + دىكى)
 * - splitView: 现代形式保持 (ئائىلى + دىكى)
 * - customView: 用户自定义词汇
 * 
 * THU格式示例: ئائىلى.(ئائىلە) دىكى
 * 解析为: 现代形式=ئائىلى, 原始形式=ئائىلە, 后缀=دىكى
 */
public class UnifiedDictionaryManager {
    
    private static final String THU_DICTIONARY_PATH = "/dictionaries/thuuy_morph_raw.txt";
    private static final String CUSTOM_DICTIONARY_PATH = "/dictionaries/custom_dictionary.txt";
    private static final UnifiedDictionaryManager SHARED_INSTANCE = new UnifiedDictionaryManager();
    
    // THU格式解析正则表达式
    private static final Pattern THU_PATTERN = Pattern.compile("^([^.()]+)\\.(\\([^)]+\\))\\s+(.+)$");  // 完整格式：词根.(原始) 后缀
    // B1 fix: use \S+ so multi-token entries like "ئارزۇ سى دىكى" don't bleed into the stem group
    private static final Pattern THU_SIMPLE_PATTERN = Pattern.compile("^(\\S+)\\s+(.+)$");              // 简单格式：词根 后缀
    private static final Pattern THU_DOT_PATTERN = Pattern.compile("^([^.()]+)\\.\\s+(.+)$");           // 点号格式：词根. 后缀

    // F1+ fix: extract the parenthetical canonical form from a single suffix token.
    // Example: "لىر(لەر)" → group(1)="لىر", group(2)="لەر"
    //          "دىكى"      → group(1)="دىكى", group(2)=null
    private static final Pattern SUFFIX_PAREN_PATTERN = Pattern.compile("^([^()]+)(?:\\(([^)]+)\\))?$");

    // 三个视图：原始、分割、自定义
    private Map<String, String[]> originalView;    // 元音修复形式
    private Map<String, String[]> splitView;       // 现代形式
    private Map<String, String[]> customView;      // 用户自定义

    // F1: 词干级规范形索引：书写词干（元音弱化后）→ 规范词干（弱化前还原）
    // 来源：THUUyMorph 括号条目，如 ئائىلى.(ئائىلە) → ئائىلى→ئائىلە
    private Map<String, String> stemCanonicalIndex;

    // Phase α: 后缀级规范形索引（按和谐类区分）
    // 结构：written_suffix → (harmony_class → canonical_suffix)
    // 例：لىر → {BACK: لار, FRONT: لەر}（取决于词干和谐类）
    // 来源：THUUyMorph 后缀括号，如 لىر(لار), لىر(لەر)
    //
    // Built in two stages:
    //   1. During parsing: accumulate counts in suffixCanonicalCounts
    //   2. After parsing: pick majority candidate per (written, harmony) with min threshold
    private Map<String, Map<HarmonyClass, String>> suffixCanonicalIndex;
    private Map<String, Map<HarmonyClass, Map<String, Integer>>> suffixCanonicalCounts;

    /** Minimum occurrence count required to accept a suffix-canonical mapping.
     *  Filters out rare/noisy entries like قاندۇر. ى(ۇ) which would otherwise
     *  pollute the index for a very common surface (ى). */
    private static final int SUFFIX_MIN_COUNT = 2;

    // 原始THU数据存储
    private Map<String, String> rawThuData;
    
    private boolean initialized = false;
    
    public UnifiedDictionaryManager() {
        this.originalView = new HashMap<>();
        this.splitView = new HashMap<>();
        this.customView = new HashMap<>();
        this.stemCanonicalIndex = new HashMap<>();
        this.suffixCanonicalIndex = new HashMap<>();
        this.suffixCanonicalCounts = new HashMap<>();
        this.rawThuData = new HashMap<>();
    }

    public static UnifiedDictionaryManager shared() {
        return SHARED_INSTANCE;
    }
    
    /**
     * 初始化统一词典系统
     */
    public synchronized void initialize() throws IOException {
        if (initialized) {
            return;
        }

        // 1. 加载自定义词典（最高优先级）
        loadCustomDictionary();
        
        // 2. 加载并解析THU词典数据
        loadAndParseThuDictionary();
        
        initialized = true;
    }
    
    /**
     * 加载自定义词典
     */
    private void loadCustomDictionary() throws IOException {
        InputStream inputStream = getResourceAsStream(CUSTOM_DICTIONARY_PATH);
        if (inputStream == null) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // 使用THU格式解析自定义词典
                parseCustomEntry(line);
            }
        }
    }
    
    /**
     * 解析自定义词典条目（使用THU格式）
     */
    private void parseCustomEntry(String entry) {
        // 尝试解析点号格式：词根. 后缀
        Matcher matcher = THU_DOT_PATTERN.matcher(entry);
        if (matcher.matches()) {
            String root = matcher.group(1).trim();
            String suffixPart = matcher.group(2).trim();
            
            // 处理后缀部分
            String[] suffixParts = suffixPart.split("\\s+");
            
            // 生成统一Key
            String unifiedKey = generateUnifiedKey(entry);
            
            // 构建分割结果
            String[] segments = new String[suffixParts.length + 1];
            segments[0] = root;
            
            for (int i = 0; i < suffixParts.length; i++) {
                segments[i + 1] = suffixParts[i].trim();
            }
            
            customView.put(unifiedKey, segments);
            return;
        }
        
        // 尝试解析简单格式：词根 后缀
        matcher = THU_SIMPLE_PATTERN.matcher(entry);
        if (matcher.matches()) {
            String root = matcher.group(1).trim();
            String suffixPart = matcher.group(2).trim();
            
            // 处理后缀部分
            String[] suffixParts = suffixPart.split("\\s+");
            
            // 生成统一Key
            String unifiedKey = generateUnifiedKey(entry);
            
            // 构建分割结果
            String[] segments = new String[suffixParts.length + 1];
            segments[0] = root;
            
            for (int i = 0; i < suffixParts.length; i++) {
                segments[i + 1] = suffixParts[i].trim();
            }
            
            customView.put(unifiedKey, segments);
            return;
        }
        
        // 如果无法解析，作为单一词汇处理
        String unifiedKey = generateUnifiedKey(entry);
        customView.put(unifiedKey, new String[]{unifiedKey});
    }
    
    /**
     * 加载并解析THU词典数据
     */
    private void loadAndParseThuDictionary() throws IOException {
        InputStream inputStream = getResourceAsStream(THU_DICTIONARY_PATH);
        if (inputStream == null) {
            throw new IOException("Unable to find THUUyMorph dictionary resource: " + THU_DICTIONARY_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                rawThuData.put(line, line);
                parseThuEntry(line);
            }
        }

        // Phase α: finalize the suffix index by majority voting + min-count threshold
        finalizeSuffixCanonicalIndex();
    }

    /**
     * Phase α helper: 把 (written_suffix, harmony, canonical_suffix) 计数转成最终索引。
     * 对每个 (written, harmony) 桶：选出现次数最多的 canonical，并要求 count ≥ SUFFIX_MIN_COUNT。
     * 这样过滤掉孤立噪声条目（如 قاندۇر. ى(ۇ) 这种单例）。
     */
    private void finalizeSuffixCanonicalIndex() {
        for (Map.Entry<String, Map<HarmonyClass, Map<String, Integer>>> e : suffixCanonicalCounts.entrySet()) {
            String written = e.getKey();
            for (Map.Entry<HarmonyClass, Map<String, Integer>> he : e.getValue().entrySet()) {
                HarmonyClass harmony = he.getKey();
                String bestCanonical = null;
                int bestCount = 0;
                for (Map.Entry<String, Integer> ce : he.getValue().entrySet()) {
                    if (ce.getValue() > bestCount) {
                        bestCount = ce.getValue();
                        bestCanonical = ce.getKey();
                    }
                }
                if (bestCanonical != null && bestCount >= SUFFIX_MIN_COUNT) {
                    suffixCanonicalIndex
                        .computeIfAbsent(written, k -> new EnumMap<>(HarmonyClass.class))
                        .put(harmony, bestCanonical);
                }
            }
        }
    }

    /** Phase α: record an observation of (written, harmony) → canonical. Index finalised later. */
    private void recordSuffixObservation(String written, HarmonyClass harmony, String canonical) {
        if (written == null || canonical == null || written.equals(canonical)) {
            return;
        }
        suffixCanonicalCounts
            .computeIfAbsent(written, k -> new EnumMap<>(HarmonyClass.class))
            .computeIfAbsent(harmony, k -> new HashMap<>())
            .merge(canonical, 1, Integer::sum);
    }
    
    /**
     * 解析THU格式条目
     * 格式1: ئائىلى.(ئائىلە) دىكى -> 现代形式.(原始形式) 后缀
     * 格式2: ئورۇنلاشتۇر. ۇش ى نى -> 词根. 后缀
     * 格式3: ئائىلى دىكى -> 简单形式 后缀
     * 
     * 关键设计：统一Key生成，差异化分割
     * - 所有视图使用相同的Key（去括号去标点连接）
     * - Split视图使用现代词根分割
     * - Original视图使用历史词根分割
     */
    private void parseThuEntry(String entry) {
        // 尝试解析完整格式：现代形式.(原始形式) 后缀
        Matcher matcher = THU_PATTERN.matcher(entry);
        if (matcher.matches()) {
            String modernForm = matcher.group(1).trim();
            String originalFormWithParens = matcher.group(2).trim();
            String suffixPart = matcher.group(3).trim();
            
            // 提取原始形式（去掉括号）
            String originalForm = originalFormWithParens.substring(1, originalFormWithParens.length() - 1);
            
            // 处理后缀部分：清理括号变体，然后分割
            String[] suffixParts = suffixPart.split("\\s+");
            
            // 生成统一Key：去括号去标点连接
            String unifiedKey = generateUnifiedKey(entry);
            
            // 构建分割结果数组：词根 + 各个后缀
            String[] modernSegments = new String[suffixParts.length + 1];
            String[] originalSegments = new String[suffixParts.length + 1];
            
            modernSegments[0] = modernForm;   // Split视图：现代词根
            originalSegments[0] = originalForm; // Original视图：历史词根

            // Phase α: 用 canonical 词干（更接近"未弱化"的真实形态）判定和谐类
            HarmonyClass stemHarmony = HarmonyClassifier.classifyWithFrontDefault(originalForm);

            // F1+ fix: 正确解析每个后缀的"书写形(规范形)"对，分别填两个视图
            for (int i = 0; i < suffixParts.length; i++) {
                String writtenSuffix;
                String canonicalSuffix;

                Matcher sm = SUFFIX_PAREN_PATTERN.matcher(suffixParts[i].trim());
                if (sm.matches()) {
                    writtenSuffix = sm.group(1).trim();
                    String paren = sm.group(2);
                    canonicalSuffix = (paren == null || paren.isBlank()) ? writtenSuffix : paren.trim();
                } else {
                    // Pattern 不匹配（不应发生），保守地用原 token
                    writtenSuffix = suffixParts[i].replaceAll("\\([^)]*\\)", "").trim();
                    canonicalSuffix = writtenSuffix;
                }

                modernSegments[i + 1]   = writtenSuffix;     // Split：弱化后写形
                originalSegments[i + 1] = canonicalSuffix;   // Original：未弱化规范形

                // 仅当 written ≠ canonical 时才填后缀索引（identity 不需要还原）
                recordSuffixObservation(writtenSuffix, stemHarmony, canonicalSuffix);
            }

            // 两个视图使用相同Key，但分割结果不同
            splitView.put(unifiedKey, modernSegments);
            originalView.put(unifiedKey, originalSegments);

            // F1: 词干级还原映射（书写词干 → 规范词干）
            stemCanonicalIndex.putIfAbsent(modernForm, originalForm);

            return;
        }
        
        // 尝试解析点号格式：词根. 后缀 (新增)
        matcher = THU_DOT_PATTERN.matcher(entry);
        if (matcher.matches()) {
            String root = matcher.group(1).trim();
            String suffixPart = matcher.group(2).trim();

            String[] suffixParts = suffixPart.split("\\s+");
            String unifiedKey = generateUnifiedKey(entry);

            // Phase α: 即使词干无括号，后缀仍可能有括号注释（如 تارماق. لىر(لار) ى）
            // 用 root 自身判和谐类（无 canonical 信息可用）
            HarmonyClass stemHarmony = HarmonyClassifier.classifyWithFrontDefault(root);

            String[] modernSegments = new String[suffixParts.length + 1];
            String[] originalSegments = new String[suffixParts.length + 1];
            modernSegments[0] = root;
            originalSegments[0] = root;  // 无 canonical 信息，两视图同

            for (int i = 0; i < suffixParts.length; i++) {
                String writtenSuffix;
                String canonicalSuffix;
                Matcher sm = SUFFIX_PAREN_PATTERN.matcher(suffixParts[i].trim());
                if (sm.matches()) {
                    writtenSuffix = sm.group(1).trim();
                    String paren = sm.group(2);
                    canonicalSuffix = (paren == null || paren.isBlank()) ? writtenSuffix : paren.trim();
                } else {
                    writtenSuffix = suffixParts[i].replaceAll("\\([^)]*\\)", "").trim();
                    canonicalSuffix = writtenSuffix;
                }
                modernSegments[i + 1]   = writtenSuffix;
                originalSegments[i + 1] = canonicalSuffix;

                recordSuffixObservation(writtenSuffix, stemHarmony, canonicalSuffix);
            }

            splitView.put(unifiedKey, modernSegments);
            originalView.put(unifiedKey, originalSegments);

            return;
        }
        
        // 尝试解析简单格式：词根 后缀
        matcher = THU_SIMPLE_PATTERN.matcher(entry);
        if (matcher.matches()) {
            String root = matcher.group(1).trim();
            String suffixPart = matcher.group(2).trim();

            String[] suffixParts = suffixPart.split("\\s+");
            String unifiedKey = generateUnifiedKey(entry);

            // Phase α: 后缀可能带括号注释，同 DOT 分支
            HarmonyClass stemHarmony = HarmonyClassifier.classifyWithFrontDefault(root);

            String[] modernSegments = new String[suffixParts.length + 1];
            String[] originalSegments = new String[suffixParts.length + 1];
            modernSegments[0] = root;
            originalSegments[0] = root;

            for (int i = 0; i < suffixParts.length; i++) {
                String writtenSuffix;
                String canonicalSuffix;
                Matcher sm = SUFFIX_PAREN_PATTERN.matcher(suffixParts[i].trim());
                if (sm.matches()) {
                    writtenSuffix = sm.group(1).trim();
                    String paren = sm.group(2);
                    canonicalSuffix = (paren == null || paren.isBlank()) ? writtenSuffix : paren.trim();
                } else {
                    writtenSuffix = suffixParts[i].replaceAll("\\([^)]*\\)", "").trim();
                    canonicalSuffix = writtenSuffix;
                }
                modernSegments[i + 1]   = writtenSuffix;
                originalSegments[i + 1] = canonicalSuffix;

                recordSuffixObservation(writtenSuffix, stemHarmony, canonicalSuffix);
            }

            splitView.put(unifiedKey, modernSegments);
            originalView.put(unifiedKey, originalSegments);

            return;
        }
        
        // 如果无法解析，作为单一词汇处理
        String unifiedKey = generateUnifiedKey(entry);
        splitView.put(unifiedKey, new String[]{unifiedKey});
        originalView.put(unifiedKey, new String[]{unifiedKey});
    }
    
    /**
     * 生成统一Key：去除括号、点号等标点符号，连接所有部分
     * 例如：ئائىلى.(ئائىلە) دىكى -> ئائىلىدىكى
     */
    private String generateUnifiedKey(String entry) {
        // 第一步：去除括号及其内容
        String step1 = entry.replaceAll("\\([^)]*\\)", "");
        
        // 第二步：去除点号
        String step2 = step1.replaceAll("\\.", "");
        
        // 第三步：分割空格并连接
        String[] parts = step2.trim().split("\\s+");
        return String.join("", parts);
    }
    
    private InputStream getResourceAsStream(String path) {
        return getClass().getResourceAsStream(path);
    }
    
    // === 查询接口 ===
    
    /**
     * 查询指定词典视图。自定义词典优先级由调用方显式控制，避免重复查询。
     */
    public String[] lookup(String word, DictionaryView viewType) {
        if (!initialized) {
            throw new IllegalStateException("词典系统未初始化");
        }

        switch (viewType) {
            case ORIGINAL:
                return originalView.get(word);
            case SPLIT:
                return splitView.get(word);
            case CUSTOM:
                return customView.get(word);
            default:
                return null;
        }
    }
    
    /**
     * 检查词汇是否存在于任何视图中
     */
    public boolean containsWord(String word) {
        return customView.containsKey(word) ||
               originalView.containsKey(word) ||
               splitView.containsKey(word);
    }

    /**
     * F1: 查询词干的规范形（元音弱化还原）。
     * 仅在 original 视图下由 OOV 层调用，用于将书写词干还原为弱化前的规范形。
     * 例如：ئائىلى → ئائىلە，ئاتى → ئاتا
     *
     * @return 规范词干，若无记录则返回 null（调用方退化为 split 输出）
     */
    public String lookupCanonicalStem(String writtenStem) {
        return stemCanonicalIndex.get(writtenStem);
    }

    /**
     * Phase α: 查询后缀的规范形（弱化前还原），按词干和谐类区分。
     *
     * <p>例：(لىر, BACK)  → لار（"tarmaq + lar + i" 类条目还原）
     *      (لىر, FRONT) → لەر（"aile + ler + i" 类条目还原）
     *
     * <p>若指定和谐类下无记录但其他和谐类有 → fallback 到另一类（次优）。
     *
     * @return 规范后缀，若完全无记录返回 null（调用方退化为 written 输出）
     */
    public String lookupCanonicalSuffix(String writtenSuffix, HarmonyClass harmony) {
        Map<HarmonyClass, String> byHarmony = suffixCanonicalIndex.get(writtenSuffix);
        if (byHarmony == null || byHarmony.isEmpty()) {
            return null;
        }
        String exact = byHarmony.get(harmony);
        if (exact != null) {
            return exact;
        }
        // Fallback: 若本和谐类未见过，取任意已有变体（保留原形也行）
        return byHarmony.values().iterator().next();
    }
    
    /**
     * 查找最长匹配
     */
    public List<String> findLongestMatches(String word, DictionaryView viewType) {
        Map<String, String[]> targetView = getViewMap(viewType);
        
        // 从最长到最短查找前缀匹配
        for (int i = word.length(); i > 0; i--) {
            String prefix = word.substring(0, i);
            if (targetView.containsKey(prefix)) {
                return Collections.singletonList(prefix);
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 获取指定视图的词典映射
     */
    private Map<String, String[]> getViewMap(DictionaryView viewType) {
        switch (viewType) {
            case ORIGINAL:
                return originalView;
            case SPLIT:
                return splitView;
            case CUSTOM:
                return customView;
            default:
                return splitView; // 默认返回分割视图
        }
    }
    
    // === Getter方法 ===
    
    public Map<String, String[]> getOriginalView() {
        return Collections.unmodifiableMap(originalView);
    }
    
    public Map<String, String[]> getSplitView() {
        return Collections.unmodifiableMap(splitView);
    }
    
    public Map<String, String[]> getCustomView() {
        return Collections.unmodifiableMap(customView);
    }
    
    public Map<String, String> getRawThuData() {
        return Collections.unmodifiableMap(rawThuData);
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("originalView", originalView.size());
        stats.put("splitView", splitView.size());
        stats.put("customView", customView.size());
        stats.put("stemCanonicalIndex", stemCanonicalIndex.size());
        stats.put("suffixCanonicalIndex", suffixCanonicalIndex.size());
        stats.put("rawThuData", rawThuData.size());
        return stats;
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        originalView.clear();
        splitView.clear();
        customView.clear();
        stemCanonicalIndex.clear();
        suffixCanonicalIndex.clear();
        suffixCanonicalCounts.clear();
        rawThuData.clear();
        initialized = false;
    }
    
    /**
     * 词典视图枚举
     */
    public enum DictionaryView {
        ORIGINAL,   // 元音修复形式
        SPLIT,      // 现代形式
        CUSTOM      // 自定义词汇
    }
} 