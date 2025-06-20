# Elasticsearch 维吾尔语分析器插件快速入门指南

本指南提供了 Elasticsearch 维吾尔语分析器插件的快速安装和使用说明，帮助您在 Elasticsearch 中实现高级维吾尔语文本分析和形态学处理。

## 插件特性

- **统一词典系统**：集成THU形态学数据集和自定义词典
- **智能形态学分析**：多层次分析策略，支持未知词汇处理
- **双视图分析**：原始形式还原和现代形式分割
- **高性能处理**：基于哈希表的快速词汇查找
- **可扩展架构**：支持自定义词汇和分析策略

## 兼容性

- **Elasticsearch**: 8.7+
- **Java**: 17 或更高版本
- **内存**: 建议至少64MB用于词典加载
- **存储**: 约50MB用于插件和词典文件

## 构建和安装插件

### 从源码构建

1. **克隆项目**：
   ```bash
   git clone https://github.com/your-repo/elastic-uyghur-analyzer.git
   cd elastic-uyghur-analyzer
   ```

2. **构建插件**：
   ```bash
   ./gradlew clean build
   ```
   
   构建完成后，插件文件位于 `build/distributions/` 目录。

### 方法一：直接安装（适用于已有 Elasticsearch 环境）

1. **安装插件**：
   ```bash
   elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin.zip
   ```

2. **重启 Elasticsearch**：
   ```bash
   # 对于基于 systemd 的系统
   systemctl restart elasticsearch
   
   # 或者对于基于 init.d 的系统
   service elasticsearch restart
   ```

### 方法二：Docker 安装

1. **启动 Elasticsearch 容器**：
   ```bash
   docker run -d --name es -p 9200:9200 -p 9300:9300 \
     -e "discovery.type=single-node" \
     -e "ELASTIC_PASSWORD=your_password" \
     docker.elastic.co/elasticsearch/elasticsearch:8.11.0
   ```

2. **复制插件到容器并安装**：
   ```bash
   docker cp build/distributions/uyghur-analyzer-plugin.zip es:/tmp/
   docker exec -it -u root es elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin.zip
   ```

3. **重启容器**：
   ```bash
   docker restart es
   ```

## 验证安装

检查插件是否正确安装：

```bash
elasticsearch-plugin list
# 或在 Docker 中
docker exec es elasticsearch-plugin list
```

您应该能在列表中看到 `uyghur-analyzer-plugin`。

## 基本使用

### 创建使用维吾尔语分析器的索引

```bash
curl -k -X PUT "https://localhost:9200/uyghur_index" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "uyghur_original": {
          "type": "uyghur_original_analyzer"
        },
        "uyghur_split": {
          "type": "uyghur_split_analyzer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "uyghur_split"
      },
      "content": {
        "type": "text",
        "analyzer": "uyghur_original"
      }
    }
  }
}'
```

### 测试分析器效果

**测试原始分析器**（元音弱化还原）：
```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_original",
  "text": "يېزىشقا كىتابلارنىڭ"
}'
```

**测试分割分析器**（现代形式保持）：
```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split",
  "text": "يېزىشقا كىتابلارنىڭ"
}'
```

**测试形态学分析**：
```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split",
  "text": "ئورۇنلاشتۇرۇشلارنى تاكسىدا"
}'
```

### 索引维吾尔语文档

```bash
curl -k -X POST "https://localhost:9200/uyghur_index/_doc" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "title": "تېخنىكا تەرەققىياتى",
  "content": "كومپيۇتېر تېخنىكىسىنىڭ تەرەققىياتى بىلەن ئىنسانلارنىڭ تۇرمۇش سۈپىتى ياخشىلاندى"
}'
```

```bash
curl -k -X POST "https://localhost:9200/uyghur_index/_doc" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "title": "مائارىپ ئىشلىرى",
  "content": "مۇئەللىملار ۋە ئوقۇغۇچىلار ئارىسىدىكى ئالاقە ناھايىتى مۇھىم"
}'
```

### 搜索维吾尔语文档

**基本搜索**：
```bash
curl -k -X GET "https://localhost:9200/uyghur_index/_search" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "content": "تېخنىكا"
    }
  }
}'
```

**多字段搜索**：
```bash
curl -k -X GET "https://localhost:9200/uyghur_index/_search" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "query": {
    "multi_match": {
      "query": "مۇئەللىم",
      "fields": ["title", "content"]
    }
  }
}'
```

**形态学匹配搜索**：
```bash
curl -k -X GET "https://localhost:9200/uyghur_index/_search" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "content": "ياز"
    }
  },
  "highlight": {
    "fields": {
      "content": {}
    }
  }
}'
```

## 分析器详解

### 可用分析器

1. **`uyghur_original_analyzer`**
   - **功能**：提供元音弱化还原的历史形式
   - **适用场景**：学术研究、历史文本分析
   - **示例**：`يېزىش` → `يازىش`

2. **`uyghur_split_analyzer`**
   - **功能**：基于形态学的智能分词，保持现代形式
   - **适用场景**：现代文本搜索、内容分析
   - **示例**：`تاكسىدا` → `تاكسى` + `دا`

### 令牌过滤器

1. **`uyghur_word_original`** - 原始形式的词汇过滤器
2. **`uyghur_word_split`** - 分割形式的词汇过滤器

### 自定义分析器配置

```bash
curl -k -X PUT "https://localhost:9200/custom_uyghur_index" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_uyghur_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase",
            "uyghur_word_split"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "text": {
        "type": "text",
        "analyzer": "my_uyghur_analyzer"
      }
    }
  }
}'
```

## 高级功能

### 自定义词典管理

1. **查看当前自定义词典**：
   ```bash
   # 词典文件位于插件内部
   # src/main/resources/dictionaries/custom_dictionary.txt
   ```

2. **添加自定义词汇**（需要重新构建插件）：
   ```
   # 在 custom_dictionary.txt 中添加
   ئەپ
   ئەپ. لار
   ۋېبسايت
   ۋېبسايت. تا
   ```

### 形态学分析测试

使用内置测试器验证形态学分析效果：

```bash
# 编译并运行交互式测试器
./gradlew compileJava
java -cp build/classes/java/main:build/resources/main org.uyghur.morphology.InteractiveMorphologyTester
```

## 故障排除

### 常见问题

1. **插件安装失败**：
   ```
   错误：java.nio.file.AccessDeniedException
   解决：使用 root 权限或检查文件权限
   ```

2. **词典加载失败**：
   ```
   错误：Dictionary file not found
   解决：检查词典文件是否正确打包在插件中
   ```

3. **分析器不工作**：
   ```
   错误：Unknown analyzer type
   解决：确认插件正确安装并重启 Elasticsearch
   ```

4. **内存不足**：
   ```
   错误：OutOfMemoryError during dictionary loading
   解决：增加 Elasticsearch 堆内存设置
   ```

### 调试和日志

启用详细日志记录：

```yaml
# 在 elasticsearch.yml 中添加
logger.org.tocharian: DEBUG
logger.org.uyghur: DEBUG
```

查看分析过程：
```bash
curl -k -X POST "https://localhost:9200/_analyze?explain=true" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split",
  "text": "تېخنىكىلىق"
}'
```

## 性能优化

### 内存配置

```yaml
# elasticsearch.yml
# 为词典加载分配足够内存
-Xms2g
-Xmx2g
```

### 索引优化

```bash
# 创建高性能索引配置
curl -k -X PUT "https://localhost:9200/optimized_uyghur_index" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "analysis": {
      "analyzer": {
        "fast_uyghur": {
          "type": "uyghur_split_analyzer"
        }
      }
    }
  }
}'
```

## 实际应用示例

### 新闻搜索系统

```bash
# 创建新闻索引
curl -k -X PUT "https://localhost:9200/uyghur_news" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "news_analyzer": {
          "type": "uyghur_split_analyzer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "news_analyzer"
      },
      "content": {
        "type": "text",
        "analyzer": "news_analyzer"
      },
      "category": {
        "type": "keyword"
      },
      "date": {
        "type": "date"
      }
    }
  }
}'
```

### 学术文献检索

```bash
# 创建学术文献索引
curl -k -X PUT "https://localhost:9200/uyghur_academic" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "academic_analyzer": {
          "type": "uyghur_original_analyzer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "academic_analyzer"
      },
      "abstract": {
        "type": "text",
        "analyzer": "academic_analyzer"
      },
      "keywords": {
        "type": "text",
        "analyzer": "keyword"
      }
    }
  }
}'
```

## 其他资源

- [词典说明文档](dictionary_explanation_zh.md) - 详细的词典系统说明
- [构建指南](build_guide_zh.md) - 完整的构建和开发指南
- [教程文档](tutorial_zh.md) - 深入的使用教程
- [常见问题](faq_zh.md) - 详细的FAQ和故障排除

## 技术支持

- **GitHub Issues**: [提交问题](https://github.com/your-repo/elastic-uyghur-analyzer/issues)
- **文档**: 查看项目文档获取更多技术细节
- **社区**: 参与开源社区讨论和贡献

---

开始使用 Elasticsearch 维吾尔语分析器插件，体验先进的维吾尔语文本处理能力！


