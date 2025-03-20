# Elasticsearch Uyghur Analyzer Plugin

## Language / 语言
- [English](#english)  
- [中文](#中文)

---

### English

## Project Introduction
The **Elasticsearch Uyghur Analyzer Plugin** provides robust support for analyzing and processing Uyghur text within Elasticsearch, featuring two specialized analyzers and token filters designed specifically for Uyghur language needs. This plugin is ideal for natural language processing (NLP) applications requiring accurate tokenization and morphological analysis for Uyghur.

## Features for Uyghur Text Processing
- **Two Tokenization Methods**: This plugin includes two custom tokenizers for Uyghur language processing, designed to handle unique linguistic properties of Uyghur text:
  1. **uyghur_original_tokenizer**: Restores morphological features by reversing vowel weakening, bringing semantic units back to their original form.
  2. **uyghur_split_tokenizer**: Directly segments text while retaining weakened vowels, ensuring accurate representation in tokenized text.
- **Stemming and Linguistic Analysis**: Includes stemming and other linguistic feature analyses optimized for Uyghur, improving accuracy in text processing and language analysis.
- **Flexible Configuration**: Customize the analyzers and token filters to fit specific Uyghur text processing needs within Elasticsearch.

## Quick Start Guide
To quickly get started with the **Uyghur Analyzer Plugin**:
1. **Install the Plugin**: Begin with the setup instructions found in the [Getting Started Guide](docs/getting_started.md).
2. **Configure the Tokenizers**:
   - Add `uyghur_original_tokenizer` for morphology-restoring tokenization.
   - Use `uyghur_split_tokenizer` for direct segmentation with vowel weakening preserved.
3. **Run Example Queries**: Test the plugin with sample queries to verify tokenization accuracy and filtering.

For further setup details, visit the full [Getting Started Guide](docs/getting_started.md).

## Installation for Elasticsearch Uyghur Analyzer
Supported Elasticsearch Version: **[Specify Supported Versions]**.
1. Verify compatibility with your Elasticsearch version.
2. Install the plugin by following the step-by-step instructions in the [Getting Started Guide](docs/getting_started.md).

## Dictionary Configuration
The plugin includes dictionaries for morphology-based Uyghur text analysis, which you can customize as needed. For configuration steps, see the [Dictionary Explanation](docs/dictionary_explanation.md).

## Contribution Guide
We welcome contributions from the community to expand and enhance the **Elasticsearch Uyghur Analyzer Plugin**! Review the [Contribution Guide](docs/contribution_guide.md) for setup, coding standards, and contribution details.

## Frequently Asked Questions
Need help? Check out our [FAQ](docs/faq.md) for common issues and troubleshooting tips.

## License
This project is licensed under Apache License 2.0. For more details, refer to the [LICENSE](LICENSE) file.

## Data Sources and External Resources
This plugin utilizes the **THUUyMorph dataset**, a morphology-based Uyghur language segmentation corpus developed by the Tsinghua University NLP and Computational Social Science Lab.  
For dataset access and details, see [THUUyMorph](http://thuuymorph.thunlp.org/).  
If you use this dataset, please cite:  
- THUUyMorph - A Uyghur Morphological Analysis Corpus. Presented at CCL/NLP-NABD 2017 Conference.

The dictionaries `ug_mor_original.txt` and `ug_mor_split.txt` are based on resources provided by Tsinghua University, which we acknowledge for its valuable contribution to Uyghur NLP research.

## Support and Help
Need assistance? Here are ways to get support for the **Elasticsearch Uyghur Analyzer Plugin**:
1. **GitHub Issues**: Submit an issue on the [GitHub Issues page](https://github.com/your-repo/issues).
2. **Community Forums and Groups**: Join relevant forums or developer communities.
3. **Direct Contact**: Reach out to the plugin’s main developers or maintainers.

---

### 中文

## 项目简介
**Elasticsearch维吾尔语分析器插件**为Elasticsearch提供强大的维吾尔语文本分析和处理支持，包含两个专为维吾尔语需求设计的分析器和词单元过滤器。该插件非常适合需要精确分词和形态分析的自然语言处理（NLP）应用。

## 维吾尔语文本处理功能
- **两种分词方式**：插件包含两种自定义维吾尔语分词器，专为处理维吾尔语的独特语言特性而设计：
  1. **uyghur_original_tokenizer**：通过逆向元音弱化恢复形态特征，将语义单元还原为原始形式。
  2. **uyghur_split_tokenizer**：直接分割文本并保留弱化元音，确保分词文本的准确表示。
- **词干提取与语言分析**：包括针对维吾尔语优化的词干提取和其他语言特征分析，提升文本处理和语言分析的准确性。
- **灵活配置**：可根据具体维吾尔语文本处理需求，自定义分析器和词单元过滤器。

## 快速入门指南
要快速开始使用**维吾尔语分析器插件**：
1. **安装插件**：请参考[入门指南](docs/getting_started.md)中的设置说明。
2. **配置分词器**：
   - 添加`uyghur_original_tokenizer`以实现形态还原分词。
   - 使用`uyghur_split_tokenizer`进行保留弱化元音的直接分割。
3. **运行示例查询**：使用示例查询测试插件，验证分词准确性和过滤效果。

更多设置详情，请查看完整的[入门指南](docs/getting_started.md)。

## Elasticsearch维吾尔语分析器安装
支持的Elasticsearch版本：**[指定支持的版本]**。
1. 确认与你的Elasticsearch版本兼容。
2. 按照[入门指南](docs/getting_started.md)中的逐步说明安装插件。

## 词典配置
插件包含基于形态学的维吾尔语文本分析词典，可根据需要进行自定义。配置步骤请参见[词典说明](docs/dictionary_explanation.md)。

## 贡献指南
我们欢迎社区为**Elasticsearch维吾尔语分析器插件**贡献代码和改进建议！请查看[贡献指南](docs/contribution_guide.md)了解设置、编码规范和贡献详情。

## 常见问题
需要帮助？请查看我们的[FAQ](docs/faq.md)，了解常见问题和解决方法。

## 许可证
本项目采用Apache License 2.0许可。详情请参阅[LICENSE](LICENSE)文件。

## 数据来源和外部资源
本插件使用**THUUyMorph数据集**，这是清华大学自然语言处理与计算社会科学实验室开发的基于形态学的维吾尔语分词语料库。  
有关数据集的访问和详情，请参见[THUUyMorph](http://thuuymorph.thunlp.org/)。  
如果使用此数据集，请引用以下内容：  
- THUUyMorph - 维吾尔语形态分析语料库。发表于CCL/NLP-NABD 2017会议。

词典`ug_mor_original.txt`和`ug_mor_split.txt`基于清华大学提供的资源，我们衷心感谢其对维吾尔语NLP研究的宝贵贡献。

## 支持与帮助
需要帮助？以下是获取**Elasticsearch维吾尔语分析器插件**支持的途径：
1. **GitHub问题**：在[GitHub Issues页面](https://github.com/your-repo/issues)提交问题。
2. **社区论坛和小组**：加入相关论坛或开发者社区。
3. **直接联系**：联系插件的主要开发者或维护者。

---

感谢使用**Elasticsearch维吾尔语分析器插件**！我们希望它能提升你对维吾尔语内容的文本处理能力。
