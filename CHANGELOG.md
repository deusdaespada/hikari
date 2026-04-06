# Changelog

## [Unreleased] - 2026-04-06

### Added
- Parallel source update concurrency setting to speed up updates for large libraries
- Targeted database indexes for mangas and chapters for significantly improved loading times
- Custom WebtoonImageDecoder for efficient handling of extremely tall vertical content
- Initial Koin modules and setup to begin the architectural migration from Injekt
- **"Seamless Stitch" Webtoon Layout**: Implemented 1-pixel bleeding overlap and disabled clipping for perfect vertical rendering without sub-pixel seams
- **Cronet (HTTP/3) Image Pipeline**: Integrated native Chromium network stack to enable QUIC and HTTP/3 support for significantly faster image loading
- **Manual WorkManager Initialization**: Fixed startup crashes by implementing custom Configuration.Provider for background task management

### Changed
- Migrated SourcePreferencesScreen from legacy Fragment bridge to pure Jetpack Compose
- Integrated legacy WebtoonFrame logic directly into WebtoonViewer for a cleaner view hierarchy
- Increased Gradle heap size to 6GB to stabilize builds with large native dependencies (Cronet)

### Fixed
- Resolved `java.lang.IllegalArgumentException` by ensuring Cronet storage directories are created before engine initialization
- Fixed module-level unresolved reference errors for Cronet in core:common
- Fixed WorkManager IllegalStateException on app startup

## [0.0.2] - 2026-04-04

### Added
- Fixed-hour schedule for library updates and backups

### Changed
- Refactored migration strategy and fixed migration crashes


## [0.0.1] - 2026-04-03

### Added

- Hidden manga management feature in Settings > Browse
- Sources list screen for identifying hidden manga across extensions
- Dedicated grid view for managing hidden manga per source
- Multi-selection mode for batch unhiding manga
- Compact grid layout support with title-on-cover aesthetic for hidden entries
- GetHiddenManga interactor for domain-layer state management
- Database queries for efficient retrieval of hidden manga counts and entries

### Changed

- Refactored hidden manga screens to use project-standard UI tokens and spacing
- Standardized source retrieval using SourceManager to ensure consistent title display
- Optimized hidden manga state management using domain-layer Source models
- Integrated localizable plural strings for accurate hidden manga counts
