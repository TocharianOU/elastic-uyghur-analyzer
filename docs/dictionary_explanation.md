# Dictionary Explanation

This document explains the dictionary system used in the Elasticsearch Uyghur Analyzer Plugin, including the unified dictionary architecture and morphological analysis capabilities.

## Overview

The plugin uses a **Unified Dictionary System** that manages multiple dictionary sources and provides vocabulary recognition and morphological restoration functionality. This system includes predefined morphological vocabulary databases and user-defined custom vocabulary.

## Dictionary Architecture

### Unified Dictionary Manager

The `UnifiedDictionaryManager` class serves as the central component that:
- Loads and parses multiple dictionary sources
- Provides different analysis views (Original, Split, Custom)
- Manages priority-based word lookup
- Supports recognition and restoration of complete word forms

### Dictionary Files Location

All dictionary files are located in `src/main/resources/dictionaries/`:

```
src/main/resources/dictionaries/
├── custom_dictionary.txt      # User-defined vocabulary (highest priority)
└── thuuy_morph_raw.txt        # THU morphological vocabulary database
```

## Dictionary Format Analysis

### THU Dictionary Format Patterns

Through analysis of actual dictionary content, the following format patterns are identified:

#### 1. Basic Word Forms
```
ئا
ئائورتا
ئائىت
```
- Individual words without morphological variations

#### 2. Vowel Weakening Restoration Format
```
ئائىلى.(ئائىلە) دىكى
ئورۇنلىش.(ئورۇنلاش) ىش
```
- Format: `modern_form.(original_form) suffix_combination`
- Parentheses contain original form before vowel weakening
- Dot followed by specific inflected form of the word

#### 3. Simple Suffix Format
```
ئائىلەر. گە
ئورۇن. نىڭ
```
- Format: `root. suffix`
- Represents specific grammatical form of the word

#### 4. Complex Suffix Combinations
```
ئائىلى.(ئائىلە) سى دىكى لەر نىڭ
ئورۇنلىش.(ئورۇنلاش) ىش قا
```
- Format: `modern_form.(original_form) suffix1 suffix2 suffix3`
- Multiple suffix combinations

#### 5. Plural Inflection Markers
```
ئورۇن. لىر(لار) ى
ئوقۇغۇچى. لىر(لار) ى دىن
```
- `لىر(لار)` indicates plural form variants
- Appropriate form selected based on vowel harmony

## Dictionary Types Detailed

### 1. Custom Dictionary (`custom_dictionary.txt`)

**Purpose**: User-defined modern vocabulary with highest recognition priority.

**Format Features**:
```
# Basic forms
كومپيۇتېر
ئىنتېرنەت

# Inflected forms
كومپيۇتېر. لار
تاكسى. دا
دوختۇر. نىڭ
```

**Content Categories**:
- **Modern Technology Terms**: `كومپيۇتېر`, `ئىنتېرنەت`, `پروگرامما`
- **Daily Life Vocabulary**: `تاكسى`, `ئوتوبۇس`, `مېترو`
- **Professional Terms**: `دوختۇر`, `ئاسپىرانت`, `دىپلوم`

### 2. THU Morphological Vocabulary Database (`thuuy_morph_raw.txt`)

**Purpose**: Comprehensive Uyghur morphological vocabulary dataset from Tsinghua University.

**Data Scale**: Approximately 21,000 word forms

**Format Analysis**:
```
# Example 1: Vowel weakening restoration
يېزىش.(يازىش) قا
# Modern form: يېزىش
# Original form: يازىش  
# Suffix: قا

# Example 2: Complex suffix combination
ئائىلى.(ئائىلە) سى دىكى لەر نىڭ
# Modern form: ئائىلى
# Original form: ئائىلە
# Suffix combination: سى + دىكى + لەر + نىڭ

# Example 3: Plural inflection
ئورۇن. لىر(لار) ى نىڭ
# Root: ئورۇن
# Plural marker: لىر/لار (based on vowel harmony)
# Suffixes: ى + نىڭ
```

## Dictionary Working Principles

### Vocabulary Recognition Process

The system performs **vocabulary recognition and morphological restoration** rather than segmentation:

1. **Complete Matching**: Check if input word exists in dictionary
2. **Morphological Restoration**: If found, return base form and grammatical information
3. **Priority Processing**: Custom dictionary > THU exact match > THU partial match

### Practical Usage Example

**Input Word**: `ئورۇنلاشتۇرۇشلارنى`

**Lookup Process**:
1. Custom dictionary lookup: Not found
2. THU complete match: Complete word not found
3. THU partial match: Found related entries
   - `ئورۇنلاش. قان لار` 
   - `ئورۇنلىش.(ئورۇنلاش) ىش`
4. **Analysis Result**: Identified as inflected form of compound word

## Analysis Views Explanation

### 1. Original View
- **Function**: Provides original form after vowel weakening restoration
- **Example**: `يېزىش` → `يازىش`
- **Usage**: Academic research, historical linguistics analysis

### 2. Split View  
- **Function**: Maintains modern writing forms
- **Example**: `يېزىش` → `يېزىش`
- **Usage**: Modern text processing, search applications

### 3. Custom View
- **Function**: Prioritizes recognition of user-defined modern vocabulary
- **Example**: `تاكسى` → Direct recognition without morphological analysis
- **Usage**: Domain-specific applications, specialized terminology processing

## Dictionary Management Operations

### Adding Custom Vocabulary

1. **Edit custom dictionary**:
   ```bash
   vim src/main/resources/dictionaries/custom_dictionary.txt
   ```

2. **Add new vocabulary**:
   ```
   # Basic form
   new_word
   
   # Common inflected forms
   new_word. لار
   new_word. نىڭ
   new_word. غا
   ```

3. **Rebuild**:
   ```bash
   ./gradlew clean build
   ```

### Dictionary Statistics

- **Custom Dictionary**: ~70 word forms (modern vocabulary)
- **THU Vocabulary Database**: ~21,000 word forms (comprehensive coverage)
- **Total Recognition Capability**: Covers classical and modern Uyghur vocabulary

## Application in Elasticsearch

### Analyzer Configuration

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

### Vocabulary Analysis Examples

```bash
# Original form analysis
curl -X POST "localhost:9200/_analyze" -d '{
  "analyzer": "uyghur_original",
  "text": "يېزىشقا كىتابلارنىڭ"
}'

# Modern form analysis  
curl -X POST "localhost:9200/_analyze" -d '{
  "analyzer": "uyghur_split",
  "text": "يېزىشقا كىتابلارنىڭ"
}'
```

## Technical Implementation Details

### Dictionary Loading Process

1. **File Reading**: Read all dictionary files
2. **Format Parsing**:
   - Identify original forms in parentheses
   - Parse suffix combinations after dots
   - Handle plural inflection markers `لىر(لار)`
3. **Index Construction**: Create efficient lookup tables
4. **View Generation**: Generate different views as needed

### Parsing Algorithm

```java
// THU format parsing example
if (line.contains(".(") && line.contains(")")) {
    // Format: modern_form.(original_form) suffixes
    String modern = extractModernForm(line);
    String original = extractOriginalForm(line);
    String suffixes = extractSuffixes(line);
} else if (line.contains(". ")) {
    // Format: root. suffixes
    String root = extractRoot(line);
    String suffixes = extractSuffixes(line);
} else {
    // Basic form
    String word = line.trim();
}
```

## Data Sources

- **THU Dataset**: THUUyMorph project from Tsinghua University NLP Lab
- **Custom Vocabulary**: Community-contributed modern Uyghur vocabulary
- **Format Standards**: Based on Uyghur morphological analysis conventions

For more information, visit: http://thuuymorph.thunlp.org/

---

This dictionary system provides accurate morphological support for Uyghur text processing through recognition and restoration of complete word forms.
