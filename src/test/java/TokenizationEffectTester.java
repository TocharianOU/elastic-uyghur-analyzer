import org.tocharian.uyghur.DictionaryLoader;
import org.tocharian.uyghur.morphology.dictionary.UnifiedDictionaryManager.DictionaryView;
import org.tocharian.uyghur.morphology.analyzer.RuleBasedMorphologyAnalyzer;
import org.tocharian.uyghur.morphology.analyzer.MorphologyAnalysisResult;
import java.util.Map;
import java.util.Scanner;

/**
 * ES维吾尔语分词效果测试器
 * 用于测试Original和Split两种分析器的分词效果
 */
public class TokenizationEffectTester {
    private static DictionaryLoader originalLoader;
    private static DictionaryLoader splitLoader;
    private static DictionaryLoader customLoader;
    private static Map<String, String[]> originalDict;
    private static Map<String, String[]> splitDict;
    private static Map<String, String[]> customDict;
    private static RuleBasedMorphologyAnalyzer morphologyAnalyzer;

    public static void main(String[] args) {
        System.out.println("=== ES维吾尔语分词效果测试器 (含形态学分析算法) ===");
        System.out.println("测试Original、Split分析器和高级形态学分析算法\n");
        
        try {
            // 初始化三种分析器
            initializeAnalyzers();
            
            // 预设测试用例
            runPredefinedTests();
            
            // 交互式测试
            runInteractiveTest();
            
        } catch (Exception e) {
            System.err.println("初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化三种分析器
     */
    private static void initializeAnalyzers() throws Exception {
        System.out.println("初始化分析器...");
        
        // Original分析器
        originalLoader = new DictionaryLoader();
        originalLoader.setDictionaryView(DictionaryView.ORIGINAL);
        originalLoader.initializeFromResource("/dictionaries/thuuy_morph_raw.txt");
        originalDict = originalLoader.getDictionary();
        
        // Split分析器
        splitLoader = new DictionaryLoader();
        splitLoader.setDictionaryView(DictionaryView.SPLIT);
        splitLoader.initializeFromResource("/dictionaries/thuuy_morph_raw.txt");
        splitDict = splitLoader.getDictionary();
        
        // Custom分析器
        customLoader = new DictionaryLoader();
        customLoader.setDictionaryView(DictionaryView.CUSTOM);
        customLoader.initializeFromResource("/dictionaries/custom_dictionary.txt");
        customDict = customLoader.getDictionary();
        
        // 初始化高级形态学分析器
        morphologyAnalyzer = new RuleBasedMorphologyAnalyzer();
        morphologyAnalyzer.initialize();
        
        System.out.println("✓ Original分析器: " + originalDict.size() + " 条词汇");
        System.out.println("✓ Split分析器: " + splitDict.size() + " 条词汇");
        System.out.println("✓ Custom分析器: " + customDict.size() + " 条词汇");
        System.out.println("✓ 高级形态学分析器: 已初始化");
        System.out.println();
    }
    
    /**
     * 运行预设测试用例
     */
    private static void runPredefinedTests() {
        System.out.println("=== 预设测试用例 ===\n");
        
        // 基础词汇测试
        System.out.println("1. 基础词汇测试:");
        String[] basicWords = {
            "كىتاب",      // 书
            "بىلىم",      // 知识
            "ئوقۇغۇچى",   // 学生
            "مۇئەللىم",   // 老师
            "مەكتەب"      // 学校
        };
        testWords(basicWords);
        
        // 复合词测试
        System.out.println("\n2. 复合词测试:");
        String[] compoundWords = {
            "كىتابخانا",   // 图书馆
            "دوستلۇق",    // 友谊
            "ھەمكارلىق",  // 合作
            "بىرلىكتە"    // 一起
        };
        testWords(compoundWords);
        
        // 动词变位测试
        System.out.println("\n3. 动词变位测试:");
        String[] verbForms = {
            "ئىشلىتىش",   // 使用
            "ئوقۇش",      // 读
            "يېزىش",      // 写
            "ئاڭلاش",     // 听
            "كۆرۈش"       // 看
        };
        testWords(verbForms);
        
        // 现代科技词汇测试
        System.out.println("\n4. 现代科技词汇测试:");
        String[] techWords = {
            "كومپيۇتېر",   // 计算机
            "تېلېفون",     // 电话
            "ئىنتېرنېت",   // 互联网
            "پروگرامما",   // 程序
            "تېخنىكا"     // 技术
        };
        testWords(techWords);
    }
    
    /**
     * 测试一组词汇
     */
    private static void testWords(String[] words) {
        for (String word : words) {
            System.out.println("\n   测试词汇: " + word);
            
            // 高级形态学分析算法 (最全面的分析)
            System.out.println("   【形态学算法分析】:");
            MorphologyAnalysisResult morphResult = morphologyAnalyzer.analyze(word);
            displayMorphologyResult(morphResult);
            
            System.out.println("\n   【ES分词器对比】:");
            
            // Custom词典优先级最高
            if (customDict.containsKey(word)) {
                String[] customResult = customDict.get(word);
                System.out.println("   Custom:   " + String.join(" + ", customResult) + " [优先]");
            }
            
            // Original分析结果
            if (originalDict.containsKey(word)) {
                String[] originalResult = originalDict.get(word);
                System.out.println("   Original: " + String.join(" + ", originalResult));
            } else {
                System.out.println("   Original: 未找到");
            }
            
            // Split分析结果
            if (splitDict.containsKey(word)) {
                String[] splitResult = splitDict.get(word);
                System.out.println("   Split:    " + String.join(" + ", splitResult));
            } else {
                System.out.println("   Split:    未找到");
            }
            
            // 分析差异
            analyzeDifference(word);
        }
    }
    
    /**
     * 显示形态学分析结果
     */
    private static void displayMorphologyResult(MorphologyAnalysisResult result) {
        String methodName = getMethodDisplayName(result.getMethod());
        System.out.println("     分析策略: " + methodName);
        System.out.println("     置信度:   " + String.format("%.1f%%", result.getConfidence() * 100));
        System.out.println("     分词结果: " + String.join(" + ", result.getMorphemes()));
        
        if (result.getNotes() != null && !result.getNotes().isEmpty()) {
            System.out.println("     分析说明: " + result.getNotes());
        }
        
        // 显示算法细节
        switch (result.getMethod()) {
            case DICTIONARY_EXACT:
                System.out.println("     算法:     直接词典查找 → 精确匹配");
                break;
            case DICTIONARY_PARTIAL:
                System.out.println("     算法:     前缀匹配 → 后缀识别");
                break;
            case RULE_BASED:
                System.out.println("     算法:     形态学规则 → 模式识别");
                break;
            case STATISTICAL:
                System.out.println("     算法:     统计模型 → 概率预测");
                break;
            case FALLBACK:
                System.out.println("     算法:     回退策略 → 音节分割");
                break;
        }
    }
    
    /**
     * 获取分析方法的显示名称
     */
    private static String getMethodDisplayName(MorphologyAnalysisResult.AnalysisMethod method) {
        switch (method) {
            case DICTIONARY_EXACT:
                return "词典精确匹配";
            case DICTIONARY_PARTIAL:
                return "词典部分匹配";
            case RULE_BASED:
                return "规则分析";
            case STATISTICAL:
                return "统计预测";
            case FALLBACK:
                return "回退策略";
            default:
                return method.toString();
        }
    }
    
    /**
     * 分析Original和Split的差异
     */
    private static void analyzeDifference(String word) {
        String[] originalResult = originalDict.get(word);
        String[] splitResult = splitDict.get(word);
        
        if (originalResult != null && splitResult != null) {
            String originalStr = String.join("", originalResult);
            String splitStr = String.join("", splitResult);
            
            if (!originalStr.equals(splitStr)) {
                System.out.println("   差异:     元音弱化恢复 vs 现代书写");
            }
        }
    }
    
    /**
     * 交互式测试
     */
    private static void runInteractiveTest() {
        System.out.println("\n=== 交互式测试模式 ===");
        System.out.println("输入维吾尔语词汇进行分词测试，输入 'quit' 或 'q' 退出");
        System.out.println("支持的命令:");
        System.out.println("  - 直接输入词汇: 进行完整形态学分析和分词测试");
        System.out.println("  - 'stats': 显示词典统计信息");
        System.out.println("  - 'help': 显示帮助信息");
        System.out.println("  - 'algorithm': 显示算法详情");
        System.out.println("  - 'quit' 或 'q': 退出程序\n");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("请输入测试词汇 > ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
                System.out.println("测试结束，再见！");
                break;
            }
            
            if (input.equalsIgnoreCase("stats")) {
                showStatistics();
                continue;
            }
            
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            
            if (input.equalsIgnoreCase("algorithm")) {
                showAlgorithmDetails();
                continue;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            // 测试输入的词汇
            System.out.println("\n--- 分词结果 ---");
            testWords(new String[]{input});
            System.out.println();
        }
        
        scanner.close();
    }
    
    /**
     * 显示词典统计信息
     */
    private static void showStatistics() {
        System.out.println("\n--- 词典统计信息 ---");
        System.out.println("Original词典: " + originalDict.size() + " 条词汇");
        System.out.println("Split词典:    " + splitDict.size() + " 条词汇");
        System.out.println("Custom词典:   " + customDict.size() + " 条词汇");
        
        // 计算覆盖率统计
        int originalOnly = 0, splitOnly = 0, both = 0;
        for (String key : originalDict.keySet()) {
            if (splitDict.containsKey(key)) {
                both++;
            } else {
                originalOnly++;
            }
        }
        for (String key : splitDict.keySet()) {
            if (!originalDict.containsKey(key)) {
                splitOnly++;
            }
        }
        
        System.out.println("共同词汇:     " + both + " 条");
        System.out.println("仅Original:  " + originalOnly + " 条");
        System.out.println("仅Split:     " + splitOnly + " 条");
        System.out.println();
    }
    
    /**
     * 显示帮助信息
     */
    private static void showHelp() {
        System.out.println("\n--- 帮助信息 ---");
        System.out.println("这是ES维吾尔语分词效果测试器，包含完整的形态学分析算法:");
        System.out.println("1. 高级形态学分析器: 多层次分析策略，显示算法过程");
        System.out.println("2. Original分析器:   恢复元音弱化，显示历史形态");
        System.out.println("3. Split分析器:      保持现代书写，适合现代文本");
        System.out.println("4. Custom分析器:     用户自定义词典，优先级最高");
        System.out.println("\n分析结果说明:");
        System.out.println("- '形态学算法分析': 显示完整的分析过程和置信度");
        System.out.println("- '分析策略': 显示使用的具体算法策略");
        System.out.println("- '置信度': 显示分析结果的可信程度");
        System.out.println("- '算法': 显示具体的算法处理流程");
        System.out.println("- 'ES分词器对比': 显示三种ES分词器的结果");
        System.out.println();
    }
    
    /**
     * 显示算法详情
     */
    private static void showAlgorithmDetails() {
        System.out.println("\n--- 形态学分析算法详情 ---");
        System.out.println("本系统采用多层次分析策略，按优先级依次尝试:");
        System.out.println();
        System.out.println("1. 词典精确匹配 (置信度: 95%)");
        System.out.println("   - 算法: 直接词典查找");
        System.out.println("   - 适用: 已知词汇的精确匹配");
        System.out.println("   - 优势: 准确率最高");
        System.out.println();
        System.out.println("2. 词典部分匹配 (置信度: 85%)");
        System.out.println("   - 算法: 前缀匹配 + 后缀识别");
        System.out.println("   - 适用: 词根已知，后缀变化的词汇");
        System.out.println("   - 优势: 处理形态变化");
        System.out.println();
        System.out.println("3. 规则分析 (置信度: 75%)");
        System.out.println("   - 算法: 形态学规则 + 模式识别");
        System.out.println("   - 适用: 遵循标准形态学规则的词汇");
        System.out.println("   - 优势: 处理规则性变化");
        System.out.println();
        System.out.println("4. 统计预测 (置信度: 65%)");
        System.out.println("   - 算法: 统计模型 + 概率预测");
        System.out.println("   - 适用: 基于后缀统计的预测分割");
        System.out.println("   - 优势: 处理未知词汇");
        System.out.println();
        System.out.println("5. 回退策略 (置信度: 30%)");
        System.out.println("   - 算法: 音节分割");
        System.out.println("   - 适用: 其他方法都失败时的最后选择");
        System.out.println("   - 优势: 保证总能给出结果");
        System.out.println();
        System.out.println("系统特点:");
        System.out.println("- 多层次递进分析，优先使用高置信度方法");
        System.out.println("- 支持元音和谐律检查");
        System.out.println("- 统计后缀模式提取");
        System.out.println("- 自定义词典优先级支持");
        System.out.println();
    }
}
