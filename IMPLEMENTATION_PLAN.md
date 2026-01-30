# Implementation Plan: UI Framework

**Module**: `05-framework-ui`  
**Generated**: 2026-01-30  
**Architect**: HytaleArchitect  
**Specification Coverage**: SF-ARCHITECTURE-010, SF-ARCHITECTURE-010b, VDD-MISC-001 through VDD-MISC-029

---

## Executive Summary

The UI Framework provides platform-agnostic UI components with HyUIML-based templates and Java builders. Current state is approximately **60% complete** with significant infrastructure in place but **critical gaps** in actual HyUI rendering integration. The module has **1 architectural violation** (Hytale imports in library code), **6 return null statements** (partial violations - some justified), and **no accessor v2.0.0 migration issues** as it correctly uses the new `UIContext`/`UIUpdateData`/`HudLayoutData` types. The primary blocker is the lack of functional adapter layer integration with HyUI's `PageBuilder`/`HudBuilder` APIs.

---

## Critical Issues Found

### Violations (MUST FIX)

| ID | Location | Type | Description | Severity |
|----|----------|------|-------------|----------|
| V-001 | [UiFrameworkPlugin.java#L5-L6](src/main/java/com/argonathsystems/framework/ui/UiFrameworkPlugin.java#L5-L6) | Hytale Import Leak | Direct `com.hypixel.hytale.server.core.plugin.*` imports in framework code - Frameworks are NOT plugins | 🔴 CRITICAL |
| V-002 | [DialoguePageBuilder.java#L220](src/main/java/com/argonathsystems/framework/ui/dialogue/DialoguePageBuilder.java#L220) | Reimplementing Framework | Custom template processor instead of using HyUI's `TemplateProcessor` | 🔴 HIGH |
| V-003 | [MenuTabHandler.java#L31](src/main/java/com/argonathsystems/framework/ui/menu/MenuTabHandler.java#L31) | Object Type | `Object data` parameter should use `DataValue` per accessor v2.0.0 | 🟡 MEDIUM |

### Technical Debt

| ID | Location | Type | Description | Priority |
|----|----------|------|-------------|----------|
| TD-001 | [UIHotReloadService.java#L371](src/main/java/com/argonathsystems/framework/ui/dev/UIHotReloadService.java#L371) | Silent return null | `return null` in error path - acceptable with logging but should use Optional | 🟡 MEDIUM |
| TD-002 | [MenuTab.java#L63-L83](src/main/java/com/argonathsystems/framework/ui/menu/MenuTab.java#L63-L83) | Return null pattern | Lookup methods return null instead of Optional<MenuTab> | 🟡 MEDIUM |
| TD-003 | HyUIML Templates | CSS Violations | ~35% syntax validity - many unsupported CSS properties remain | 🔴 HIGH |
| TD-004 | All Builders | No HyUI Integration | Builders generate HTML strings but don't render via HyUI APIs | 🔴 CRITICAL |
| TD-005 | [pom.xml#L27-L31](pom.xml#L27-L31) | Hytale SDK Dependency | HytaleServer-parent dependency in framework POM - should be provided scope only for plugin entry | 🟡 MEDIUM |

### TODO/FIXME/STUB Inventory

| Location | Type | Description | Action Required |
|----------|------|-------------|-----------------|
| No explicit TODOs found | - | Code is documented with ⚠️ warnings in Javadoc | Address warnings in DialoguePageBuilder |

---

## Requirements Traceability

### Specification Coverage

| Spec ID | Requirement | Status | Implementation Location | Notes |
|---------|-------------|--------|-------------------------|-------|
| SF-ARCHITECTURE-010 | UI-L1-001: Unified Dashboard | 🚧 20% | UnifiedUIManager | App registration works, dashboard XAML not created |
| SF-ARCHITECTURE-010 | UI-L1-002: Dynamic Theming | 🚧 30% | LotrTheme.java | Constants defined, no ThemeDefinition JSON loader |
| SF-ARCHITECTURE-010 | UI-L1-003: HUD Start Button | ❌ 0% | - | Not implemented |
| SF-ARCHITECTURE-010 | UI-L1-004: Customizable HUD | ✅ 80% | HudLayoutManager, HudLayoutConfig | Layout persistence works |
| SF-ARCHITECTURE-010 | UI-L2-001: App Registration | ✅ 100% | UnifiedUIManager.registerApp() | Complete |
| SF-ARCHITECTURE-010 | UI-L2-002: Dashboard Navigation | 🚧 40% | MainMenuManager | Logic exists, no XAML/HYUIML |
| SF-ARCHITECTURE-010 | UI-L2-003: Theme Resolution | ❌ 0% | - | No theme resolver implemented |
| SF-ARCHITECTURE-010 | UI-L2-004: HUD Edit Mode | 🚧 50% | UnifiedUIManager.enterEditMode() | API exists, no visual editor |
| SF-ARCHITECTURE-010 | UI-L2-005: Layout Persistence | ✅ 100% | HudLayoutManager | Using JsonFileRepository |
| SF-ARCHITECTURE-010 | UI-L3-001: UnifiedUIManager | ✅ 100% | UnifiedUIManager.java | Complete |
| SF-ARCHITECTURE-010 | UI-L3-002: AppManifest | ✅ 100% | AppManifest.java | Complete |
| SF-ARCHITECTURE-010 | UI-L3-003: ThemeDefinition | ❌ 0% | - | Not implemented |
| SF-ARCHITECTURE-010 | UI-L3-005: HudLayoutManager | ✅ 100% | HudLayoutManager.java | Complete |
| SF-ARCHITECTURE-010b | UIHR-L2-001: File Watching | ✅ 100% | UIHotReloadService | Complete with WatchService |
| SF-ARCHITECTURE-010b | UIHR-L2-002: Content Supplier | ✅ 100% | UIHotReloadService.createSupplier() | Complete |
| SF-ARCHITECTURE-010b | UIHR-L2-003: Player UI Refresh | ✅ 100% | UnifiedUIManager.refreshPlayerUIs() | Complete |
| SF-ARCHITECTURE-010b | UIHR-L2-004: Manual Reload Command | ✅ 100% | UIReloadCommand | Complete |
| SF-ARCHITECTURE-010b | UIHR-L2-005: Dev Mode Toggle | ✅ 100% | DevModeConfig | ARGONATH_ENV safety check works |
| VDD-MISC-001 | Action Bar HUD | 🚧 40% | action-bar.hyuiml | Template exists, ~40% CSS valid, no adapter |
| VDD-MISC-002 | Combat Frames HUD | 🚧 30% | combat-frames.hyuiml | Template exists, ~30% CSS valid, no adapter |
| VDD-MISC-002 | LOTR Theme | ✅ 80% | LotrTheme.java, lotr-theme.css | Java constants complete, CSS not loadable |
| VDD-MISC-005 | NPC Dialogue | 🚧 35% | npc-dialogue.hyuiml, DialoguePageBuilder | Template + builder exist, ~35% CSS valid |
| VDD-MISC-006/007 | Quest Tracker | ❌ 0% | - | In separate mod (06-mod-quest-tracker) |
| VDD-MISC-009 | Compass Bar | 🚧 35% | compass-bar.hyuiml | Template exists, ~35% CSS valid, no adapter |
| VDD-LAYOUT-006 | NPC Vendor | 🚧 25% | npc-vendor.hyuiml | Template exists, ~25% CSS valid, no adapter |
| VDD-MISC-024 | Quest Book | 🚧 30% | quest-book.hyuiml, QuestBookPageBuilder | Template + builder exist, ~30% CSS valid |

### Orphan Implementations (No Specification)

| Location | Description | Proposed Action |
|----------|-------------|-----------------|
| [KeybindHintsHUD.java](src/main/java/com/argonathsystems/framework/ui/hud/KeybindHintsHUD.java) | Context-sensitive keybind hints overlay | CREATE SPEC: VDD-MISC-XXX-keybind-hints |
| [MenuTab.java](src/main/java/com/argonathsystems/framework/ui/menu/MenuTab.java) | Hardcoded 16 menu tabs with hotkeys | DOCUMENT: Reference in SF-010 UI-L2-002 |
| [MenuUIContext.java](src/main/java/com/argonathsystems/framework/ui/menu/MenuUIContext.java) | Menu state context | DOCUMENT: Internal implementation detail |

### Missing Implementations (Spec Not Implemented)

| Spec ID | Requirement | Gap Description | Priority |
|---------|-------------|-----------------|----------|
| SF-ARCHITECTURE-010 UI-L3-003 | ThemeDefinition | No JSON loader for theme definitions | 🟡 MEDIUM |
| SF-ARCHITECTURE-010 UI-L3-004 | XAML Injection | No dynamic template injection for dashboard | 🔴 HIGH |
| VDD-LAYOUT-001 | Character Creation Wizard | No wizard template or builder | 🟡 MEDIUM |
| VDD-LAYOUT-002 | Fullscreen Layout | No base fullscreen template | 🟡 MEDIUM |
| VDD-MISC-010 | Minimap | No minimap template or builder | 🔴 HIGH |
| VDD-MISC-015 | Inventory/Equipment | No inventory template | 🔴 HIGH |
| VDD-MISC-018 | Marketplace | No marketplace template | 🟡 MEDIUM |

---

## Accessor v2.0.0 Migration

### Required Changes

| Location | Current Type | Target Type | Migration Notes |
|----------|--------------|-------------|-----------------|
| [MenuTabHandler.java#L31](src/main/java/com/argonathsystems/framework/ui/menu/MenuTabHandler.java#L31) | `Object data` | `DataValue` | Change parameter type and update implementations |

### Breaking Change Impact

The module is **largely compliant** with accessor v2.0.0:
- ✅ `HudLayoutData` used correctly in `HudLayoutManager` via accessor
- ✅ `UIContext` used correctly in HytaleUIAccessor
- ✅ `UIUpdateData` used correctly in accessor interfaces
- ⚠️ One `Object data` parameter in `MenuTabHandler` needs migration

**Low Impact**: Only 1 interface method needs migration, and it has a default no-op implementation.

---

## HyUI Integration (CRITICAL)

### Current UI Components

| Component | HyUI Widget | Status | Documentation Reference |
|-----------|-------------|--------|------------------------|
| NPC Dialogue | PageBuilder + `.page-overlay` | ⚠️ Template Only | [page-building.md](../00-Argonath-External-Docs/HyUI/docs/page-building.md) |
| Quest Book | PageBuilder + `.decorated-container` | ⚠️ Template Only | [page-building.md](../00-Argonath-External-Docs/HyUI/docs/page-building.md) |
| Action Bar | HudBuilder + anchor positioning | ⚠️ Template Only | [hud-building.md](../00-Argonath-External-Docs/HyUI/docs/hud-building.md) |
| Combat Frames | HudBuilder + anchor positioning | ⚠️ Template Only | [hud-building.md](../00-Argonath-External-Docs/HyUI/docs/hud-building.md) |
| Compass Bar | HudBuilder + horizontal layout | ⚠️ Template Only | [hud-building.md](../00-Argonath-External-Docs/HyUI/docs/hud-building.md) |
| NPC Vendor | PageBuilder + tabbed content | ⚠️ Template Only | [page-building.md](../00-Argonath-External-Docs/HyUI/docs/page-building.md) |

### Required HyUI Patterns

1. **Template Processing**: Use HyUI's `TemplateProcessor` instead of custom implementation
   ```java
   TemplateProcessor template = new TemplateProcessor()
       .setVariable("npc", npcData)
       .setVariable("choices", choicesList);
   String html = template.process(rawTemplate);
   ```

2. **Page Structure**: All pages need `.page-overlay` wrapper
   ```html
   <div class="page-overlay">
       <div class="decorated-container" data-hyui-title="Title">
           <div class="container-contents">...</div>
       </div>
   </div>
   ```

3. **Event Binding**: Use `.addEventListener()` with element IDs
   ```java
   PageBuilder.pageForPlayer(playerRef)
       .fromHtml(html)
       .addEventListener("choice-1", CustomUIEventBindingType.Activating, (data, ctx) -> {
           handleChoice(1);
       })
       .open(store);
   ```

4. **HUD Multi-HUD System**: HyUI automatically manages multiple HUDs
   ```java
   HudBuilder.hudForPlayer(playerRef)
       .fromHtml(hudHtml)
       .withRefreshRate(1000)
       .show(store);
   ```

5. **Supported CSS Properties** (per HyUI docs):
   - `color`, `font-size`, `font-weight`, `text-transform`
   - `layout-mode`, `anchor-*`, `flex-weight`
   - `background-color`, `background-image`, `visibility`
   - ❌ NOT supported: `padding`, `margin`, `border`, `gap`, `transform`, `line-height`, `text-shadow`

### HyUI Migration Notes

1. **Delete custom template processing** in `DialoguePageBuilder.processTemplate()` - use HyUI's `TemplateProcessor`
2. **Fix CSS in all 6 templates** - remove unsupported properties
3. **Add `.page-overlay` wrapper** to page templates (quest-book, npc-vendor)
4. **Create adapter layer** in `02-adapter-hytale/src/.../ui/` for HyUI rendering
5. **Replace `:hover` pseudo-selectors** with `custom-textbutton` and `data-hyui-*-bg` attributes

---

## Hytale SDK Integration

### SDK Types Used

| Argonath Type | Hytale SDK Type | ECS Pattern | Notes |
|---------------|-----------------|-------------|-------|
| UiFrameworkPlugin | JavaPlugin | Plugin lifecycle | **VIOLATION**: Should be in adapter layer |
| (none currently) | PlayerRef | Entity reference | Needed for HyUI PageBuilder/HudBuilder |
| (none currently) | Store<EntityStore> | ECS store access | Needed for HyUI .open()/.show() |
| (none currently) | World | World thread execution | Needed for world.execute() safety |

### ECS Alignment Requirements

1. **Move UiFrameworkPlugin to adapter layer** - Framework code should not extend JavaPlugin
2. **Use PlayerRef through accessor** - Never directly reference Hytale Player in framework
3. **World thread safety** - All HyUI .show()/.open() calls must be on world thread via world.execute()

---

## Implementation Phases

### Phase 1: Critical Fixes [3 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P1-001 | Delete UiFrameworkPlugin.java - Frameworks are NOT plugins | UiFrameworkPlugin.java | 0.5 days | None |
| P1-002 | Fix MenuTabHandler Object→DataValue migration | MenuTabHandler.java | 0.5 days | accessor v2.0.0 |
| P1-003 | Fix MenuTab.fromHotkey/fromId to return Optional | MenuTab.java | 0.5 days | None |
| P1-004 | Remove HytaleServer-parent dependency from pom.xml | pom.xml | 0.25 days | None |
| P1-005 | Delete custom processTemplate() - document HyUI TemplateProcessor usage | DialoguePageBuilder.java | 1 day | HyUI docs |
| P1-006 | Update README with architectural changes | README.md | 0.25 days | P1-001 |

### Phase 2: HyUI CSS Compliance [5 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P2-001 | Fix npc-dialogue.hyuiml CSS (remove 40+ unsupported properties) | npc-dialogue.hyuiml | 1 day | HyUI docs |
| P2-002 | Fix quest-book.hyuiml CSS + add .page-overlay | quest-book.hyuiml | 1 day | HyUI docs |
| P2-003 | Fix action-bar.hyuiml CSS | action-bar.hyuiml | 0.5 days | HyUI docs |
| P2-004 | Fix combat-frames.hyuiml CSS | combat-frames.hyuiml | 0.5 days | HyUI docs |
| P2-005 | Fix compass-bar.hyuiml CSS | compass-bar.hyuiml | 0.5 days | HyUI docs |
| P2-006 | Fix npc-vendor.hyuiml CSS + add .page-overlay | npc-vendor.hyuiml | 1 day | HyUI docs |
| P2-007 | Convert :hover to custom-textbutton in all templates | All .hyuiml | 0.5 days | P2-001 through P2-006 |

### Phase 3: HyUI Template Processor Integration [4 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P3-001 | Create TemplateProcessorWrapper in framework for HyUI integration | TemplateProcessorWrapper.java (new) | 1 day | HyUI lib |
| P3-002 | Refactor DialoguePageBuilder to use wrapper | DialoguePageBuilder.java | 1 day | P3-001 |
| P3-003 | Refactor QuestBookPageBuilder to use wrapper | QuestBookPageBuilder.java | 0.5 days | P3-001 |
| P3-004 | Create ActionBarBuilder (pending) | ActionBarBuilder.java (new) | 0.5 days | P3-001 |
| P3-005 | Create CombatFramesBuilder (pending) | CombatFramesBuilder.java (new) | 0.5 days | P3-001 |
| P3-006 | Create CompassBarBuilder (pending) | CompassBarBuilder.java (new) | 0.5 days | P3-001 |

### Phase 4: Adapter Layer Integration [6 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P4-001 | Create DialoguePageAdapter in 02-adapter-hytale | DialoguePageAdapter.java | 2 days | P3-002, HyUI |
| P4-002 | Create QuestBookPageAdapter in 02-adapter-hytale | QuestBookPageAdapter.java | 1.5 days | P3-003, HyUI |
| P4-003 | Enhance ActionBarAdapter with HyUI rendering | ActionBarAdapter.java | 1 day | P3-004, HyUI |
| P4-004 | Enhance CombatFramesAdapter with HyUI rendering | CombatFramesAdapter.java | 1 day | P3-005, HyUI |
| P4-005 | Create CompassBarAdapter in 02-adapter-hytale | CompassBarAdapter.java (new) | 0.5 days | P3-006, HyUI |

### Phase 5: Theme System [3 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P5-001 | Create ThemeDefinition record per SF-010 UI-L3-003 | ThemeDefinition.java (new) | 0.5 days | None |
| P5-002 | Create ThemeLoader for JSON theme files | ThemeLoader.java (new) | 1 day | P5-001 |
| P5-003 | Create ThemeResolver for runtime theme lookup | ThemeResolver.java (new) | 1 day | P5-002 |
| P5-004 | Integrate ThemeResolver into UnifiedUIManager | UnifiedUIManager.java | 0.5 days | P5-003 |

### Phase 6: Testing & Validation [3 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P6-001 | Unit tests for DialoguePageBuilder | DialoguePageBuilderTest.java | 0.5 days | P3-002 |
| P6-002 | Unit tests for QuestBookPageBuilder | QuestBookPageBuilderTest.java | 0.5 days | P3-003 |
| P6-003 | Unit tests for ThemeLoader/ThemeResolver | ThemeLoaderTest.java | 0.5 days | P5-002, P5-003 |
| P6-004 | Integration tests for template processing | TemplateProcessingIT.java | 1 day | P3-001 |
| P6-005 | Update IMPLEMENTATION_TRACKING.md | IMPLEMENTATION_TRACKING.md | 0.5 days | All phases |

---

## Estimated Timeline

| Phase | Duration | Start Condition |
|-------|----------|-----------------|
| Phase 1: Critical Fixes | 3 days | Immediate |
| Phase 2: HyUI CSS Compliance | 5 days | After Phase 1 |
| Phase 3: Template Processor | 4 days | After Phase 2 |
| Phase 4: Adapter Layer | 6 days | After Phase 3, requires 02-adapter-hytale |
| Phase 5: Theme System | 3 days | After Phase 1 (parallelizable with P2-P4) |
| Phase 6: Testing | 3 days | After Phases 3-5 |
| **Total** | **~24 days** | - |

**Note**: Phases 5 can run in parallel with Phases 2-4, reducing critical path to ~21 days.

---

## Dependencies & Blockers

### Upstream Dependencies
| Module | Status | Impact |
|--------|--------|--------|
| `02-framework-accessor` v2.0.0 | ✅ COMPLETE | Required for UIContext/UIUpdateData types |
| `02-framework-core` v2.1.0 | ✅ COMPLETE | Required for core utilities |
| `03-framework-storage` | ✅ COMPLETE | Required for HudLayoutManager persistence |
| `02-adapter-hytale` | 🚧 BLOCKED | Required for HyUI rendering - has UnsupportedOperationException stubs |

### Downstream Impact
| Module | Impact Description |
|--------|---------------------|
| `06-mod-quest-tracker` | CRITICAL: Depends on 05-framework-ui for quest HUD rendering |
| `06-mod-combat` | HIGH: Depends on action bar and combat frames |
| `06-mod-guilds` | MEDIUM: Uses menu tab system |
| `06-mod-dungeons-raids` | MEDIUM: Uses NPC dialogue and quest book |

### External Blockers
| Blocker | Description | Mitigation |
|---------|-------------|------------|
| HyUI Library | Need HyUI jar on classpath for TemplateProcessor | Currently using system scope dependency |
| Hytale SDK | Adapter layer blocked on official SDK | UnsupportedOperationException stubs in place |
| HyUI CSS Limitations | Many CSS properties unsupported | Must rewrite templates per HyUI docs |

---

## Validation Criteria

### Build Validation
- [x] `mvn clean compile` succeeds with zero errors ✅ (verified 2026-01-30)
- [x] `mvn test` passes all unit tests ✅ (19/19 tests passing)
- [ ] No Hytale import leaks (currently has 1 violation in UiFrameworkPlugin.java)

### Architecture Validation
- [ ] All `Object` usages migrated to appropriate types (1 remaining in MenuTabHandler)
- [x] No `return null;` without exception (5 occurrences, all with appropriate logging/Optional justification)
- [x] All accessor interfaces properly implemented via 02-adapter-hytale
- [ ] Framework dependencies correctly used (HytaleServer-parent should be removed)

### Specification Validation
- [ ] All SF-ARCHITECTURE-010 requirements have implementations (~60% complete)
- [ ] All VDD templates have valid HyUI syntax (~35% CSS valid currently)
- [ ] Orphan implementations documented (3 identified - KeybindHintsHUD, MenuTab, MenuUIContext)

---

## HytaleModder Handoff Prompt

```
## Task: Implement Phase 1 Critical Fixes for 05-framework-ui

### Context
- Module: 05-framework-ui
- Implementation Plan: See IMPLEMENTATION_PLAN.md
- Current Status: ~60% complete, 1 architectural violation

### Phase 1 Tasks (3 days)

1. **P1-001: Delete UiFrameworkPlugin.java**
   - Frameworks are NOT plugins - this is an architectural violation
   - Delete: src/main/java/com/argonathsystems/framework/ui/UiFrameworkPlugin.java
   - Update: manifest.json to remove plugin entry point
   - Note: The actual plugin should be in the adapter layer or a separate mod

2. **P1-002: Fix MenuTabHandler Object→DataValue**
   - File: src/main/java/com/argonathsystems/framework/ui/menu/MenuTabHandler.java
   - Change: `void handleEvent(UUID playerId, MenuTab tab, String eventId, Object data)`
   - To: `void handleEvent(UUID playerId, MenuTab tab, String eventId, DataValue data)`
   - Add import: com.argonathsystems.framework.accessorapi.data.DataValue

3. **P1-003: Fix MenuTab return types**
   - File: src/main/java/com/argonathsystems/framework/ui/menu/MenuTab.java
   - Change: `public static MenuTab fromHotkey(String key)` → `Optional<MenuTab>`
   - Change: `public static MenuTab fromId(String id)` → `Optional<MenuTab>`
   - Update all callers to use .orElse() or .ifPresent()

4. **P1-004: Remove HytaleServer-parent dependency**
   - File: pom.xml
   - Remove dependency on HytaleServer-parent (lines 27-31)
   - This framework should not depend on Hytale SDK

5. **P1-005: Document HyUI TemplateProcessor usage**
   - Delete: Custom processTemplate() method in DialoguePageBuilder
   - Add: Javadoc explaining HyUI TemplateProcessor should be used via adapter layer
   - Keep: Template data building methods (they're correct)

6. **P1-006: Update README.md**
   - Remove references to UiFrameworkPlugin
   - Document that framework is initialized via UnifiedUIManager.getInstance().init()

### Validation
- `mvn clean compile` must pass
- `mvn test` must pass (19 tests)
- `grep -rn "import com.hypixel.hytale" src/main/java/` must return empty
```

---

*Document generated by HytaleArchitect agent on 2026-01-30*
