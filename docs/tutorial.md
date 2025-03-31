# Elastic Uyghur Analyzer Tutorial

This tutorial provides detailed instructions on how to use the Elastic Uyghur Analyzer plugin and its effects, helping you fully utilize Uyghur language analysis capabilities for text processing and search.

## Table of Contents

1. [Basic Concepts](#basic-concepts)
2. [Analyzer Types](#analyzer-types)
3. [Creating Indices](#creating-indices)
4. [Document Indexing and Analysis](#document-indexing-and-analysis)
5. [Search Techniques](#search-techniques)
6. [Advanced Configuration](#advanced-configuration)
7. [Performance Optimization](#performance-optimization)
8. [Frequently Asked Questions](#frequently-asked-questions)

## Basic Concepts

Elastic Uyghur Analyzer is an Elasticsearch plugin designed specifically for Uyghur text analysis. Uyghur is an agglutinative language where words form different grammatical forms by adding suffixes. This analyzer can recognize these morphological variations, improving the accuracy and recall rate of searches.

### Key Features

- Support for Uyghur morphological analysis
- Provides both original word form and decomposed word form analysis modes
- Seamless integration with Elasticsearch standard analysis functions
- Optimized for Uyghur language-specific characteristics

## Analyzer Types

The plugin provides two main analyzers:

### 1. uyghur_original_analyzer

Preserves the original word forms while providing morphological analysis, suitable for scenarios where maintaining the original text structure is needed.

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

Decomposes words into morphological components, suitable for search scenarios requiring higher recall rates.

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

## Creating Indices

### Basic Index Configuration

Here is a basic index configuration that can be used directly, suitable for most Uyghur text processing scenarios:

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

### Multi-field Configuration

Using different analyzers for the same field to support different search requirements:

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
      "content": {
        "type": "text",
        "analyzer": "uyghur_original",
        "fields": {
          "split": {
            "type": "text",
            "analyzer": "uyghur_split"
          }
        }
      }
    }
  }
}'
```

## Document Indexing and Analysis

### Sample Document Indexing

```bash
# Medical document example
curl -X POST "localhost:9200/uyghur_docs/_doc/1" -H "Content-Type: application/json" -d'
{
  "title": "يۇقۇملۇق كېسەللەر توغرىسىدا",
  "content": "يۇقۇملۇق كېسەللەر ۋىرۇس، باكتېرىيە ياكى باشقا مىكروئورگانىزملار تەرىپىدىن كېلىپ چىقىدىغان كېسەللەر بولۇپ، بۇلار ئادەملەر ئارىسىدا ئاسانلا تارقىلىدۇ. بۇ خىل كېسەللەردىن ساقلىنىش ئۈچۈن شەخسىي پاكىزلىققا دىققەت قىلىش ۋە ۋاقتىدا ۋاكسىنا ئۇرۇش مۇھىم."
}'

# Medical diagnosis example
curl -X POST "localhost:9200/uyghur_docs/_doc/2" -H "Content-Type: application/json" -d'
{
  "title": "يۈرەك كېسەللىكلىرىنىڭ دىئاگنوزى",
  "content": "يۈرەك كېسەللىكلىرىنى دىئاگنوز قىلىشتا ئېلېكتروكاردىئوگرام (ئې.ك.گ)، ئېكوكاردىئوگرافىيە ۋە قان تەكشۈرۈش قاتارلىق ئۇسۇللار قوللىنىلىدۇ. بېمارلاردا كۆكرەك ئاغرىقى، نەپەس قىسىش ياكى ھالسىزلىق ئالامەتلىرى كۆرۈلگەندە، دەرھال دوختۇرغا مۇراجىئەت قىلىش كېرەك."
}'

# Legal document example - Civil contract
curl -X POST "localhost:9200/uyghur_docs/_doc/3" -H "Content-Type: application/json" -d'
{
  "title": "ئۆي ئىجارە كېلىشىمى",
  "content": "بۇ كېلىشىم ئۆي ئىگىسى ۋە ئىجارىكەش ئوتتۇرىسىدا تۈزۈلگەن بولۇپ، ئىجارە مۇددىتى، ئايلىق ئىجارە ھەققى، تۆلەم ئۇسۇلى ۋە تەرەپلەرنىڭ مەجبۇرىيەتلىرىنى بەلگىلەيدۇ. كېلىشىمنى بۇزغان تەرەپ قانۇن بويىچە جاۋابكارلىققا تارتىلىدۇ."
}'

# Legal document example - Criminal case
curl -X POST "localhost:9200/uyghur_docs/_doc/4" -H "Content-Type: application/json" -d'
{
  "title": "جىنايى ئىشلار قانۇنى بويىچە ئەرز قىلىش جەريانى",
  "content": "جىنايى ئىشلار قانۇنى بويىچە ئەرز قىلىش جەريانىدا، دەۋاگەر دەلىل-ئىسپاتلارنى توپلاپ، ساقچى ئىدارىسىگە ياكى پروكۇرورلۇق ئورگىنىغا مۇراجىئەت قىلىشى كېرەك. ئەرزنامە يېزىلغاندا ۋەقە جەريانى، ۋاقتى، ئورنى ۋە ئالاقىدار شەخسلەر توغرىسىدا تەپسىلىي مەلۇمات بېرىلىشى لازىم."
}'

# Medical research paper example
curl -X POST "localhost:9200/uyghur_docs/_doc/5" -H "Content-Type: application/json" -d'
{
  "title": "قەنت كېسىلى ۋە ئۇنىڭ داۋالاش ئۇسۇللىرى ھەققىدە تەتقىقات",
  "content": "بۇ تەتقىقاتتا قەنت كېسىلىنىڭ ئۇيغۇر نوپۇسى ئارىسىدىكى تارقىلىشى ۋە ئۇنى داۋالاشنىڭ زامانىۋى ئۇسۇللىرى مۇھاكىمە قىلىنغان. تەتقىقات نەتىجىلىرى كۆرسەتكەندەك، كۈندىلىك تۇرمۇش ئادەتلىرىنى ئۆزگەرتىش، مۇۋاپىق بەدەن چېنىقتۇرۇش ۋە دورا داۋالاش ئارقىلىق قەنت كېسىلىنى ئۈنۈملۈك كونترول قىلغىلى بولىدۇ."
}'

# Legal interpretation document example
curl -X POST "localhost:9200/uyghur_docs/_doc/6" -H "Content-Type: application/json" -d'
{
  "title": "مۈلۈك ۋارىسلىق قانۇنى چۈشەندۈرۈلۈشى",
  "content": "مۈلۈك ۋارىسلىق قانۇنى بويىچە، مەرھۇمنىڭ ۋەسىيىتى بولمىغان ئەھۋالدا، مۈلكى قانۇنىي ۋارىسلارغا تەقسىم قىلىنىدۇ. بىرىنچى دەرىجىلىك ۋارىسلار جۈملىسىگە پەرزەنتلەر، ئايال-خوتۇن ۋە ئاتا-ئانىلار كىرىدۇ. ئەگەر بىرىنچى دەرىجىلىك ۋارىسلار بولمىسا، ئىككىنچى دەرىجىلىك ۋارىسلار مۈلۈككە ۋارىسلىق قىلىدۇ."
}'
```

### Text Analysis Examples

Analyzing the title field:

```bash
curl -X POST "localhost:9200/uyghur_docs/_analyze" -H "Content-Type: application/json" -d'
{
  "field": "title",
  "text": "يېزىلاردىكى ئوقۇتۇش"
}'
```

Analyzing specific words in the content field:

```bash
curl -X POST "localhost:9200/uyghur_docs/_analyze" -H "Content-Type: application/json" -d'
{
  "field": "content",
  "text": "ئوقۇغۇچىلارنىڭ"
}'
```

## Search Techniques

### Basic Search

```bash
# Simple word matching
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "content": "ئوقۇغۇچى"
    }
  }
}'
```

### Root Word Search

Even when searching for root forms, it can match words with various suffixes:

```bash
# Searching for "يېزا" (village) can match "يېزىلاردىكى" (in villages)
curl -X GET "localhost:9200/uyghur_advanced/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "match": {
      "content.split": "يېزا"
    }
  }
}'
```

### Phrase Search

```bash
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "match_phrase": {
      "content": "ئۇيغۇر تىلى"
    }
  }
}'
```

### Compound Queries

```bash
curl -X GET "localhost:9200/uyghur_docs/_search" -H "Content-Type: application/json" -d'
{
  "query": {
    "bool": {
      "must": [
        { "match": { "content": "ئوقۇتۇش" }},
        { "match": { "content": "مەكتەپ" }}
      ],
      "must_not": [
        { "match": { "content": "كېلىشىم" }}
      ]
    }
  },
  "min_score": 0.5
}'
```

### Highlighting

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

## Advanced Configuration

### Custom Analyzers

Combining standard tokenizer with Uyghur word form filters:

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

### Synonym Configuration

Adding Uyghur synonym support:

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

## Performance Optimization

### Index Settings Optimization

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

## Frequently Asked Questions

### Problem: Search Results Not as Expected

**Solution**: Use the `_analyze` API to check how text is tokenized, ensuring the query uses the correct analyzer.

```bash
curl -X POST "localhost:9200/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_original_analyzer",
  "text": "Your keywords"
}'
```

### Problem: Some Word Form Variations Cannot Be Matched

**Solution**: Consider using `uyghur_split_analyzer` or using both analyzers in a multi-field configuration. (Algorithm and dictionary updates coming soon)

### Problem: Analyzer Performance Issues

**Solution**:
- Optimize index settings
- Consider increasing server resources
- Use caching
- Monitor analyzer performance

---

Through this tutorial, you should be able to fully utilize the features of the Elastic Uyghur Analyzer plugin to provide better support for Uyghur text processing and searching. For more questions, please refer to the official documentation or submit an issue on GitHub. 