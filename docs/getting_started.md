# Elasticsearch Uyghur Analyzer Plugin Quick Start Guide

This guide provides quick installation and usage instructions for the Elasticsearch Uyghur Analyzer Plugin, helping you implement advanced Uyghur text analysis and morphological processing in Elasticsearch.

## Plugin Features

- **Unified Dictionary System**: Integrates THU morphological dataset and custom dictionary
- **Intelligent Morphological Analysis**: Multi-layered analysis strategies supporting unknown word processing
- **Dual-view Analysis**: Original form restoration and modern form segmentation
- **High-performance Processing**: Hash-based fast vocabulary lookup
- **Extensible Architecture**: Supports custom vocabulary and analysis strategies

## Compatibility

- **Elasticsearch**: 8.x or 9.x
- **Java**: 17 or higher for Elasticsearch 8.x builds; Java 21 or higher for Elasticsearch 9.x builds
- **Memory**: Recommend at least 64MB for dictionary loading
- **Storage**: About 50MB for plugin and dictionary files

Use `uyghur-analyzer-plugin-2.0.0-es8.zip` for Elasticsearch 8.x and `uyghur-analyzer-plugin-2.0.0-es9.zip` for Elasticsearch 9.x.

## Building and Installing the Plugin

### Building from Source

1. **Clone the project**:
   ```bash
   git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
   cd elastic-uyghur-analyzer
   ```

2. **Build the plugin**:
   ```bash
   ./gradlew clean check
   ```
   
   After building, the plugin file is located in the `build/distributions/` directory.

### Method 1: Direct Installation (for existing Elasticsearch environments)

1. **Install the plugin**:
   ```bash
   elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin.zip
   ```

2. **Restart Elasticsearch**:
   ```bash
   # For systemd-based systems
   systemctl restart elasticsearch
   
   # Or for init.d-based systems
   service elasticsearch restart
   ```

### Method 2: Docker Installation

1. **Start an Elasticsearch container**:
   ```bash
   docker run -d --name es -p 9200:9200 -p 9300:9300 \
     -e "discovery.type=single-node" \
     -e "ELASTIC_PASSWORD=your_password" \
     docker.elastic.co/elasticsearch/elasticsearch:8.11.0
   ```

2. **Copy the plugin to the container and install it**:
   ```bash
   docker cp build/distributions/uyghur-analyzer-plugin.zip es:/tmp/
   docker exec -it -u root es elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin.zip
   ```

3. **Restart the container**:
   ```bash
   docker restart es
   ```

## Verifying Installation

Check that the plugin is installed correctly:

```bash
elasticsearch-plugin list
# Or in Docker
docker exec es elasticsearch-plugin list
```

You should see `uyghur-analyzer-plugin` in the list.

## Basic Usage

### Creating an Index with Uyghur Analyzers

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

### Testing Analyzer Effects

**Test Original Analyzer** (vowel weakening restoration):
```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_original",
  "text": "يېزىشقا كىتابلارنىڭ"
}'
```

**Test Split Analyzer** (modern form preservation):
```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split",
  "text": "يېزىشقا كىتابلارنىڭ"
}'
```

**Test Morphological Analysis**:
```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split",
  "text": "ئورۇنلاشتۇرۇشلارنى تاكسىدا"
}'
```

### Indexing Uyghur Documents

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

### Searching Uyghur Documents

**Basic Search**:
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

**Multi-field Search**:
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

**Morphological Matching Search**:
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

## Analyzer Details

### Available Analyzers

1. **`uyghur_original_analyzer`**
   - **Function**: Provides historical forms with vowel weakening restoration
   - **Use Cases**: Academic research, historical text analysis
   - **Example**: `يېزىش` → `يازىش`

2. **`uyghur_split_analyzer`**
   - **Function**: Intelligent morphology-based segmentation preserving modern forms
   - **Use Cases**: Modern text search, content analysis
   - **Example**: `تاكسىدا` → `تاكسى` + `دا`

### Token Filters

1. **`uyghur_word_original`** - Token filter for original forms
2. **`uyghur_word_split`** - Token filter for split forms

### Custom Analyzer Configuration

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

## Advanced Features

### Custom Dictionary Management

1. **View current custom dictionary**:
   ```bash
   # Dictionary file is located inside the plugin
   # src/main/resources/dictionaries/custom_dictionary.txt
   ```

2. **Add custom vocabulary** (requires plugin rebuild):
   ```
   # Add to custom_dictionary.txt
   ئەپ
   ئەپ. لار
   ۋېبسايت
   ۋېبسايت. تا
   ```

### Morphological Analysis Testing

Use the built-in tester to verify morphological analysis effects:

```bash
# Compile and run interactive tester
./gradlew compileJava
java -cp build/classes/java/test:build/classes/java/main:build/resources/main org.tocharian.uyghur.morphology.InteractiveMorphologyTester
```

## Troubleshooting

### Common Issues

1. **Plugin installation fails**:
   ```
   Error: java.nio.file.AccessDeniedException
   Solution: Use root privileges or check file permissions
   ```

2. **Dictionary loading fails**:
   ```
   Error: Dictionary file not found
   Solution: Check if dictionary files are properly packaged in plugin
   ```

3. **Analyzer doesn't work**:
   ```
   Error: Unknown analyzer type
   Solution: Confirm plugin is correctly installed and restart Elasticsearch
   ```

4. **Out of memory**:
   ```
   Error: OutOfMemoryError during dictionary loading
   Solution: Increase Elasticsearch heap memory settings
   ```

### Debugging and Logging

Enable verbose logging:

```yaml
# Add to elasticsearch.yml
logger.org.tocharian: DEBUG
logger.org.tocharian.uyghur: DEBUG
```

View analysis process:
```bash
curl -k -X POST "https://localhost:9200/_analyze?explain=true" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_split",
  "text": "تېخنىكىلىق"
}'
```

## Performance Optimization

### Memory Configuration

```yaml
# elasticsearch.yml
# Allocate sufficient memory for dictionary loading
-Xms2g
-Xmx2g
```

### Index Optimization

```bash
# Create high-performance index configuration
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

## Practical Application Examples

### News Search System

```bash
# Create news index
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

### Academic Literature Search

```bash
# Create academic literature index
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

## Additional Resources

- [Dictionary Explanation](dictionary_explanation.md) - Detailed dictionary system documentation
- [Build Guide](build_guide.md) - Complete build and development guide
- [Tutorial](tutorial.md) - In-depth usage tutorial
- [FAQ](faq.md) - Detailed FAQ and troubleshooting

## Technical Support

- **GitHub Issues**: [Submit Issues](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)
- **Documentation**: Check project documentation for more technical details
- **Community**: Participate in open source community discussions and contributions

---

Start using the Elasticsearch Uyghur Analyzer Plugin and experience advanced Uyghur text processing capabilities!
