# Quick Start Guide for Elastic Uyghur Analyzer Plugin

This guide provides quick installation and usage instructions for the Elastic Uyghur Analyzer Plugin, helping you implement Uyghur text analysis in Elasticsearch.

## Compatibility

- Elasticsearch 8.7.0
- Java 17 or later

## Installing the Plugin

### Method 1: Direct Installation (for existing Elasticsearch environments)

1. Download the plugin ZIP file:
   ```bash
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v8.7.0/uyghur-analyzer-plugin-8.7.0.zip
   ```

2. Install using the Elasticsearch plugin manager:
   ```bash
   bin/elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin-8.7.0.zip
   ```

3. Restart Elasticsearch:
   ```bash
   # For systemd-based systems
   systemctl restart elasticsearch
   
   # Or for init.d-based systems
   service elasticsearch restart
   ```

### Method 2: Docker Installation

1. Start an Elasticsearch container:
   ```bash
   docker run -d --name es -p 9200:9200 -p 9300:9300 \
     -e "discovery.type=single-node" \
     -e "ELASTIC_PASSWORD=your_password" \
     docker.elastic.co/elasticsearch/elasticsearch:8.7.0
   ```

2. Copy the plugin to the container and install it:
   ```bash
   docker cp uyghur-analyzer-plugin-8.7.0.zip es:/tmp/
   docker exec -it -u root es bash -c "chown elasticsearch:root /tmp/uyghur-analyzer-plugin-8.7.0.zip && \
     /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/uyghur-analyzer-plugin-8.7.0.zip"
   ```

3. Restart the container:
   ```bash
   docker restart es
   ```

## Verifying Installation

Check that the plugin is installed correctly:

```bash
bin/elasticsearch-plugin list
# Or in Docker
docker exec es elasticsearch-plugin list
```

You should see `uyghur-analyzer-plugin` in the list.

## Using the Plugin

### Creating an Index with the Uyghur Analyzer

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

If security is enabled, add authentication:

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

### Testing the Analyzer

```bash
curl -X POST "localhost:9200/uyghur_index/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "uyghur_analyzer",
  "text": "مەن ئۇيغۇرچە سۆزلەيمەن"
}'
```

### Indexing Documents

```bash
curl -X POST "localhost:9200/uyghur_index/_doc" -H "Content-Type: application/json" -d'
{
  "content": "يېزىلاردىكى ئېشىنچا ئەمگەكچىلەرنى"
}'
```

### Searching Documents

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

## Available Analyzers and Filters

### Analyzers

1. `uyghur_original_analyzer` - Preserves original word forms while providing morphological analysis
2. `uyghur_split_analyzer` - Splits words into morphological components

### Token Filters

1. `uyghur_word_original` - Token filter for original word forms
2. `uyghur_word_split` - Token filter for split word forms

### Custom Analyzer Example

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

## Troubleshooting

### Common Issues

1. **Plugin installation fails**:
   - Check if Elasticsearch version is 8.7.0
   - Ensure you're using Java 17
   - Check Elasticsearch logs

2. **Analyzer doesn't work**:
   - Confirm the plugin is installed correctly
   - Check analyzer configuration in index settings
   - Try rebuilding the index

3. **Search results not as expected**:
   - Use the `_analyze` API to check how text is tokenized
   - Adjust queries to match tokenization results

## Additional Resources

- [Build Guide](https://github.com/TocharianOU/elastic-uyghur-analyzer/blob/main/docs/build_guide.md)
- [Usage Guide](https://github.com/TocharianOU/elastic-uyghur-analyzer/blob/main/docs/tutorial.md)
- [FAQ](https://github.com/TocharianOU/elastic-uyghur-analyzer/blob/main/docs/faq.md)

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues) on GitHub.
