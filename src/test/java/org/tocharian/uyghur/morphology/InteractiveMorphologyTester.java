/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tocharian.uyghur.morphology;

import org.tocharian.uyghur.morphology.analyzer.RuleBasedMorphologyAnalyzer;
import org.tocharian.uyghur.morphology.analyzer.MorphologyAnalysisResult;

import java.io.IOException;
import java.util.Scanner;

/**
 * 交互式维吾尔语形态分析测试器
 * 支持终端输入和实时分词分析
 */
public class InteractiveMorphologyTester {
    
    private static final String WELCOME_MESSAGE = """
        ===================================================
        🔤 维吾尔语形态分析器 - 交互式测试工具
        ===================================================
        
        欢迎使用维吾尔语形态分析器！
        
        功能说明：
        • 输入维吾尔语词汇，获得详细的形态分析结果
        • 支持词典匹配、规则分析和统计预测
        • 提供置信度评估和分析方法说明
        
        使用说明：
        • 直接输入维吾尔语文本进行分析
        • 输入 'help' 查看帮助信息
        • 输入 'stats' 查看分析器统计信息
        • 输入 'examples' 查看测试示例
        • 输入 'quit' 或 'exit' 退出程序
        
        ===================================================
        """;
    
    private static final String HELP_MESSAGE = """
        
        📖 帮助信息：
        
        命令列表：
        • help        - 显示此帮助信息
        • stats       - 显示分析器统计信息
        • examples    - 显示测试示例
        • clear       - 清屏
        • quit/exit   - 退出程序
        
        分析结果说明：
        • 高置信度 (>80%) ✓  - 结果非常可靠
        • 中等置信度 (50-80%) ⚠ - 结果较为可靠
        • 低置信度 (<50%) ✗  - 结果仅供参考
        
        分析方法：
        • DICTIONARY_EXACT  - 词典精确匹配
        • DICTIONARY_PARTIAL - 词典部分匹配
        • RULE_BASED       - 基于规则分析
        • STATISTICAL      - 统计模型预测
        • FALLBACK         - 回退策略
        """;
    
    private static final String[] EXAMPLE_WORDS = {
        "ئائىلىدىكى",      // 家庭的
        "ئاخباراتلار",     // 消息们
        "ئادەملەرنىڭ",     // 人们的
        "ئاتموسفېرادىكى",  // 大气层的
        "كىتابخانىدا",      // 在图书馆
        "دوستلىرىمنىڭ",    // 我的朋友们的
        "ئۆيلەردىكى",      // 房子们里的
        "ئىشلارنى"         // 工作们
    };
    
    private RuleBasedMorphologyAnalyzer analyzer;
    private Scanner scanner;
    
    public static void main(String[] args) {
        InteractiveMorphologyTester tester = new InteractiveMorphologyTester();
        tester.run();
    }
    
    public void run() {
        try {
            initialize();
            showWelcomeMessage();
            startInteractiveSession();
        } catch (IOException e) {
            System.err.println("❌ 初始化失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    private void initialize() throws IOException {
        System.out.println("🔧 正在初始化维吾尔语形态分析器...");
        analyzer = new RuleBasedMorphologyAnalyzer();
        analyzer.initialize();
        scanner = new Scanner(System.in);
        System.out.println("✅ 初始化完成！\n");
    }
    
    private void showWelcomeMessage() {
        System.out.println(WELCOME_MESSAGE);
    }
    
    private void startInteractiveSession() {
        System.out.println("请输入维吾尔语文本进行分析（输入 'help' 查看帮助）：");
        
        while (true) {
            System.out.print("\n🔤 输入> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (handleCommand(input)) {
                continue;
            }
            
            // 处理维吾尔语文本分析
            analyzeText(input);
        }
    }
    
    private boolean handleCommand(String input) {
        String command = input.toLowerCase();
        
        switch (command) {
            case "quit", "exit", "q" -> {
                System.out.println("\n👋 感谢使用维吾尔语形态分析器！再见！");
                System.exit(0);
                return true;
            }
            case "help", "h" -> {
                System.out.println(HELP_MESSAGE);
                return true;
            }
            case "stats" -> {
                showStatistics();
                return true;
            }
            case "examples" -> {
                showExamples();
                return true;
            }
            case "clear", "cls" -> {
                clearScreen();
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    
    private void analyzeText(String text) {
        System.out.println("\n📊 分析结果：");
        System.out.println("═".repeat(50));
        
        // 按空格分割处理多个词
        String[] words = text.split("\\s+");
        
        for (String word : words) {
            if (word.trim().isEmpty()) {
                continue;
            }
            
            MorphologyAnalysisResult result = analyzer.analyze(word.trim());
            printDetailedResult(result);
            System.out.println("─".repeat(50));
        }
    }
    
    private void printDetailedResult(MorphologyAnalysisResult result) {
        System.out.printf("📝 原词: %s\n", result.getOriginalWord());
        System.out.printf("🔗 分词结果: %s\n", String.join(" + ", result.getMorphemes()));
        System.out.printf("📈 置信度: %.1f%%\n", result.getConfidence() * 100);
        System.out.printf("🔍 分析方法: %s\n", getMethodDescription(result.getMethod()));
        System.out.printf("💡 备注: %s\n", result.getNotes());
        
        // 置信度评估
        String confidenceIcon;
        String confidenceText;
        if (result.isHighConfidence()) {
            confidenceIcon = "✅";
            confidenceText = "高置信度 - 结果非常可靠";
        } else if (result.isMediumConfidence()) {
            confidenceIcon = "⚠️";
            confidenceText = "中等置信度 - 结果较为可靠";
        } else {
            confidenceIcon = "❌";
            confidenceText = "低置信度 - 结果仅供参考";
        }
        System.out.printf("🎯 评估: %s %s\n", confidenceIcon, confidenceText);
        
        // 形态分析详情
        if (result.getMorphemes().size() > 1) {
            System.out.println("📋 形态结构:");
            for (int i = 0; i < result.getMorphemes().size(); i++) {
                String morpheme = result.getMorphemes().get(i);
                String type = (i == 0) ? "词根" : "后缀" + i;
                System.out.printf("   %d. %s (%s)\n", i + 1, morpheme, type);
            }
        }
    }
    
    private String getMethodDescription(MorphologyAnalysisResult.AnalysisMethod method) {
        return switch (method) {
            case DICTIONARY_EXACT -> "词典精确匹配";
            case DICTIONARY_PARTIAL -> "词典部分匹配";
            case RULE_BASED -> "基于规则分析";
            case WEIGHTED_MODEL -> "加权模型解码";
            case OOV_BOUNDARY -> "未登录词边界预测";
            case STATISTICAL -> "统计模型预测";
            case FALLBACK -> "回退策略";
        };
    }
    
    private void showStatistics() {
        System.out.println("\n📊 分析器统计信息：");
        System.out.println("═".repeat(40));
        
        if (analyzer.isInitialized()) {
            analyzer.getAnalyzerStatistics().forEach((key, value) -> {
                System.out.printf("• %s: %s\n", key, value);
            });
        } else {
            System.out.println("❌ 分析器尚未初始化");
        }
    }
    
    private void showExamples() {
        System.out.println("\n🎯 测试示例：");
        System.out.println("═".repeat(40));
        System.out.println("以下是一些可以测试的维吾尔语词汇：\n");
        
        for (int i = 0; i < EXAMPLE_WORDS.length; i++) {
            System.out.printf("%d. %s\n", i + 1, EXAMPLE_WORDS[i]);
        }
        
        System.out.println("\n💡 提示：您可以复制这些词汇进行测试分析");
    }
    
    private void clearScreen() {
        // 简单的清屏方法（跨平台兼容）
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        showWelcomeMessage();
    }
    
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
