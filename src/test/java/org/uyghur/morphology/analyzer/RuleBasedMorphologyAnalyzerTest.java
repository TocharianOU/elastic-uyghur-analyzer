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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 基于规则的形态分析器测试类
 */
public class RuleBasedMorphologyAnalyzerTest {
    
    public static void main(String[] args) {
        try {
            RuleBasedMorphologyAnalyzer analyzer = new RuleBasedMorphologyAnalyzer();
            analyzer.initialize();
            
            System.out.println("=== 维吾尔语形态分析器测试 ===\n");
            
            // 测试已知词汇
            testKnownWords(analyzer);
            
            // 测试陌生词汇
            testUnknownWords(analyzer);
            
            // 显示统计信息
            showStatistics(analyzer);
            
        } catch (IOException e) {
            System.err.println("初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试已知词汇（词典中存在的）
     */
    private static void testKnownWords(RuleBasedMorphologyAnalyzer analyzer) {
        System.out.println("=== 测试已知词汇 ===");
        
        List<String> knownWords = Arrays.asList(
            "ئائىلىدىكى",      // 家庭的
            "ئائىلىسى",       // 他的家庭
            "ئاخباراتلار",     // 消息们
            "ئادەملەرنىڭ",     // 人们的
            "ئاتموسفېرادىكى"   // 大气层的
        );
        
        for (String word : knownWords) {
            MorphologyAnalysisResult result = analyzer.analyze(word);
            printAnalysisResult(result);
        }
    }
    
    /**
     * 测试陌生词汇（词典中不存在的）
     */
    private static void testUnknownWords(RuleBasedMorphologyAnalyzer analyzer) {
        System.out.println("\n=== 测试陌生词汇 ===");
        
        List<String> unknownWords = Arrays.asList(
            "كىتابلىرىمنىڭ",   // 我的书们的（假设的陌生词）
            "ئۆيلەردىكى",      // 房子们里的
            "دوستلىرىنى",      // 朋友们
            "ئىشلارنى",        // 工作们
            "بىلىملىك"         // 知识的
        );
        
        for (String word : unknownWords) {
            MorphologyAnalysisResult result = analyzer.analyze(word);
            printAnalysisResult(result);
        }
    }
    
    /**
     * 显示分析器统计信息
     */
    private static void showStatistics(RuleBasedMorphologyAnalyzer analyzer) {
        System.out.println("\n=== 分析器统计信息 ===");
        analyzer.getAnalyzerStatistics().forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
    }
    
    /**
     * 打印分析结果
     */
    private static void printAnalysisResult(MorphologyAnalysisResult result) {
        System.out.printf("输入词汇: %s\n", result.getOriginalWord());
        System.out.printf("分析结果: %s\n", result.getMorphemes());
        System.out.printf("置信度: %.2f\n", result.getConfidence());
        System.out.printf("分析方法: %s\n", result.getMethod());
        System.out.printf("备注: %s\n", result.getNotes());
        
        // 根据置信度给出评估
        if (result.isHighConfidence()) {
            System.out.println("评估: 高置信度 ✓");
        } else if (result.isMediumConfidence()) {
            System.out.println("评估: 中等置信度 ⚠");
        } else {
            System.out.println("评估: 低置信度 ✗");
        }
        
        System.out.println("---");
    }
} 