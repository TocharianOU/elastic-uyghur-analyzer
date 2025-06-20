# Frequently Asked Questions (FAQ)

## General Questions

### What is the Uyghur Analyzer Plugin?
The Uyghur Analyzer Plugin is a specialized Elasticsearch plugin designed to provide advanced Uyghur language text processing and analysis capabilities. It features a unified dictionary system, morphological analyzer, and multiple analysis views, supporting intelligent processing of both modern and classical Uyghur texts.

### What are the main features of the plugin?
- **Unified Dictionary System**: Manages multiple dictionary sources (THU dataset + custom dictionary)
- **Morphological Analysis**: Rule-based intelligent word analysis and restoration
- **Multi-view Analysis**: Original view, Split view, Custom view
- **ES Integration**: Complete Elasticsearch analyzers and token filters
- **Priority Processing**: Custom dictionary priority with multi-layered analysis strategies

### What types of Uyghur texts are supported?
- **Modern Uyghur**: Contemporary written and spoken vocabulary
- **Classical Uyghur**: Historical texts through vowel weakening restoration
- **Professional Terms**: Technology, medical, educational domain vocabulary
- **Neologisms**: Modern terms through custom dictionary support

## Dictionary System

### How does the dictionary system work?
The plugin uses a unified dictionary manager containing two main dictionaries:
1. **Custom Dictionary** (highest priority): User-defined modern vocabulary
2. **THU Dictionary**: Tsinghua University's 21,000+ morphological vocabulary data

The system performs **vocabulary recognition and morphological restoration** rather than simple segmentation.

### What is the format of the THU dictionary?
The THU dictionary contains multiple formats:
```
# Basic forms
ئا، ئائورتا

# Vowel weakening restoration
يېزىش.(يازىش) قا
# Modern form: يېزىش, Original form: يازىش, Suffix: قا

# Complex suffix combinations
ئائىلى.(ئائىلە) سى دىكى لەر نىڭ

# Plural inflections
ئورۇن. لىر(لار) ى نىڭ
```

### How to add custom vocabulary?
1. Edit `src/main/resources/dictionaries/custom_dictionary.txt`
2. Add vocabulary (basic forms and inflected forms):
   ```
   # New vocabulary
   تېخنىكا
   تېخنىكا. لار
   تېخنىكا. نىڭ
   ```
3. Rebuild: `./gradlew clean build`

### What is the priority of the custom dictionary?
The custom dictionary has the **highest priority** (95% confidence), taking precedence over the THU dictionary for matching. This ensures user-defined modern terms are correctly recognized.

## Morphological Analysis

### What is morphological analysis?
Morphological analysis is the process of structural analysis of Uyghur vocabulary, including:
- **Root Identification**: Finding the base form of words
- **Suffix Analysis**: Identifying grammatical suffixes and their functions
- **Vowel Weakening Restoration**: Recovering historical language forms
- **Confidence Assessment**: Providing reliability of analysis results

### What are the analyzer's strategy levels?
The system employs multi-layered analysis strategies:
1. **Custom Dictionary Exact Match** (95% confidence)
2. **THU Dictionary Exact Match** (95% confidence)
3. **THU Dictionary Partial Match** (85% confidence)
4. **Rule-based Analysis** (75% confidence)
5. **Statistical Prediction Analysis** (65% confidence)
6. **Fallback Strategy** (30% confidence)

### How are unknown words handled?
For words not in the dictionary, the system will:
1. Apply morphological rules for analysis
2. Use suffix pattern matching
3. Make predictions based on statistical information
4. Provide best guess results with confidence levels

## Elasticsearch Integration

### How to configure analyzers in ES?
```json
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
  }
}
```

### What's the difference between original and split analyzers?
- **Original Analyzer**: Provides historical forms after vowel weakening restoration, suitable for academic research
- **Split Analyzer**: Maintains modern writing forms, suitable for contemporary text processing

### How to test analysis effects?
```bash
# Test original analyzer
curl -X POST "localhost:9200/_analyze" -d '{
  "analyzer": "uyghur_original",
  "text": "يېزىشقا كىتابلارنىڭ"
}'

# Test split analyzer
curl -X POST "localhost:9200/_analyze" -d '{
  "analyzer": "uyghur_split", 
  "text": "يېزىشقا كىتابلارنىڭ"
}'
```

## Installation and Deployment

### What are the system requirements?
- **Elasticsearch**: 8.7+
- **Java**: 17 or higher
- **Memory**: Recommend at least 512MB for dictionary loading
- **Storage**: About 50MB for plugin and dictionary files

### How to install the plugin?
1. **Build plugin**:
   ```bash
   ./gradlew clean build
   ```

2. **Install to ES**:
   ```bash
   elasticsearch-plugin install file:///path/to/plugin.zip
   ```

3. **Restart Elasticsearch**

### How to verify successful installation?
```bash
# Check plugin list
elasticsearch-plugin list

# Test analyzer
curl -X POST "localhost:9200/_analyze" -d '{
  "analyzer": "uyghur_split",
  "text": "سالام دۇنيا"
}'
```

### How to deploy in Docker environment?
```bash
# Copy plugin to container
docker cp plugin.zip es:/tmp/

# Enter container and install
docker exec -it es elasticsearch-plugin install file:///tmp/plugin.zip

# Restart container
docker restart es
```

## Performance and Optimization

### How is the plugin's performance?
- **Dictionary Loading**: One-time loading at startup, about 2-3 seconds
- **Lookup Speed**: Hash-based, average O(1) time complexity
- **Memory Usage**: About 30-50MB for dictionary indexing
- **Analysis Speed**: Can process thousands of words per second

### How to optimize performance?
- **Memory Configuration**: Allocate sufficient memory for ES
- **Dictionary Optimization**: Add frequently used words to custom dictionary
- **Caching Strategy**: System automatically caches frequently looked up results
- **Batch Processing**: Use bulk API for large text processing

### Considerations for large-scale deployment?
- Install plugin on all ES nodes
- Ensure dictionary files are synchronized across all nodes
- Monitor memory usage
- Regularly update custom dictionary

## Troubleshooting

### Common errors and solutions

#### Plugin installation failure
```
Error: java.nio.file.AccessDeniedException
Solution: Use root privileges or check file permissions
```

#### Dictionary loading failure
```
Error: Dictionary file not found
Solution: Check dictionary file path and permissions
```

#### Analyzer not working
```
Error: Unknown analyzer type
Solution: Confirm plugin is correctly installed and restart ES
```

### How to enable debug logging?
Add to `elasticsearch.yml`:
```yaml
logger.org.tocharian: DEBUG
logger.org.uyghur: DEBUG
```

### How to report issues?
1. Collect error logs
2. Provide ES version and plugin version
3. Describe reproduction steps
4. Create issue in GitHub project

## Development and Extension

### How to add new analysis strategies?
1. Implement `MorphologyAnalysisStrategy` interface
2. Register in `RuleBasedMorphologyAnalyzer`
3. Set appropriate confidence levels
4. Add unit tests

### How to extend the dictionary system?
1. Create new dictionary parser
2. Register in `UnifiedDictionaryManager`
3. Define priority and format rules
4. Update documentation and tests

### How to contribute code?
1. Fork project repository
2. Create feature branch
3. Write code and tests
4. Submit Pull Request
5. Participate in code review

## Data and Licensing

### Dictionary data sources?
- **THU Dataset**: Tsinghua University THUUyMorph project (academic license)
- **Custom Vocabulary**: Community contributions (open source license)
- **Format Standards**: Based on Uyghur morphological analysis conventions

### Plugin license?
This plugin uses Apache 2.0 license, allowing both commercial and non-commercial use.

### How to cite the project?
When citing in academic papers, please reference the THUUyMorph project and this plugin's GitHub repository.

---

For more technical details, please refer to project documentation and source code comments.

