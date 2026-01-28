# Changelog

All notable changes to the UI Framework will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **UI Hot Reload System** for development mode (SF-10b specification)
  - `UIHotReloadService`: File watcher for HYUIML files with automatic content caching
  - `DevModeConfig`: Configuration record for development features
  - `/uireload` command for manual file reload
  - `UnifiedUIManager.createUISupplier()` for hot-reloadable content suppliers
  - Player UI tracking for targeted refresh on file changes
  - Production safety: Auto-disabled when `ARGONATH_ENV=production`

### Fixed
- Fixed method override errors in `UiFrameworkPlugin`: Changed `setup()` and `shutdown()` from `protected` to `public` visibility to match `JavaPlugin` contract.

## [1.0.0-SNAPSHOT] - Initial Release

### Added
- Platform-agnostic UI framework for managing HUD layouts and menu systems
- `HudLayoutManager` singleton for HUD management
- `MainMenuManager` singleton for main menu coordination
