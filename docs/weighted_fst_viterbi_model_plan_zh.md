# 加权 FST/Viterbi 与 OOV 边界预测改造计划

## 目标

把当前的“词典精确匹配 + 手写后缀规则”升级为 Java 原生的模型化形态分析体系，保持 Elasticsearch Analyzer 的低延迟、可打包、可回滚特性。

## 总体架构

1. **加权形态词典模型**
   - 从 `thuuy_morph_raw.txt` 与 `custom_dictionary.txt` 编译词形、词根、后缀、切分序列。
   - 为完整词形、词根、后缀、后缀转移设置权重。
   - 初版用 Trie/HashMap 表示，后续可替换为 Lucene FST。

2. **Viterbi 解码器**
   - 对输入词生成所有可行切分路径。
   - 按词典命中、后缀长度、转移成本、OOV 惩罚打分。
   - 返回最低成本路径，替代现有硬编码后缀规则。

3. **OOV 边界预测模型**
   - 从 THUUyMorph 切分结果自动生成字符级边界样本。
   - 初版使用 Java 原生特征打分器；后续可升级为 Perceptron 或 Logistic Regression。
   - 只在词典/Viterbi 无高置信路径时兜底。

4. **Elasticsearch 集成**
   - 模型随插件资源打包，不依赖外部 Python 服务。
   - 初始化后只读共享，保证线程安全。
   - 输出仍走现有 `MorphologyAnalysisResult`，尽量减少 Analyzer 接口变化。

## 代码拆分

```text
src/main/java/org/tocharian/uyghur/morphology/model/
├── WeightedMorphologyModel.java      # 模型数据结构与权重
├── WeightedModelCompiler.java        # 从统一词典编译模型
├── ViterbiMorphologySegmenter.java   # 最短路径/最佳路径解码
├── SegmentationPath.java             # 解码结果
└── OovBoundaryPredictor.java         # OOV 边界预测接口与初版实现
```

## 落地顺序

1. 先保留现有精确词典匹配，避免破坏已验证行为。
2. 用 Viterbi 解码替换 `tryRuleBasedAnalysis()` 与虚假的 `tryStatisticalAnalysis()`。
3. 把 OOV 边界预测接到 Viterbi 失败后的 fallback。
4. 增加单元测试覆盖：已登录词、部分匹配词、未知词、混合文本、offset 稳定性。
5. 后续再把 HashMap/Trie 模型压缩为 Lucene FST 或二进制资源。

## 约束

- 不引入重型 ML 依赖。
- 不在 Analyzer 热路径做训练或动态编译。
- 不改变 ES 8/9 双产物发布结构。
- 不牺牲 token offset 的稳定性。
- 模型版本必须和插件版本一起发布。
