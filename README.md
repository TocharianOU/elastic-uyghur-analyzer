# Elasticsearch Uyghur Analyzer Plugin

[![English](https://img.shields.io/badge/Language-English-blue)](README.md) [![中文](https://img.shields.io/badge/语言-中文-red)](README_zh.md) [![Downloads](https://img.shields.io/github/downloads/TocharianOU/elastic-uyghur-analyzer/total)](https://github.com/TocharianOU/elastic-uyghur-analyzer/releases) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/TocharianOU/elastic-uyghur-analyzer)

An Elasticsearch plugin that provides Uyghur language text analysis and tokenization capabilities.

## Overview

This plugin adds Uyghur language support to Elasticsearch through custom analyzers and token filters. It handles Uyghur morphological analysis and provides two tokenization approaches for different use cases.

## Features

- **Two Analyzer Types**:
  - `uyghur_original_analyzer`: Performs morphological restoration by reversing vowel weakening
  - `uyghur_split_analyzer`: Direct segmentation while preserving modern writing forms
- **Morphological Analysis**: Rule-based analyzer with multi-level confidence scoring
- **Custom Dictionary Support**: Allows adding domain-specific vocabulary
- **THU Dictionary Integration**: Based on THUUyMorph dataset from Tsinghua University

## Requirements

- Elasticsearch 8.7.0 or higher
- Java 17 or higher

## Version Compatibility

| Plugin Version | Elasticsearch Version | Release Date | Key Features |
|---------------|----------------------|--------------|--------------|
| v2.0-es8.7+   | 8.7.0+              | 2024-06     | Unified dictionary system, morphological analyzer |

## Installation

### Option 1: Download from GitHub Release (Recommended)

1. Download the latest plugin:
   ```bash
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.0-es8.7%2B/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   ```

2. Install to Elasticsearch:
   ```bash
   # Install plugin from local file
   elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   
   # Or install directly from URL
   elasticsearch-plugin install https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.0-es8.7%2B/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   ```

3. Restart Elasticsearch and verify installation:
   ```bash
   # Restart Elasticsearch service
   sudo systemctl restart elasticsearch
   
   # Verify installation
   elasticsearch-plugin list
   ```

### Option 2: Build from Source

1. Clone and build:
   ```bash
   git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
   cd elastic-uyghur-analyzer
   ./gradlew clean build
   ```

2. Install the built plugin:
   ```bash
   elasticsearch-plugin install file:///path/to/build/distributions/uyghur-analyzer-plugin-v2.0-es8.7+.zip
   ```

## Usage

### Create Index with Uyghur Analyzer

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

### Test Tokenization

```bash
curl -k -X POST "https://localhost:9200/uyghur_index/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'{
  "analyzer": "uyghur_analyzer",
  "text": "ئورۇنلاشتۇرۇشلارنى"
}'
```

Expected output:
```json
{
  "tokens": [
    {"token": "ئورۇنلاشتۇرۇشلار", "start_offset": 0, "end_offset": 16, "type": "word", "position": 0},
    {"token": "نى", "start_offset": 17, "end_offset": 19, "type": "word", "position": 1}
  ]
}
```

## Configuration

The plugin uses several dictionary files:
- `custom_dictionary.txt`: User-defined vocabulary (highest priority)
- `thuuy_morph_raw.txt`: THU morphological dataset

## Building from Source

For development or customization:

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
./gradlew clean build
```

The built plugin will be available at `build/distributions/uyghur-analyzer-plugin-v2.0-es8.7+.zip`

## Documentation

- [Build Guide](docs/build_guide.md)
- [Getting Started](docs/getting_started.md)
- [Dictionary Configuration](docs/dictionary_explanation.md)
- [FAQ](docs/faq.md)

## Data Sources

This plugin uses the THUUyMorph dataset developed by Tsinghua University NLP Lab.
- Website: http://thuuymorph.thunlp.org/
- Citation: THUUyMorph - A Uyghur Morphological Analysis Corpus (CCL/NLP-NABD 2017)

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## Contributing

See [Contribution Guide](docs/contribution_guide.md) for development setup and guidelines.

## Support

- GitHub Issues: [Report issues](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)
- Documentation: Check the `docs/` directory for detailed guides
