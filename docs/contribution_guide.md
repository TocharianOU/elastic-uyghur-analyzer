# Contribution Guide

Thank you for your interest in contributing to the Elasticsearch Uyghur Analyzer Plugin! This guide will help you get started with contributing to the project.

## Getting Started

### Prerequisites

- JDK 17 or higher for Elasticsearch 8.x builds
- JDK 21 or higher for Elasticsearch 9.x builds
- Git
- Basic understanding of Elasticsearch plugins
- Familiarity with Java and Gradle

### Development Environment Setup

1. **Fork and Clone**:
   ```bash
   git clone https://github.com/your-username/elastic-uyghur-analyzer.git
   cd elastic-uyghur-analyzer
   ```

2. **Build the Project**:
   ```bash
   ./gradlew clean check
   ```

3. **Run Tests**:
   ```bash
   ./gradlew test
   ```

## Ways to Contribute

### 1. Dictionary Enhancement

The plugin uses several dictionary files located in `src/main/resources/dictionaries/`:
- `custom_dictionary.txt`: User-defined vocabulary (highest priority)
- `thuuy_morph_raw.txt`: THU morphological dataset

**How to contribute**:
- Add new vocabulary entries to `custom_dictionary.txt`
- Follow the format: `word. root suffix1 suffix2`
- Test your additions with the morphological analyzer

### 2. Code Improvements

#### Areas for Enhancement:
- **Performance optimization** in `RuleBasedMorphologyAnalyzer`
- **New analysis strategies** with different confidence levels
- **Bug fixes** in morphological analysis logic
- **New token filters** for specific use cases

#### Code Standards:
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Include unit tests for new features
- Maintain backward compatibility

### 3. Testing and Quality Assurance

- Test with supported Elasticsearch 8.x and 9.x versions
- Add test cases for edge cases in Uyghur morphology
- Performance testing with large text corpora
- Integration testing with real-world scenarios

### 4. Documentation

- Update README files for new features
- Add examples in `docs/` directory
- Improve code comments and JavaDoc
- Create tutorials for specific use cases

## Development Workflow

### 1. Issue Creation

Before starting work:
- Check existing issues on GitHub
- Create a new issue describing the problem or feature
- Discuss the approach with maintainers

### 2. Branch Strategy

```bash
# Create a feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-description
```

### 3. Code Changes

- Make focused commits with clear messages
- Follow conventional commit format:
  ```
  feat: add new morphological analysis strategy
  fix: resolve dictionary parsing issue
  docs: update installation guide
  ```

### 4. Testing

Before submitting:
```bash
# Run all tests
./gradlew test

# Build the plugin
./gradlew clean check

# Test with Elasticsearch (optional)
# Follow the build guide to test in Docker
```

### 5. Pull Request

1. **Push your branch**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request**:
   - Use a clear title and description
   - Reference related issues
   - Include testing instructions
   - Add screenshots if applicable

3. **PR Template**:
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Documentation update
   - [ ] Performance improvement

   ## Testing
   - [ ] Tests pass locally
   - [ ] Added new tests for changes
   - [ ] Tested with Elasticsearch

   ## Related Issues
   Fixes #issue_number
   ```

## Code Review Process

1. **Automated Checks**: All PRs must pass CI/CD checks
2. **Peer Review**: At least one maintainer review required
3. **Testing**: Changes must include appropriate tests
4. **Documentation**: Update docs for user-facing changes

## Specific Contribution Areas

### Dictionary Contributions

1. **Custom Dictionary Entries**:
   - Modern technical terms
   - Domain-specific vocabulary
   - Regional variations

2. **Format Requirements**:
   ```
   # Format: word. root suffix1 suffix2
   تېخنىكا. تېخنىكا
   كومپيۇتېر. كومپيۇتېر
   ```

### Algorithm Improvements

1. **Morphological Analysis**:
   - Improve confidence scoring
   - Add new analysis strategies
   - Optimize performance

2. **Pattern Recognition**:
   - Enhance suffix detection
   - Improve vowel harmony rules
   - Add new morphological patterns

## Community Guidelines

- **Be respectful** in all interactions
- **Be patient** with review processes
- **Be collaborative** in discussions
- **Be thorough** in testing and documentation

## Getting Help

- **GitHub Issues**: For bugs and feature requests
- **Discussions**: For questions and general discussion
- **Email**: Contact maintainers directly for sensitive issues

## Recognition

Contributors will be recognized in:
- README contributors section
- Release notes for significant contributions
- GitHub contributor statistics

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

---

Thank you for contributing to the Elasticsearch Uyghur Analyzer Plugin!
