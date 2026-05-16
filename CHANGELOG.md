# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [2.4.0] - 2026-05
### Added
- Use the curated TSV affix inventory directly in fallback morphology analysis for suffix and clitic segmentation.
- Add TSV-driven slot-order penalties to Viterbi morphology decoding.
- Add OOV canonical stem restoration for the original analyzer view.
- Extend the Uyghur affix inventory with additional possessive, degree, evidential, conditional, imperative, converb, and verbal-noun forms.

### Changed
- Replace the legacy hardcoded fallback suffix list with suffix and clitic surfaces loaded from the affix inventory, keeping a smaller emergency fallback list if the TSV resource is unavailable.
- Exclude ambiguous warning-marked affix surfaces from blind fallback stripping.

### Fixed
- Tighten THUUyMorph simple-format parsing so multi-token entries do not bleed into the stem group.

## [2.3.0] - 2026-05
### Added
- Add a curated Uyghur affix inventory resource with archiforms, surface variants, grammatical categories, morphotactic slots, harmony classes, and suffix/clitic distinctions.
- Add Java model and loader APIs for querying affixes by id, surface form, category, and slot.
- Add unit coverage for affix inventory loading, harmony-variant indexing, clitic classification, and slot queries.

## [2.2.0] - 2026-05
### Added
- Add a weighted morphology model compiler that derives root, suffix, and suffix-transition costs from the unified dictionary views.
- Add a Viterbi-style morphology segmenter and OOV suffix-boundary fallback for words not covered by exact dictionary entries.
- Document the planned weighted FST/Viterbi and OOV boundary prediction architecture.

### Changed
- Route rule-based fallback analysis through weighted model decoding before using legacy suffix heuristics.
- Report weighted model statistics in analyzer diagnostics.

## [2.1.0] - 2026-05
### Added
- Add recursive dictionary-prefix expansion for open Uyghur suffix chains, so known forms such as `ئىشلىگەن` can be expanded before additional suffixes such as `نىڭ` are appended.
- Link the standalone `es-ug-benchmark` repository as the reproducible evaluation framework for analyzer quality and retrieval tests.

### Fixed
- Preserve the selected original/split dictionary view when recursively expanding dictionary prefixes.
- Update release artifact naming and documentation to `v2.1.0` for both Elasticsearch 8.x and 9.x builds.

## [2.0.0-es8] - 2026-05
### Added
- Unified dictionary system based on `thuuy_morph_raw.txt`.
- Rule-based Uyghur morphology analyzer with original and split dictionary views.
- Elasticsearch 8.x compatible stable analysis plugin packaging, built against the 8.7.0 stable plugin API.

## [2.0.0-es9] - 2026-05
### Added
- Elasticsearch 9.x compatible stable analysis plugin packaging, built against the 9.4.0 stable plugin API.

## [1.0.0] - 2024-07-31
### Added
- Initial release with support for two analyzers and two token filters for Uyghur text analysis.
