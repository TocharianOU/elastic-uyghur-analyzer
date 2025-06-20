# Elasticsearch 维吾尔语分析器插件

[![English](https://img.shields.io/badge/Language-English-blue)](README.md) [![中文](https://img.shields.io/badge/语言-中文-red)](README_zh.md) [![Downloads](https://img.shields.io/github/downloads/TocharianOU/elastic-uyghur-analyzer/total)](https://github.com/TocharianOU/elastic-uyghur-analyzer/releases) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/TocharianOU/elastic-uyghur-analyzer)

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

- Elasticsearch 8.7.0 或更高版本
- Java 17 或更高版本

## 版本兼容性

| 插件版本 | Elasticsearch 版本 | 发布日期 | 主要功能 |
|---------|-------------------|----------|----------|
| v2.0-es8.7+ | 8.7.0+ | 2024-06 | 统一词典系统，形态学分析器 |

## 安装

### 方式一：从 GitHub Release 下载（推荐）

1. 下载最新插件：
   ```bash
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.0-es8.7%2B/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   ```

2. 安装到 Elasticsearch：
   ```bash
   # 从本地文件安装插件
   elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   
   # 或直接从 URL 安装
   elasticsearch-plugin install https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.0-es8.7%2B/uyghur-analyzer-plugin-v2.0-es8.7+.zip
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
   ./gradlew clean build
   ```

2. 安装构建的插件：
   ```bash
   elasticsearch-plugin install file:///path/to/build/distributions/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   ```

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

## 从源码构建

用于开发或自定义：

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
./gradlew clean build
```

构建的插件将位于 `build/distributions/uyghur-analyzer-plugin-v2.0-es8.7+.zip`

## 文档

- [构建指南](docs/build_guide_zh.md)
- [入门指南](docs/getting_started_zh.md)
- [词典配置](docs/dictionary_explanation_zh.md)
- [常见问题](docs/faq_zh.md)

## 数据来源

本插件使用清华大学自然语言处理实验室开发的 THUUyMorph 数据集。
- 网站：http://thuuymorph.thunlp.org/
- 引用：THUUyMorph - A Uyghur Morphological Analysis Corpus (CCL/NLP-NABD 2017)

## 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE) 文件。

## 贡献

查看[贡献指南](docs/contribution_guide_zh.md)了解开发设置和指导原则。

## 支持

- GitHub Issues：[报告问题](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)
- 文档：查看 `docs/` 目录获取详细指南
