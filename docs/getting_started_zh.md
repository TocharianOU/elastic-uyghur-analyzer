# 维吾尔文本分析插件（Elasticsearch）入门指南

欢迎使用维吾尔文本分析插件！本指南将为您提供在Elasticsearch环境中安装、配置和测试该插件的分步说明。

## 前提条件

开始前请确保以下几点：
- 已安装Elasticsearch 8.7.0或更高版本。
- 可以访问安装了Elasticsearch的服务器命令行。
- 拥有管理Elasticsearch插件的管理权限。

## 安装

按照以下步骤安装维吾尔文本分析插件：

1. **下载插件**
   - 从GitHub发布页面下载`.zip`文件。

2. **安装插件**
   - 在命令行中打开您的Elasticsearch目录并运行：
     ```bash
     bin/elasticsearch-plugin install file:///path/to/plugin/uyghur-analysis-plugin.zip
     ```
   - 将`/path/to/plugin/`替换为下载的`.zip`文件所在的路径。

3. **验证安装**
   - 重启Elasticsearch并通过运行以下命令确保插件已加载：
     ```bash
     curl -X GET "localhost:9200/_cat/plugins?v=true"
     ```
   - 确认已安装的插件列表中包含`uyghur_analysis_plugin`。

## 配置

`uyghur_original_analyzer`和`uyghur_split_analyzer`开箱即用，可以立即用于分析文本：

```json
PUT /my_index
{
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "uyghur_original_analyzer"
      },
      "content_split": {
        "type": "text",
        "analyzer": "uyghur_split_analyzer"
      }
    }
  }
}
```

```json
POST /my_index/_analyze
{
  "analyzer": "uyghur_original_analyzer",  //uyghur_split_analyzer
  "text": "يېزىلاردىكى ئېشىنچا ئەمگەكچىلەرنى"
}
```

这将根据维吾尔语形态学将文本分解为标记。

### 配置自定义分析器

要设置名为`my_custom_analyzer`的自定义分析器，请使用以下索引配置：

```json
PUT /costum_text
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_custom_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["uyghur_word_original"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "custom_content": {
        "type": "text",
        "analyzer": "my_custom_analyzer"
      }
    }
  }
}
```

## 后续步骤

探索预配置和自定义分析器的功能。如有任何问题或进一步的疑问，请参阅[常见问题解答](faq.md)或在GitHub上提出问题。

感谢您使用维吾尔文本分析插件（Elasticsearch）！


