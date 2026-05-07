# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
### Fixed
- Ensure `uyghur_original_analyzer` and `uyghur_split_analyzer` pass their selected dictionary view into morphology analysis.
- Convert the morphology analyzer smoke test into executable JUnit assertions.
- Correct stale documentation references for dictionary files and repository URLs.
- Normalize the ES 8 release tag and ZIP artifact name to `v2.0.0` / `uyghur-analyzer-plugin-2.0.0-es8.zip`.
- Verify the ES 8 stable plugin artifact installs and analyzes successfully on Elasticsearch 8.19.15.

## [2.0.0-es8] - 2026-05
### Added
- Unified dictionary system based on `thuuy_morph_raw.txt`.
- Rule-based Uyghur morphology analyzer with original and split dictionary views.
- Elasticsearch 8.x compatible stable analysis plugin packaging, built against the 8.7.0 stable plugin API.

## [1.0.0] - 2024-07-31
### Added
- Initial release with support for two analyzers and two token filters for Uyghur text analysis.
