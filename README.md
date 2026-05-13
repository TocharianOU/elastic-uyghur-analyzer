# Elasticsearch Uyghur Analyzer Plugin

[![Build](https://github.com/TocharianOU/elastic-uyghur-analyzer/actions/workflows/build.yml/badge.svg)](https://github.com/TocharianOU/elastic-uyghur-analyzer/actions/workflows/build.yml) [![English](https://img.shields.io/badge/Language-English-blue)](README.md) [![中文](https://img.shields.io/badge/语言-中文-red)](README_zh.md) [![Downloads](https://img.shields.io/github/downloads/TocharianOU/elastic-uyghur-analyzer/total)](https://github.com/TocharianOU/elastic-uyghur-analyzer/releases) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/TocharianOU/elastic-uyghur-analyzer)

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

- Elasticsearch 8.x or 9.x
- Java 17 or higher for Elasticsearch 8.x builds
- Java 21 or higher for Elasticsearch 9.x builds

Use the matching plugin artifact for your Elasticsearch major version:

- `uyghur-analyzer-plugin-2.3.0-es8.zip` for Elasticsearch 8.x
- `uyghur-analyzer-plugin-2.3.0-es9.zip` for Elasticsearch 9.x

## Version Compatibility

| Plugin Version | Elasticsearch Version | Release Date | Key Features |
|---------------|----------------------|--------------|--------------|
| 2.3.0-es8 | Elasticsearch 8.x, built against 8.7.0 stable plugin API; smoke-tested on 8.7.0 and 8.19.15 | 2026-05 | Structured affix inventory, morphotactic slots, suffix/clitic metadata |
| 2.3.0-es9 | Elasticsearch 9.x, built against 9.4.0 stable plugin API; smoke-tested on 9.4.0 | 2026-05 | Structured affix inventory, morphotactic slots, suffix/clitic metadata |

## Installation

### Option 1: Download from GitHub Release (Recommended)

1. Download the latest plugin:
   ```bash
   # Elasticsearch 8.x
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.3.0/uyghur-analyzer-plugin-2.3.0-es8.zip

   # Elasticsearch 9.x
   wget https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.3.0/uyghur-analyzer-plugin-2.3.0-es9.zip
   ```

2. Install to Elasticsearch:
   ```bash
   # Install the matching plugin from a local file
   elasticsearch-plugin install file:///path/to/uyghur-analyzer-plugin-2.3.0-es8.zip

   # Or install directly from URL for Elasticsearch 8.x
   elasticsearch-plugin install https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.3.0/uyghur-analyzer-plugin-2.3.0-es8.zip

   # Or install directly from URL for Elasticsearch 9.x
   elasticsearch-plugin install https://github.com/TocharianOU/elastic-uyghur-analyzer/releases/download/v2.3.0/uyghur-analyzer-plugin-2.3.0-es9.zip
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
   ./gradlew clean check
   ```

2. Install the built plugin:
   ```bash
   elasticsearch-plugin install file:///path/to/build/distributions/uyghur-analyzer-plugin-2.3.0-es8.zip
   ```

## Analyzer Behavior

The plugin provides two analyzers for different search strategies:

- `uyghur_original_analyzer`: restores historical/root forms where the THUUyMorph entry records vowel weakening.
- `uyghur_split_analyzer`: preserves modern written forms while splitting suffixes.

Examples:

| Input | `uyghur_original_analyzer` | `uyghur_split_analyzer` |
|-------|----------------------------|-------------------------|
| `ئائىلىدىكى` | `ئائىلە + دىكى` | `ئائىلى + دىكى` |
| `يېزىش` | `ياز + ىش` | `يېز + ىش` |

## Evaluation Benchmark

The independent ES-UG Benchmark project is available at [TocharianOU/es-ug-benchmark](https://github.com/TocharianOU/es-ug-benchmark). It provides the full evaluation framework, including corpus parsing, Docker-based Elasticsearch setup, segmentation quality tests, information-retrieval metrics, and a Streamlit UI for comparing analyzer behavior. Detailed benchmark documentation and results are maintained in that repository.

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

## Verification

After installing the plugin and restarting Elasticsearch, verify both analyzers with `_analyze`:

```bash
curl -k -X POST "https://localhost:9200/_analyze" \
  -u elastic:your_password \
  -H "Content-Type: application/json" -d'{
  "analyzer": "uyghur_original_analyzer",
  "text": "ئائىلىدىكى"
}'
```

Then repeat the request with `uyghur_split_analyzer`. The two analyzers should return different root forms for words such as `ئائىلىدىكى` and `يېزىش`.

## Building from Source

For development or customization:

```bash
git clone https://github.com/TocharianOU/elastic-uyghur-analyzer.git
cd elastic-uyghur-analyzer
./gradlew clean check
```

The built plugin will be available at `build/distributions/uyghur-analyzer-plugin-2.3.0-es8.zip`

To build the Elasticsearch 9.x artifact, use Java 21 or higher and Gradle 8.14:

```bash
curl -fsSL https://services.gradle.org/distributions/gradle-8.14-bin.zip -o /tmp/gradle-8.14-bin.zip
unzip -q /tmp/gradle-8.14-bin.zip -d /tmp
/tmp/gradle-8.14/bin/gradle clean check -PesMajor=9 -PelasticsearchVersion=9.4.0 -PluceneVersion=10.4.0
```

The Elasticsearch 9.x plugin will be available at `build/distributions/uyghur-analyzer-plugin-2.3.0-es9.zip`.

## Documentation

- [Build Guide](docs/build_guide.md)
- [Getting Started](docs/getting_started.md)
- [Dictionary Configuration](docs/dictionary_explanation.md)
- [FAQ](docs/faq.md)

## Data Sources

This plugin uses the THUUyMorph dataset developed by Tsinghua University NLP Lab.
- Website: http://thuuymorph.thunlp.org/
- Citation: THUUyMorph - A Uyghur Morphological Analysis Corpus (CCL/NLP-NABD 2017)

The bundled `thuuy_morph_raw.txt` dictionary is third-party data derived from THUUyMorph resources. Please preserve the attribution notice and cite the THUUyMorph paper if you use the bundled dictionary data in research or derivative work.

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## Contributing

See [Contribution Guide](docs/contribution_guide.md) for development setup and guidelines.

## Support

- GitHub Issues: [Report issues](https://github.com/TocharianOU/elastic-uyghur-analyzer/issues)
- Documentation: Check the `docs/` directory for detailed guides
