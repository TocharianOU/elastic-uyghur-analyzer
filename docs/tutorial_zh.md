# Elastic Uyghur Analyzer 使用教程

本教程详细介绍了 Elastic Uyghur Analyzer 插件的使用方法和效果，帮助您充分利用维吾尔语分析功能进行文本处理和搜索。

## 目录

1. [基本概念](#基本概念)
2. [分析器类型](#分析器类型)
3. [创建索引](#创建索引)
4. [文档索引与分析](#文档索引与分析)
5. [搜索技巧](#搜索技巧)
6. [高级配置](#高级配置)
7. [性能优化](#性能优化)
8. [常见问题](#常见问题)

## 基本概念

Elastic Uyghur Analyzer 是专为维吾尔语文本分析设计的 Elasticsearch 插件。维吾尔语是一种黏着语，单词通过添加后缀形成不同的语法形式。本分析器能够识别这些形态变化，提高搜索的准确性和召回率。

### 主要特点

- 支持维吾尔语形态学分析
- 提供原始词形和分解词形两种分析模式
- 与 Elasticsearch 标准分析功能无缝集成
- 针对维吾尔语特有的语言特性进行优化

## 分析器类型

插件提供两种主要的分析器：

### 1. uyghur_original_analyzer

保留原始词形的同时提供形态分析，适合需要保持原始文本结构的场景。

```bash
curl -X POST "localhost:9200/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_original_analyzer",
  "text": "ئائىلىلەرنى"
}'

#预计输出
# {
#   "tokens": [
#     {
#       "token": "ئائىلە",
#       "start_offset": 0,
#       "end_offset": 6,
#       "type": "word",
#       "position": 0
#     },
#     {
#       "token": "لەر",
#       "start_offset": 7,
#       "end_offset": 10,
#       "type": "word",
#       "position": 1
#     },
#     {
#       "token": "نى",
#       "start_offset": 11,
#       "end_offset": 13,
#       "type": "word",
#       "position": 2
#     }
#   ]
# }
```

### 2. uyghur_split_analyzer

将词分解为形态组件，适合需要更高召回率的搜索场景。

```bash
curl -X POST "localhost:9200/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split_analyzer",
  "text": "ئائىلىلەرنى"
}'

#预计输出
# {
#   "tokens": [
#     {
#       "token": "ئائىلى",
#       "start_offset": 0,
#       "end_offset": 6,
#       "type": "word",
#       "position": 0
#     },
#     {
#       "token": "لەر",
#       "start_offset": 7,
#       "end_offset": 10,
#       "type": "word",
#       "position": 1
#     },
#     {
#       "token": "نى",
#       "start_offset": 11,
#       "end_offset": 13,
#       "type": "word",
#       "position": 2
#     }
#   ]
# }
```

## 创建索引

### 基本索引配置

以下是一个可以直接使用的基本索引配置，适用于大多数维吾尔语文本处理场景：

```bash
curl -X PUT "localhost:9200/uyghur_docs" -H "Content-Type: application/json" -d'
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
      "title": {
        "type": "text",
        "analyzer": "uyghur_analyzer"
      },
      "content": {
        "type": "text",
        "analyzer": "uyghur_original_analyzer"
      },
      "author": {
        "type": "keyword"
      },
      "publish_date": {
        "type": "date"
      }
    }
  }
}'
```

这个配置包含了标题和内容字段，同时添加了作者和发布日期字段，便于构建完整的文档系统。title和content字段都使用维吾尔语分析器，可在安装插件完成后直接对text类型的字段中定义analyzer来使用，也可以在settings中单独定义后使用。

### 多字段配置

下面的配置展示了如何对同一字段同时应用两种分析器，以支持不同的搜索需求：

```bash
curl -X PUT "localhost:9200/uyghur_advanced" -H "Content-Type: application/json" -d'
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
        "analyzer": "uyghur_original",
        "fields": {
          "split": {
            "type": "text",
            "analyzer": "uyghur_split"
          }
        }
      },
      "content": {
        "type": "text",
        "analyzer": "uyghur_original",
        "fields": {
          "split": {
            "type": "text",
            "analyzer": "uyghur_split"
          }
        }
      },
      "keywords": {
        "type": "keyword"
      },
      "category": {
        "type": "keyword"
      },
      "created_at": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      }
    }
  }
}'
```

这个高级配置的优势：

1. **双重分析**：标题和内容字段同时使用原始分析器和分解分析器
   - 使用 `title` 或 `content` 字段进行精确匹配
   - 使用 `title.split` 或 `content.split` 字段进行词根搜索，提高召回率

2. **完整字段集**：包含关键词、分类和创建时间字段，适合构建完整的内容管理系统

3. **灵活的日期格式**：支持多种日期输入格式

### 新闻网站索引示例

以下是一个为维吾尔语的内容搜索网站实用索引配置：

```bash
curl -X PUT "localhost:9200/uyghur_docs" -H "Content-Type: application/json" -d'
{
  "settings": {
    "index": {
      "number_of_shards": 1,
      "number_of_replicas": 1
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "uyghur_split_analyzer",
        "boost": 2.0
      },
      "summary": {
        "type": "text",
        "analyzer": "uyghur_original_analyzer"
      },
      "content": {
        "type": "text",
        "analyzer": "uyghur_original_analyzer"
      },
      "author": {
        "type": "keyword"
      },
      "source": {
        "type": "keyword"
      },
      "category": {
        "type": "keyword"
      },
      "tags": {
        "type": "keyword"
      },
      "publish_date": {
        "type": "date"
      },
      "location": {
        "type": "geo_point"
      }
    }
  }
}'
```

这个内容网站配置提供了：

- 标题字段权重提升，使标题匹配的结果排名更靠前
- 地理位置字段，支持基于位置的新闻搜索
- 多个分类字段，便于内容组织和过滤
- 优化的分片设置，适合大型内容搜索网站

## 文档索引与分析

### 索引示例文档

```bash
curl -X POST "localhost:9200/uyghur_docs/_doc/1" -H "Content-Type: application/json" -d'
{
  "title": "يۇقۇملۇق كېسەللەر توغرىسىدا",
  "content": "يۇقۇملۇق كېسەللەر ۋىرۇس، باكتېرىيە ياكى باشقا مىكروئورگانىزملار تەرىپىدىن كېلىپ چىقىدىغان كېسەللەر بولۇپ، بۇلار ئادەملەر ئارىسىدا ئاسانلا تارقىلىدۇ. بۇ خىل كېسەللەردىن ساقلىنىش ئۈچۈن شەخسىي پاكىزلىققا دىققەت قىلىش ۋە ۋاقتىدا ۋاكسىنا ئۇرۇش مۇھىم."
}'

curl -X POST "localhost:9200/uyghur_docs/_doc/2" -H "Content-Type: application/json" -d'
{
  "title": "يۈرەك كېسەللىكلىرىنىڭ دىئاگنوزى",
  "content": "يۈرەك كېسەللىكلىرىنى دىئاگنوز قىلىشتا ئېلېكتروكاردىئوگرام (ئې.ك.گ)، ئېكوكاردىئوگرافىيە ۋە قان تەكشۈرۈش قاتارلىق ئۇسۇللار قوللىنىلىدۇ. بېمارلاردا كۆكرەك ئاغرىقى، نەپەس قىسىش ياكى ھالسىزلىق ئالامەتلىرى كۆرۈلگەندە، دەرھال دوختۇرغا مۇراجىئەت قىلىش كېرەك."
}'

curl -X POST "localhost:9200/uyghur_docs/_doc/3" -H "Content-Type: application/json" -d'
{
  "title": "ئۆي ئىجارە كېلىشىمى",
  "content": "بۇ كېلىشىم ئۆي ئىگىسى ۋە ئىجارىكەش ئوتتۇرىسىدا تۈزۈلگەن بولۇپ، ئىجارە مۇددىتى، ئايلىق ئىجارە ھەققى، تۆلەم ئۇسۇلى ۋە تەرەپلەرنىڭ مەجبۇرىيەتلىرىنى بەلگىلەيدۇ. كېلىشىمنى بۇزغان تەرەپ قانۇن بويىچە جاۋابكارلىققا تارتىلىدۇ."
}'

curl -X POST "localhost:9200/uyghur_docs/_doc/4" -H "Content-Type: application/json" -d'
{
  "title": "جىنايى ئىشلار قانۇنى بويىچە ئەرز قىلىش جەريانى",
  "content": "جىنايى ئىشلار قانۇنى بويىچە ئەرز قىلىش جەريانىدا، دەۋاگەر دەلىل-ئىسپاتلارنى توپلاپ، ساقچى ئىدارىسىگە ياكى تەپتىش مەھكىمىسىگە مۇراجىئەت قىلىشى كېرەك. ئەرزنامە يېزىلغاندا ۋەقە جەريانى، ۋاقتى، ئورنى ۋە ئالاقىدار شەخسلەر توغرىسىدا تەپسىلىي مەلۇمات بېرىلىشى لازىم."
}'

curl -X POST "localhost:9200/uyghur_docs/_doc/5" -H "Content-Type: application/json" -d'
{
  "title": "قەنت كېسىلى ۋە ئۇنىڭ داۋالاش ئۇسۇللىرى ھەققىدە تەتقىقات",
  "content": "بۇ تەتقىقاتتا قەنت كېسىلىنىڭ ئۇيغۇر نوپۇسى ئارىسىدىكى تارقىلىشى ۋە ئۇنى داۋالاشنىڭ زامانىۋى ئۇسۇللىرى مۇھاكىمە قىلىنغان. تەتقىقات نەتىجىلىرى كۆرسەتكەندەك، كۈندىلىك تۇرمۇش ئادەتلىرىنى ئۆزگەرتىش، مۇۋاپىق بەدەن چېنىقتۇرۇش ۋە دورا داۋالاش ئارقىلىق قەنت كېسىلىنى ئۈنۈملۈك كونترول قىلغىلى بولىدۇ."
}'

curl -X POST "localhost:9200/uyghur_docs/_doc/6" -H "Content-Type: application/json" -d'
{
  "title": "مۈلۈك ۋارىسلىق قانۇنى چۈشەندۈرۈلۈشى",
  "content": "مۈلۈك ۋارىسلىق قانۇنى بويىچە، ۋاپات بولغۇچىنىڭ ۋەسىيىتى بولمىغان ئەھۋالدا، مۈلكى قانۇنىي ۋارىسلارغا تەقسىم قىلىنىدۇ. بىرىنچى دەرىجىلىك ۋارىسلار جۈملىسىگە پەرزەنتلەر، ئايال-خوتۇن ۋە ئاتا-ئانىلار كىرىدۇ. ئەگەر بىرىنچى دەرىجىلىك ۋارىسلار بولمىسا، ئىككىنچى دەرىجىلىك ۋارىسلار مۈلۈككە ۋارىسلىق قىلىدۇ."
}'
```

### 分析文本示例

分析标题字段：

```bash
curl -X POST "localhost:9200/uyghur_docs/_analyze" -H "Content-Type: application/json" -d'
{
  "field": "title",
  "text": "يېزىلاردىكى ئوقۇتۇش"
}'
```

分析内容字段中的特定词：

```bash
curl -X POST "localhost:9200/uyghur_docs/_analyze" -H "Content-Type: application/json" -d'
{
  "field": "content",
  "text": "قانۇنى"
}'
```

## 搜索技巧

### 基本搜索

```bash
# 简单词匹配
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "title": "قانۇنى ھۆججەت"
    }
  }
}'
```

### 词根搜索

即使搜索词根形式，也能匹配包含各种后缀的词：

```bash
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "multi_match": {
     "fields": ["title","content"],
     "query": "كېسەل "
    }
  }
}
'
```


### 复合查询

```bash
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "bool": {
      "must": [
        { "match": { "content": "قانۇنى" }}
        
      ],
      "must_not": [
        { "match": { "content": "كېلىشىم" }}
      ]
    }
  },
  "min_score": 0.5
}'
```

### 高亮显示

```bash
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "title": "كېسەل"
    }
  },
  "highlight": {
    "fields": {
      "title": {}
    }
  }
}'
```

## 高级配置

### 自定义分析器

结合标准分词器和维吾尔语词形过滤器：

```bash
curl -X PUT "localhost:9200/custom_uyghur" -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "custom_uyghur_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase",
            "uyghur_word_original"
          ]
        }
      }
    }
  }
}'
```

### 同义词配置

添加维吾尔语同义词支持：

```bash
curl -X PUT "localhost:9200/uyghur_synonyms" -H "Content-Type: application/json" -d'
{
  "settings": {
    "analysis": {
      "filter": {
        "uyghur_synonym_filter": {
          "type": "synonym",
          "synonyms": [
            "كومپيۇتېر, كومپيۇتىر, كومپيوتېر",
            "ئىنتېرنېت, تور"
          ]
        }
      },
      "analyzer": {
        "uyghur_synonym_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "uyghur_word_original",
            "uyghur_synonym_filter"
          ]
        }
      }
    }
  }
}'
```

## 性能优化

### 索引设置优化

```bash
curl -X PUT "localhost:9200/uyghur_optimized" -H "Content-Type: application/json" -d'
{
  "settings": {
    "index": {
      "number_of_shards": 3,
      "number_of_replicas": 1,
      "refresh_interval": "5s"
    },
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

## 常见问题

### 问题：搜索结果不符合预期

**解决方案**：使用 `_analyze` API 检查文本如何被分词，确保查询使用了正确的分析器。

```bash
curl -X POST "localhost:9200/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_original_analyzer",
  "text": "您的搜索词"
}'
```

### 问题：某些词形变化无法匹配

**解决方案**：考虑使用 `uyghur_split_analyzer` 或在多字段配置中同时使用两种分析器。（即将更新算法和字典库）

### 问题：分析器性能问题

**解决方案**：
- 优化索引设置
- 考虑增加服务器资源
- 使用缓存
- 监控分析器性能

---

通过本教程，您应该能够充分利用 Elastic Uyghur Analyzer 插件的功能，为维吾尔语文本处理和搜索提供更好的支持。如有更多问题，请参考官方文档或在 GitHub 上提交问题。 
