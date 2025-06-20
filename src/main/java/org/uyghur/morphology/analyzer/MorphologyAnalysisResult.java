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

import java.util.List;

/**
 * 维吾尔语形态分析结果类
 * 包含分词结果、置信度和分析方法信息
 */
public class MorphologyAnalysisResult {
    
    public enum AnalysisMethod {
        DICTIONARY_EXACT,    // 词典精确匹配
        DICTIONARY_PARTIAL,  // 词典部分匹配
        RULE_BASED,         // 基于规则分析
        STATISTICAL,        // 统计模型预测
        FALLBACK           // 回退策略
    }
    
    private final String originalWord;
    private final List<String> morphemes;
    private final double confidence;
    private final AnalysisMethod method;
    private final String notes;
    
    public MorphologyAnalysisResult(String originalWord, List<String> morphemes, 
                                  double confidence, AnalysisMethod method, String notes) {
        this.originalWord = originalWord;
        this.morphemes = morphemes;
        this.confidence = confidence;
        this.method = method;
        this.notes = notes;
    }
    
    public String getOriginalWord() {
        return originalWord;
    }
    
    public List<String> getMorphemes() {
        return morphemes;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public AnalysisMethod getMethod() {
        return method;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
    
    public boolean isMediumConfidence() {
        return confidence >= 0.5 && confidence < 0.8;
    }
    
    public boolean isLowConfidence() {
        return confidence < 0.5;
    }
    
    @Override
    public String toString() {
        return String.format("MorphologyAnalysisResult{word='%s', morphemes=%s, confidence=%.2f, method=%s}", 
                           originalWord, morphemes, confidence, method);
    }
} 