# Elastic Uyghur Analyzer 插件快速入门指南

本指南提供了 Elastic Uyghur Analyzer 插件的快速安装和使用说明，帮助您在 Elasticsearch 中实现维吾尔语文本分析。

## 兼容性

- Elasticsearch 8.7.0
- Java 17 或更高版本

## 安装插件

### 方法一：直接安装（适用于已有 Elasticsearch 环境）

1. 下载插件 ZIP 文件：
   ```bash
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v8.7.0/uyghur-analyzer-plugin-8.7.0.zip
   ```

2. 使用 Elasticsearch 插件管理器安装：
   ```bash
   bin/elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin-8.7.0.zip
   ```

3. 重启 Elasticsearch：
   ```bash
   # 对于基于 systemd 的系统
   systemctl restart elasticsearch
   
   # 或者对于基于 init.d 的系统
   service elasticsearch restart
   ```

### 方法二：Docker 安装

1. 启动 Elasticsearch 容器：
   ```bash
   docker run -d --name es -p 9200:9200 -p 9300:9300 \
     -e "discovery.type=single-node" \
     -e "ELASTIC_PASSWORD=your_password" \
     docker.elastic.co/elasticsearch/elasticsearch:8.7.0
   ```

2. 复制插件到容器并安装：
   ```bash
   docker cp uyghur-analyzer-plugin-8.7.0.zip es:/tmp/
   docker exec -it -u root es bash -c "chown elasticsearch:root /tmp/uyghur-analyzer-plugin-8.7.0.zip && \
     /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin-8.7.0.zip"
   ```

3. 重启容器：
   ```bash
   docker restart es
   ```

## 验证安装

检查插件是否正确安装：

```bash
bin/elasticsearch-plugin list
# 或在 Docker 中
docker exec es elasticsearch-plugin list
```

您应该能在列表中看到 `uyghur-analyzer-plugin`。

## 使用插件

### 创建使用维吾尔语分析器的索引

```bash
curl -X PUT "localhost:9200/uyghur_index" -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "uyghur_analyzer": {
          "type": "uyghur_original_analyzer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "uyghur_analyzer"
      }
    }
  }
}'
```

如果启用了安全功能，请添加认证信息：

```bash
curl -k -X PUT "https://localhost:9200/uyghur_index" \
  -u elastic:your_password \
  -H "Content-Type: application/json" \
  -d'{
  "settings": {
    "analysis": {
      "analyzer": {
        "uyghur_analyzer": {
          "type": "uyghur_original_analyzer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "uyghur_analyzer"
      }
    }
  }
}'
```

### 测试分析器

```bash
curl -X POST "localhost:9200/uyghur_index/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_analyzer",
  "text": "مەن ئۇيغۇرچە سۆزلەيمەن"
}'
```

### 索引文档

```bash
curl -X POST "localhost:9200/uyghur_index/_doc" -H "Content-Type: application/json" -d'
{
  "content": "يېزىلاردىكى ئېشىنچا ئەمگەكچىلەرنى"
}'
```

### 搜索文档

```bash
curl -X GET "localhost:9200/uyghur_index/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "content": "يېزا"
    }
  }
}'
```

## 可用分析器和过滤器

### 分析器

1. `uyghur_original_analyzer` - 保留原始词形的同时提供形态分析
2. `uyghur_split_analyzer` - 将词分解为形态组件

### 令牌过滤器

1. `uyghur_word_original` - 用于原始词形的令牌过滤器
2. `uyghur_word_split` - 用于分解词形的令牌过滤器

### 自定义分析器示例

```bash
curl -X PUT "localhost:9200/custom_index" -H "Content-Type: application/json" -d'
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
}'
```

## 故障排除

### 常见问题

1. **插件安装失败**：
   - 检查 Elasticsearch 版本是否为 8.7.0
   - 确保使用 Java 17
   - 检查 Elasticsearch 日志

2. **分析器不工作**：
   - 确认插件已正确安装
   - 检查索引设置中的分析器配置
   - 尝试重建索引

3. **搜索结果不符合预期**：
   - 使用 `_analyze` API 检查文本如何被分词
   - 调整查询以匹配分词结果

## 其他资源

- [构建指南](https://github.com/TocharianOU/elastic-uyghur-analyzer/blob/main/docs/build_guide_zh.md)
- [使用指南](https://github.com/TocharianOU/elastic-uyghur-analyzer/blob/main/docs/tutorial_zh.md)
- [常见问题](https://github.com/TocharianOU/elastic-uyghur-analyzer/blob/main/docs/faq_zh.md)

## 支持

如果您遇到任何问题或有疑问，请在 GitHub 上[提交问题](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)。


