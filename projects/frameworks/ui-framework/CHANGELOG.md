# Changelog

All notable changes to the UI Framework will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- Fixed method override errors in `UiFrameworkPlugin`: Changed `setup()` and `shutdown()` from `protected` to `public` visibility to match `JavaPlugin` contract.

## [1.0.0-SNAPSHOT] - Initial Release

### Added
- Platform-agnostic UI framework for managing HUD layouts and menu systems
- `HudLayoutManager` singleton for HUD management
- `MainMenuManager` singleton for main menu coordination
