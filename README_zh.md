# Elasticsearch 维吾尔语分析器插件

[![Build](https://github.com/TocharianOU/elastic-uyghur-analyzer/actions/workflows/build.yml/badge.svg)](https://github.com/TocharianOU/elastic-uyghur-analyzer/actions/workflows/build.yml) [![English](https://img.shields.io/badge/Language-English-blue)](README.md) [![中文](https://img.shields.io/badge/语言-中文-red)](README_zh.md) [![Downloads](https://img.shields.io/github/downloads/TocharianOU/elastic-uyghur-analyzer/total)](https://github.com/TocharianOU/elastic-uyghur-analyzer/releases) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/TocharianOU/elastic-uyghur-analyzer)

一个为 Elasticsearch 提供维吾尔语文本分析和分词功能的插件。

## 概述

本插件通过自定义分析器和词单元过滤器为 Elasticsearch 添加维吾尔语支持。它处理维吾尔语形态学分析，并为不同用例提供两种分词方法。

## 功能特性

- **两种分析器类型**：
  - `uyghur_original_analyzer`：通过逆转元音弱化进行形态学恢复
  - `uyghur_split_analyzer`：直接分割，保留现代书写形式
- **形态学分析**：基于规则的分析器，具有多级置信度评分
- **自定义词典支持**：允许添加特定领域的词汇
- **THU 词典集成**：基于清华大学的 THUUyMorph 数据集

## 系统要求

- Elasticsearch 8.x 或 9.x
- Elasticsearch 8.x 构建需要 Java 17 或更高版本
- Elasticsearch 9.x 构建需要 Java 21 或更高版本

请根据 Elasticsearch 主版本选择对应插件包：

- `uyghur-analyzer-plugin-2.2.0-es8.zip` 用于 Elasticsearch 8.x
- `uyghur-analyzer-plugin-2.2.0-es9.zip` 用于 Elasticsearch 9.x

## 版本兼容性

| 插件版本 | Elasticsearch 版本 | 发布日期 | 主要功能 |
|---------|-------------------|----------|----------|
| 2.2.0-es8 | Elasticsearch 8.x，基于 8.7.0 stable plugin API 构建；已在 8.7.0 和 8.19.15 smoke test 通过 | 2026-05 | 加权形态模型、Viterbi 解码、OOV 后缀边界兜底 |
| 2.2.0-es9 | Elasticsearch 9.x，基于 9.4.0 stable plugin API 构建；已在 9.4.0 smoke test 通过 | 2026-05 | 加权形态模型、Viterbi 解码、OOV 后缀边界兜底 |

## 安装

### 方式一：从 GitHub Release 下载（推荐）

1. 下载最新插件：
   ```bash
   # Elasticsearch 8.x
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.2.0/uyghur-analyzer-plugin-2.2.0-es8.zip

   # Elasticsearch 9.x
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.2.0/uyghur-analyzer-plugin-2.2.0-es9.zip
   ```

2. 安装到 Elasticsearch：
   ```bash
   # 从本地文件安装对应版本的插件
   elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin-2.2.0-es8.zip

   # 或直接从 URL 安装 Elasticsearch 8.x 插件
   elasticsearch-plugin install https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.2.0/uyghur-analyzer-plugin-2.2.0-es8.zip

   # 或直接从 URL 安装 Elasticsearch 9.x 插件
   elasticsearch-plugin install https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.2.0/uyghur-analyzer-plugin-2.2.0-es9.zip
   ```

3. 重启 Elasticsearch 并验证安装：
   ```bash
   # 重启 Elasticsearch 服务
   sudo systemctl restart elasticsearch

   # 验证安装
   elasticsearch-plugin list
   ```

### 方式二：从源码构建

1. 克隆并构建：
   ```bash
   git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
   cd elastic-uyghur-analyzer
   ./gradlew clean check
   ```

2. 安装构建的插件：
   ```bash
   elasticsearch-plugin install file:///path/to/build/distributions/uyghur-analyzer-plugin-2.2.0-es8.zip
   ```

## 分析器行为

本插件提供两种适用于不同检索策略的分析器：

- `uyghur_original_analyzer`：在 THUUyMorph 记录元音弱化时恢复历史/词根形式。
- `uyghur_split_analyzer`：保留现代书写形式，同时拆分后缀。

示例：

| 输入 | `uyghur_original_analyzer` | `uyghur_split_analyzer` |
|------|----------------------------|-------------------------|
| `ئائىلىدىكى` | `ئائىلە + دىكى` | `ئائىلى + دىكى` |
| `يېزىش` | `ياز + ىش` | `يېز + ىش` |

## 评测基准

独立评测项目 ES-UG Benchmark 位于 [TocharianOU/es-ug-benchmark](https://github.com/TocharianOU/es-ug-benchmark)。该仓库提供完整测试框架，包括语料解析、基于 Docker 的 Elasticsearch 启动方式、分词质量评测、信息检索指标，以及用于对比不同 analyzer 行为的 Streamlit UI。详细的评测说明和结果会在该仓库中维护。

## 使用方法

### 创建使用维吾尔语分析器的索引

```bash
curl -k -X PUT "https://localhost:9200/uyghur_index" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'{
  "settings": {
    "analysis": {
      "analyzer": {
        "uyghur_analyzer": {
          "type": "uyghur_original_analyzer"
        }
      }
    }
  }
}'
```

### 测试分词

```bash
curl -k -X POST "https://localhost:9200/uyghur_index/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'{
  "analyzer": "uyghur_analyzer",
  "text": "ئورۇنلاشتۇرۇشلارنى"
}'
```

预期输出：
```json
{
  "tokens": [
    {"token": "ئورۇنلاشتۇرۇشلار", "start_offset": 0, "end_offset": 16, "type": "word", "position": 0},
    {"token": "نى", "start_offset": 17, "end_offset": 19, "type": "word", "position": 1}
  ]
}
```

## 配置

插件使用以下词典文件：
- `custom_dictionary.txt`：用户定义的词汇（最高优先级）
- `thuuy_morph_raw.txt`：THU 形态学数据集

## 验证

安装插件并重启 Elasticsearch 后，可以使用 `_analyze` 分别验证两个分析器：

```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'{
  "analyzer": "uyghur_original_analyzer",
  "text": "ئائىلىدىكى"
}'
```

然后将 analyzer 改为 `uyghur_split_analyzer` 再执行一次。对于 `ئائىلىدىكى`、`يېزىش` 等词，两种分析器应返回不同的词根形式。

## 从源码构建

用于开发或自定义：

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
./gradlew clean check
```

构建的插件将位于 `build/distributions/uyghur-analyzer-plugin-2.2.0-es8.zip`

构建 Elasticsearch 9.x 插件包时，使用 Java 21 或更高版本以及 Gradle 8.14：

```bash
curl -fsSL https://services.gradle.org/distributions/gradle-8.14-bin.zip -o /tmp/gradle-8.14-bin.zip
unzip -q /tmp/gradle-8.14-bin.zip -d /tmp
/tmp/gradle-8.14/bin/gradle clean check -PesMajor=9 -PelasticsearchVersion=9.4.0 -PluceneVersion=10.4.0
```

Elasticsearch 9.x 插件将位于 `build/distributions/uyghur-analyzer-plugin-2.2.0-es9.zip`。

## 文档

- [构建指南](docs/build_guide_zh.md)
- [入门指南](docs/getting_started_zh.md)
- [词典配置](docs/dictionary_explanation_zh.md)
- [常见问题](docs/faq_zh.md)

## 数据来源

本插件使用清华大学自然语言处理实验室开发的 THUUyMorph 数据集。
- 网站：http://thuuymorph.thunlp.org/
- 引用：THUUyMorph - A Uyghur Morphological Analysis Corpus (CCL/NLP-NABD 2017)

随插件提供的 `thuuy_morph_raw.txt` 词典是基于 THUUyMorph 资源生成的第三方数据。使用、分发或在研究/衍生工作中使用该词典时，请保留数据来源说明并引用 THUUyMorph 论文。

## 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE) 文件。

## 贡献

查看[贡献指南](docs/contribution_guide_zh.md)了解开发设置和指导原则。

## 支持

- GitHub Issues：[报告问题](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)
- 文档：查看 `docs/` 目录获取详细指南
