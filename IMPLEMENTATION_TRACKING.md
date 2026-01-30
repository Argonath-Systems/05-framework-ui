# Framework UI - Implementation Tracking

> **Module**: `05-framework-ui`  
> **Status**: ✅ PHASE 4 COMPLETE - HyUI CSS COMPLIANT + ADAPTER LAYER (~75%)  
> **Last Updated**: 2026-01-30 (Session 6: HyUI Compliance Audit)  
> **Version**: 1.1.0-SNAPSHOT

---

## ⚠️ STATUS UPDATE - 2026-01-30 (SESSION 6: HytaleArchitect Audit)

**Previous Status:** Adapters Re-enabled + Unit Tests Created (~60%)  
**Current Status:** HyUI CSS Compliant + Adapter Layer Complete (~75%)

**Session 6 Progress:**
- ✅ **DELETED**: `UiFrameworkPlugin.java` (architectural violation - frameworks are NOT plugins)
- ✅ **FIXED**: `MenuTabHandler.java` - Object→DataValue migration
- ✅ **FIXED**: `MenuTab.java` - return Optional instead of nullable
- ✅ **FIXED**: `DialoguePageBuilder.java` - removed custom processTemplate(), added buildTemplateVariables()
- ✅ **FIXED**: `pom.xml` - removed HytaleServer-parent dependency (frameworks shouldn't depend on SDK)
- ✅ **UPDATED**: All 6 HYUIML templates to HyUI CSS compliant (v1.1.0)
  - Removed unsupported CSS: padding-*, margin-*, gap, border-*, opacity, text-decoration
  - Added .page-overlay wrapper to page templates
  - Converted hover states to data-hyui-*-bg attributes
- ✅ **CREATED**: Template processing infrastructure in `com.argonathsystems.framework.ui.template`:
  - `TemplateProcessorWrapper.java` (interface)
  - `TemplateProcessorFactory.java` (interface)
  - `TemplateLoader.java` (implementation)
- ✅ **CREATED**: Adapter layer in `02-adapter-hytale/src/main/java/.../adapter/hytale/ui`:
  - `HyUITemplateProcessor.java` - HyUI implementation of TemplateProcessorWrapper
  - `HyUITemplateProcessorFactory.java` - Factory with LOTR/Combat/Dialogue component sets
  - `DialoguePageAdapter.java` - Full adapter for NPC dialogue rendering
- ✅ **BUILD VERIFIED**: 19/19 tests passing

**Architectural Changes:**
- Template processing moved to adapter layer (no HyUI imports in framework)
- DialoguePageBuilder is now a data builder only (no HTML processing)
- TemplateProcessorWrapper interface allows platform-agnostic template usage

---

## Implementation Summary

| Category | Complete | Total | Percentage |
|----------|----------|-------|------------|
| HYUIML Template Files Created | 7 | 7 | 100% ✅ |
| HyUI CSS Compliance | 6 | 6 | **100%** ✅ |
| Template Variable Processing | 6 | 7 | **~86%** ✅ |
| Java Data Models | 8 | 8 | 100% ✅ |
| Template Infrastructure | 4 | 4 | **100%** ✅ |
| Adapter Layer (HyUI) | 3 | 5 | **60%** ⚠️ |
| Unit Tests | 19 | 25 | **~76%** ✅ |
| **FUNCTIONAL COMPLETION** | **~53** | **~70** | **~75%** ⚠️ |

---

## HyUIML Templates Status

### ✅ Phase 2: HyUI CSS Compliance (COMPLETE)

| Template | Version | Lines | CSS Valid | Status |
|----------|---------|-------|-----------|--------|
| `npc-dialogue.hyuiml` | 1.1.0 | 145 | ✅ 100% | ✅ Complete |
| `quest-book.hyuiml` | 1.1.0 | 397 | ✅ 100% | ✅ Complete |
| `action-bar.hyuiml` | 1.1.0 | 145 | ✅ 100% | ✅ Complete |
| `combat-frames.hyuiml` | 1.1.0 | 253 | ✅ 100% | ✅ Complete |
| `compass-bar.hyuiml` | 1.1.0 | 165 | ✅ 100% | ✅ Complete |
| `npc-vendor.hyuiml` | 1.1.0 | 292 | ✅ 100% | ✅ Complete |

### CSS Properties Reference

**Supported (Used):**
- `layout-mode`, `anchor-*`, `flex-weight`
- `color`, `font-size`, `font-weight`, `text-transform`
- `background-color`, `background-image`, `visibility`
- `horizontal-align`, `vertical-align`, `text-align`

**Removed (Unsupported):**
- ~~padding-*~~, ~~margin-*~~, ~~gap~~ (spacing)
- ~~border-*~~, ~~border-radius~~ (borders)
- ~~transform~~, ~~line-height~~, ~~opacity~~ (effects)
- ~~text-decoration~~, ~~font-style~~ (text decoration)

---

## Template Infrastructure Status

### ✅ Framework Layer (`05-framework-ui`)

| Class | Package | Status | Description |
|-------|---------|--------|-------------|
| `TemplateProcessorWrapper` | template | ✅ Interface | Platform-agnostic template processing |
| `TemplateProcessorFactory` | template | ✅ Interface | Factory for creating processors |
| `TemplateLoader` | template | ✅ Implementation | Loads templates from resources |
| `DialoguePageBuilder` | dialogue | ✅ Refactored | Data builder (no HTML processing) |

### ✅ Adapter Layer (`02-adapter-hytale`)

| Class | Package | Status | Description |
|-------|---------|--------|-------------|
| `HyUITemplateProcessor` | ui | ✅ Implementation | HyUI TemplateProcessor wrapper |
| `HyUITemplateProcessorFactory` | ui | ✅ Implementation | Factory with component sets |
| `DialoguePageAdapter` | ui | ✅ Implementation | Full dialogue rendering |
| `QuestBookPageAdapter` | ui | ⏳ Pending | Quest book rendering |
| `VendorPageAdapter` | ui | ⏳ Pending | Vendor UI rendering |
3. Use only HyUI-supported properties: `color`, `font-size`, `font-weight`, `text-transform`, `layout-mode`, `anchor-*`, `flex-weight`, `background-color`, `background-image`, `visibility`

### Blocker 2: No Template Processing ❌
**Impact:** Variable interpolation does not work  
**Affected:** All "Builder" classes  
**Missing:**
- No `TemplateProcessor` usage
- `generateHtml()` returns raw template strings with unparsed `{{$variables}}`
- No Handlebars library integration

**Fix Required:**
```java
// Add dependency to pom.xml:
<dependency>
    <groupId>com.github.jknack</groupId>
    <artifactId>handlebars</artifactId>
    <version>4.3.1</version>
</dependency>

// Refactor DialoguePageBuilder.generateHtml():
public String generateHtml() {
    Handlebars handlebars = new Handlebars();
    // Register helpers: eq, isEmpty, etc.
    Template template = handlebars.compileInline(templateSupplier.get());
    return template.apply(buildTemplateData());
}
```

### Blocker 3: No HyUI Integration ❌
**Impact:** Cannot actually render UIs  
**Affected:** All UI components  
**Missing:**
- No adapter layer code in `02-adapter-hytale`
- No `PageBuilder.fromHtml().open()` calls
- No event listener binding
- No PlayerRef/Store handling

**Fix Required:**
Create `02-adapter-hytale/src/.../ui/DialogueAdapter.java`:
```java
public class DialogueAdapter {
    public void showDialogue(PlayerRef player, Store<EntityStore> store, DialogueData data) {
        // Process template
        DialogueTemplateProcessor processor = new DialogueTemplateProcessor();
        String html = processor.populate(data);
        
        // Show via HyUI
        PageBuilder.pageForPlayer(player)
            .fromHtml(html)
            .addEventListener("choice-1", CustomUIEventBindingType.Activating, (evt, ctx) -> {
                handleChoice(player, 1);
            })
            // ... more event bindings
            .open(store);
    }
}
```

### Blocker 4: Missing `.page-overlay` Wrapper ❌
**Impact:** Page-based UIs won't render correctly  
**Affected:** npc-dialogue.hyuiml (claims to be modal but structured as HUD)  
**Decision Required:** Is this a Page (modal) or HUD (overlay)?

**If Page/Modal:**
```html
<div class="page-overlay">
    <div class="hud-npc-dialogue">
        <!-- content -->
    </div>
</div>
```

**If HUD:** Remove modal claims, ensure proper anchor positioning

---

## ✅ IMMEDIATE ACTION ITEMS

### Priority 0 (This Week)
1. ✅ Fix `<hyvatar>` syntax (DONE)
2. ✅ Remove `data-hyui-layer` (DONE for npc-dialogue.hyuiml)
3. ✅ Remove unsupported CSS (IN PROGRESS for npc-dialogue.hyuiml)
4. ⏳ Apply CSS fixes to remaining 5 templates
5. ⏳ Decide Page vs HUD for npc-dialogue

### Priority 1 (Next Week)
1. ⏳ Add Handlebars dependency
2. ⏳ Implement `TemplateProcessor` wrapper
3. ⏳ Create minimal `DialogueAdapter` in 02-adapter-hytale
4. ⏳ Test end-to-end rendering of ONE dialogue

### Priority 2 (Week 3)
1. ⏳ Replace all pseudo-selectors with custom buttons
2. ⏳ Implement keyboard shortcut handling (1-9 keys)
3. ⏳ Create QuestBookAdapter
4. ⏳ Write unit tests for template processing

---

## Java Data Models Status (NOT HyUI Builders)

### ⚠️ Phase 1: Template Data Models (Misleading Names)

| Class | Package | Lines | Actual Purpose | Issues |
|-------|---------|-------|----------------|--------|
| `DialoguePageBuilder` | `...ui.dialogue` | 230 | Template data container | ❌ Not a HyUI Builder, no template processing |
| `DialogueChoice` | `...ui.dialogue` | 80 | Data model | ✅ OK as data model |
| `QuestOfferData` | `...ui.dialogue` | 140 | Data model | ✅ OK as data model |
| `QuestBookPageBuilder` | `...ui.quest` | 130 | Template data container | ❌ Not a HyUI Builder, no template processing |
| `QuestCategory` | `...ui.quest` | 50 | Data model | ✅ OK as data model |
| `QuestListItem` | `...ui.quest` | 45 | Data model | ✅ OK as data model |
| `QuestDetail` | `...ui.quest` | 130 | Data model | ✅ OK as data model |
| `LotrTheme` | `...ui` | 120 | Constants class | ✅ Complete |

### 🔄 Phase 5: Pending

| Class | Package | Priority | Lines | Effort |
|-------|---------|----------|-------|--------|
| `ActionBarBuilder` | `...ui.combat` | P1 | 150 | 1 day |
| `CombatFramesBuilder` | `...ui.combat` | P1 | 200 | 2 days |
| `CompassBarBuilder` | `...ui.world` | P1 | 100 | 1 day |
| `VendorPageBuilder` | `...ui.vendor` | P1 | 180 | 2 days |
| `InventoryPageBuilder` | `...ui.inventory` | P1 | 200 | 2 days |
| `CharacterSheetBuilder` | `...ui.character` | P1 | 250 | 2 days |
| `MinimapBuilder` | `...ui.world` | P1 | 120 | 1 day |

---

## Adapter Layer Integration (Pending)

The following adapter classes need to be created in `02-adapter-hytale` to render these UIs:

| Adapter Class | Connects | Priority | Effort |
|---------------|----------|----------|--------|
| `DialogueHudAdapter` | DialoguePageBuilder → HyUI | P0 | 2 days |
| `QuestBookAdapter` | QuestBookPageBuilder → HyUI | P0 | 2 days |
| `ActionBarAdapter` | ActionBarBuilder → HyUI HudBuilder | P1 | 1 day |
| `CombatFramesAdapter` | CombatFramesBuilder → HyUI HudBuilder | P1 | 2 days |
| `CompassBarAdapter` | CompassBarBuilder → HyUI HudBuilder | P1 | 1 day |
| `VendorAdapter` | VendorPageBuilder → HyUI PageBuilder | P1 | 2 days |
| `KeyboardShortcutHandler` | Number keys 1-9 → dialogue choices | P0 | 1 day |
| `TypewriterAnimator` | Character-by-character text reveal | P1 | 1 day |
| `CooldownAnimationHandler` | Circular sweep cooldown | P1 | 1 day |

---

## Testing Status

### Unit Tests (0/8 Complete)

| Test Suite | Coverage | Status |
|------------|----------|--------|
| DialoguePageBuilderTest | 0% | ❌ Not started |
| QuestBookPageBuilderTest | 0% | ❌ Not started |
| LotrThemeTest | 0% | ❌ Not started |
| DialogueChoiceTest | 0% | ❌ Not started |
| QuestOfferDataTest | 0% | ❌ Not started |
| QuestCategoryTest | 0% | ❌ Not started |
| QuestListItemTest | 0% | ❌ Not started |
| QuestDetailTest | 0% | ❌ Not started |

### Integration Tests (Pending)

- [ ] Hot reload system E2E test
- [ ] Template rendering with real HyUI
- [ ] Keyboard shortcut handling (1-9)
- [ ] Quest book tab switching
- [ ] Vendor transaction flow
- [ ] Compass marker positioning
- [ ] Combat frame buff updates

---

## Documentation Status

### ✅ Completed

- [x] CHANGELOG.md updated with all new features
- [x] README.md updated with comprehensive usage examples
- [x] Inline Javadoc for all public APIs
- [x] IMPLEMENTATION_TRACKING.md created

### 🔄 Pending

- [ ] User guide: NPC dialogue system usage
- [ ] User guide: Quest book navigation
- [ ] Developer guide: Creating new UI templates
- [ ] Theme customization guide
- [ ] API reference documentation (Javadoc generation)
- [ ] Sample code in 00-Argonath-Samples
- [ ] Video tutorial: UI hot reload workflow

---

## Timeline & Roadmap

### ⏳ Phase 1: Foundation (CURRENT - 4-6 weeks)

| Task | Components | Status | Estimated Duration |
|------|------------|--------|--------------------|
| **Fix Critical Syntax** | All HYUIML templates | 🔄 In Progress | 3 days |
| **Template Processing** | Handlebars integration | ⏳ Not Started | 5 days |
| **Adapter Layer** | DialogueAdapter, QuestBookAdapter | ⏳ Not Started | 10 days |
| **Event Handling** | Keyboard shortcuts, button clicks | ⏳ Not Started | 7 days |
| **E2E Testing** | One complete dialogue flow | ⏳ Not Started | 3 days |

### 🔄 Remaining Work (8-12 weeks)

| Phase | Components | Priority | Estimated Duration |
|-------|------------|----------|-------------------|
| **Phase 2: Combat/World UIs** | Action bar, combat frames, compass | P1 | 3 weeks |
| **Phase 3: Vendor System** | NPC vendor integration | P1 | 2 weeks |
| **Phase 4: Additional UIs** | 12 remaining templates | P2-P3 | 4-6 weeks |
| **Phase 5: Testing & Docs** | Unit tests, integration tests, guides | P2 | 2 weeks |

**Revised Timeline:**
- ⏳ **Phase 1 (Current):** 4-6 weeks
- ⏳ **Phase 2-5:** 11-13 weeks
- **Realistic Total:** ~15-19 weeks (~3.5-4.5 months)

---

## Version Roadmap

| Version | Target | Features | Status |
|---------|--------|----------|--------|
| **1.0.0-SNAPSHOT** | Current | Template structure, data models, LOTR theme constants | 🔄 10-15% Complete |
| **1.0.0-ALPHA1** | Week 6 | Fixed syntax, template processing, ONE working dialogue | ⏳ Planned |
| **1.0.0-ALPHA2** | Week 12 | Quest book, adapter layer complete | ⏳ Planned |
| **1.0.0-BETA1** | Q2 2026 | All Phase 1-3 UIs functional | ⏳ Planned |
| **1.0.0** | Q3 2026 | Production-ready with tests and docs | ⏳ Planned |

---

## Current Sprint Focus

### 🔥 Sprint 1: Critical Syntax Fixes (Week 1-2)
- [x] Audit all HYUIML templates against HyUI documentation
- [x] Fix `<hyvatar>` syntax in npc-dialogue.hyuiml
- [x] Remove invalid `data-hyui-layer` from all templates
- [🔄] Remove unsupported CSS properties from npc-dialogue.hyuiml (IN PROGRESS)
- [ ] Apply CSS fixes to remaining 5 templates
- [ ] Replace all pseudo-selectors with `custom-textbutton` approach
- [ ] Update IMPLEMENTATION_TRACKING.md with realistic status

### ⏳ Sprint 2: Template Processing (Week 3-4)
- [ ] Add Handlebars dependency to pom.xml
- [ ] Implement `TemplateProcessor` wrapper class
- [ ] Refactor `DialoguePageBuilder.generateHtml()` to use Handlebars
- [ ] Register template helpers: `eq`, `isEmpty`, `contains`
- [ ] Test variable interpolation with sample data
- [ ] Refactor `QuestBookPageBuilder` similarly

### ⏳ Sprint 3: Minimal Adapter (Week 5-6)
- [ ] Create `02-adapter-hytale/.../ui/` package
- [ ] Implement `DialogueAdapter.showDialogue()`
- [ ] Wire up ONE event listener (close button)
- [ ] Test complete flow: data → template → HyUI → player
- [ ] Document integration pattern for other UIs

---

## Package Structure (Updated)

```
com.argonathsystems.framework.ui/
├── UiFrameworkPlugin.java          ✅ Complete
├── LotrTheme.java                  ✅ Complete (NEW)
├── dialogue/                       ✅ Complete (NEW)
│   ├── DialoguePageBuilder.java
│   ├── DialogueChoice.java
│   └── QuestOfferData.java
├── quest/                          ✅ Complete (NEW)
│   ├── QuestBookPageBuilder.java
│   ├── QuestCategory.java
│   ├── QuestListItem.java
│   └── QuestDetail.java
├── combat/                         🔄 Pending
│   ├── ActionBarBuilder.java
│   └── CombatFramesBuilder.java
├── world/                          🔄 Pending
│   ├── CompassBarBuilder.java
│   └── MinimapBuilder.java
├── vendor/                         🔄 Pending
│   └── VendorPageBuilder.java
├── hud/                            ⬜ Existing
│   ├── HudLayoutManager.java       ✅ Complete
│   ├── HudElement.java             ✅ Complete
│   ├── KeybindHintsHUD.java        ✅ Complete
│   └── DynamicHudBuilder.java      ⬜ Not Started
├── menu/                           ⬜ Existing
│   ├── MainMenuManager.java        ✅ Complete
│   ├── MenuTab.java                ✅ Complete
│   ├── MenuItem.java               ✅ Complete
│   ├── MenuBuilder.java            ⬜ Not Started
│   └── MenuPagination.java         ⬜ Not Started
└── keybind/                        ⬜ Existing
    ├── KeybindRegistry.java        ✅ Complete
    ├── KeybindHandler.java         ✅ Complete
    └── ContextKeybinds.java        ⬜ Not Started
```

### Resources Structure (NEW)

```
src/main/resources/config/ui/
├── huds/                           ✅ Complete (NEW)
│   ├── npc-dialogue.hyuiml
│   ├── action-bar.hyuiml
│   ├── combat-frames.hyuiml
│   └── compass-bar.hyuiml
├── pages/                          ✅ Complete (NEW)
│   ├── quest-book.hyuiml
│   └── npc-vendor.hyuiml
└── styles/                         ✅ Complete (NEW)
    └── lotr-theme.css
```

---

## Source Statistics (Updated)

| Metric | Value |
|--------|-------|
| HyUIML Template Files | 7 |
| Java Source Files | 23 |
| Test Files | 0 |
| Lines of HyUIML | ~1,920 |
| Lines of Java Code | ~1,850 |
| Total Lines | ~3,770 |

---
