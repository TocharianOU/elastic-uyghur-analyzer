package org.tocharian.uyghur.morphology;

import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;
import org.tocharian.uyghur.morphology.analyzer.RuleBasedMorphologyAnalyzer;
import org.tocharian.uyghur.morphology.analyzer.MorphologyAnalysisResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 维吾尔语分词效果测试工具
 * 用于测试和演示统一词典系统的分词效果
 */
public class TokenizationTester {
    
    private UnifiedDictionaryManager dictionaryManager;
    private RuleBasedMorphologyAnalyzer analyzer;
    
    public TokenizationTester() throws IOException {
        System.out.println("=== 维吾尔语分词效果测试工具 ===");
        System.out.println("初始化统一词典系统...");
        
        this.dictionaryManager = new UnifiedDictionaryManager();
        this.dictionaryManager.initialize();
        
        System.out.println("初始化形态分析器...");
        this.analyzer = new RuleBasedMorphologyAnalyzer();
        this.analyzer.initialize();
        
        System.out.println("系统初始化完成！\n");
    }
    
    public static void main(String[] args) {
        try {
            TokenizationTester tester = new TokenizationTester();
            
            // 预定义测试用例
            tester.runPredefinedTests();
            
            // 交互式测试
            tester.runInteractiveTest();
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 运行预定义的测试用例
     */
    private void runPredefinedTests() {
        System.out.println("=== 预定义测试用例 ===");
        
        String[] testCases = {
            "ئائىلىدىكى",         // 有词根变体的词汇
            "ئائىلەدىكى",         // 历史形式
            "كومپيۇتېرلار",        // 自定义词典
            "مەكتەپلەردە",        // 复杂后缀组合
            "دوستلىرىمنىڭ",       // 多层后缀
            "ئوقۇغۇچىلارنىڭ",     // 长词汇
            "يېزىشىمچۇ",         // 可能无法分析的词
            "ئۆلچەنمىگەن"         // 否定形式
        };
        
        for (String testCase : testCases) {
            testTokenization(testCase);
            System.out.println();
        }
    }
    
    /**
     * 测试单个词汇的分词效果
     */
    private void testTokenization(String word) {
        System.out.println("🔍 测试词汇: " + word);
        System.out.println("─".repeat(50));
        
        // 1. 词典查找测试
        testDictionaryLookup(word);
        
        // 2. 形态分析测试
        testMorphologyAnalysis(word);
    }
    
    /**
     * 测试词典查找
     */
    private void testDictionaryLookup(String word) {
        System.out.println("📚 词典查找:");
        
        // Original视图
        String[] originalResult = dictionaryManager.lookup(word, DictionaryView.ORIGINAL);
        if (originalResult != null) {
            System.out.println("  🏛️  Original: " + Arrays.toString(originalResult) + 
                             " (词根: " + originalResult[0] + ")");
        } else {
            System.out.println("  🏛️  Original: 未找到");
        }
        
        // Split视图
        String[] splitResult = dictionaryManager.lookup(word, DictionaryView.SPLIT);
        if (splitResult != null) {
            System.out.println("  📝 Split: " + Arrays.toString(splitResult) + 
                             " (词根: " + splitResult[0] + ")");
        } else {
            System.out.println("  📝 Split: 未找到");
        }
        
        // Custom视图
        String[] customResult = dictionaryManager.lookup(word, DictionaryView.CUSTOM);
        if (customResult != null) {
            System.out.println("  ⭐ Custom: " + Arrays.toString(customResult) + 
                             " (词根: " + customResult[0] + ")");
        } else {
            System.out.println("  ⭐ Custom: 未找到");
        }
    }
    
    /**
     * 测试形态分析
     */
    private void testMorphologyAnalysis(String word) {
        System.out.println("🔬 形态分析:");
        
        MorphologyAnalysisResult result = analyzer.analyze(word);
        
        System.out.println("  结果: " + result.getMorphemes());
        System.out.println("  置信度: " + String.format("%.1f%%", result.getConfidence() * 100));
        System.out.println("  方法: " + result.getMethod());
        System.out.println("  说明: " + result.getNotes());
        
        // 分析质量评估
        if (result.getConfidence() >= 0.9) {
            System.out.println("  ✅ 高质量分析");
        } else if (result.getConfidence() >= 0.7) {
            System.out.println("  ⚠️  中等质量分析");
        } else {
            System.out.println("  ❌ 低质量分析");
        }
    }
    
    /**
     * 交互式测试
     */
    private void runInteractiveTest() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== 交互式分词测试 ===");
        System.out.println("输入维吾尔语词汇进行分词测试");
        System.out.println("命令:");
        System.out.println("  help - 显示帮助");
        System.out.println("  stats - 显示词典统计");
        System.out.println("  quit - 退出");
        System.out.println();
        
        while (true) {
            System.out.print("请输入词汇或命令: ");
            String input = scanner.nextLine().trim();
            
            if (input.equals("quit") || input.equals("exit")) {
                System.out.println("测试结束！");
                break;
            }
            
            if (input.equals("help")) {
                showHelp();
                continue;
            }
            
            if (input.equals("stats")) {
                showStatistics();
                continue;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            // 执行分词测试
            System.out.println();
            testTokenization(input);
            System.out.println();
        }
        
        scanner.close();
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelp() {
        System.out.println("\n=== 帮助信息 ===");
        System.out.println("本工具用于测试维吾尔语分词效果，包括:");
        System.out.println();
        System.out.println("📚 词典查找:");
        System.out.println("  - Original视图: 使用历史词根形式");
        System.out.println("  - Split视图: 使用现代词根形式");
        System.out.println("  - Custom视图: 用户自定义词汇");
        System.out.println();
        System.out.println("🔬 形态分析:");
        System.out.println("  - 统一词典精确匹配");
        System.out.println("  - 统一词典部分匹配");
        System.out.println("  - 基于规则的形态分析");
        System.out.println("  - 回退处理");
        System.out.println();
        System.out.println("测试示例:");
        System.out.println("  ئائىلىدىكى - 词根变体测试");
        System.out.println("  كومپيۇتېرلار - 自定义词典测试");
        System.out.println("  مەكتەپلەردە - 复杂后缀测试");
        System.out.println();
    }
    
    /**
     * 显示统计信息
     */
    private void showStatistics() {
        System.out.println("\n=== 词典统计信息 ===");
        
        var stats = dictionaryManager.getStatistics();
        
        System.out.println("Original视图: " + stats.get("originalView") + " 条");
        System.out.println("Split视图: " + stats.get("splitView") + " 条");
        System.out.println("Custom视图: " + stats.get("customView") + " 条");
        System.out.println("原始THU数据: " + stats.get("rawThuData") + " 条");
        System.out.println();
    }
}
