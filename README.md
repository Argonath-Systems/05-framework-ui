# UI Framework

> **User interface components and rendering with HyUIML support**

[![GitHub](https://img.shields.io/badge/GitHub-Argonath--Systems-181717?logo=github)](https://github.com/Argonath-Systems/05-framework-ui)
[![Maven](https://img.shields.io/badge/Maven-Central-C71A36?logo=apache-maven)](https://maven.apache.org/)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](../LICENSE)
[![Website](https://img.shields.io/badge/Docs-argonath--systems.github.io-blue)](https://argonath-systems.github.io/00-Argonath-Wiki)

---

## 📋 Overview

Platform-agnostic UI framework providing HyUIML-based templates and Java builders for all in-game UI components. Implements the complete LOTR-themed interface system for quests, NPCs, combat, and world interaction.

**Key Features:**
- 🎨 **HyUIML Templates** - HTML-like markup for all UI components
- 🏰 **LOTR Theme System** - Consistent parchment, gold, and earth-tone styling
- 🔄 **Hot Reload** - Development mode with automatic UI refresh
- 🧩 **Reusable Widgets** - Shared components across all UIs
- 🎮 **Platform Agnostic** - Zero Hytale imports, works through Accessor API

## 🏗️ C4 Component Diagram

> [!NOTE]
> Detailed architecture diagrams can be found in the [Argonath Wiki C4 Documentation](https://argonath-systems.github.io/00-Argonath-Wiki/diagrams/).

## ✨ Features

### In-Game UI Components

**Quest & NPC (Phase 1)**
- **NPC Dialogue HUD** - Bottom-center modal with quest offers, keyboard shortcuts (1-9), typewriter effect
- **Quest Book Page** - Journal with Active/Complete/Failed tabs, category accordion, track/abandon actions

**Combat UI (Phase 2)**
- **Action Bar HUD** - 12 ability slots with cooldown animations, health/mana bars
- **Combat Frames HUD** - Player/target/party frames with HP/MP, buffs, debuffs, cast bars

**World UI (Phase 3)**
- **Compass Bar HUD** - Skyrim-style horizontal compass with directional markers, quest/POI tracking

**NPC Interaction (Phase 4)**
- **NPC Vendor Page** - Shop interface with Buy/Sell/Buyback tabs, quantity selectors, transaction controls

**LOTR Theme System**
- `lotr-theme.css` - Comprehensive CSS variables for colors, fonts, spacing
- `LotrTheme.java` - Java constants mirroring CSS for programmatic use
- Quest type colors (Main/Side/Daily/Weekly/Guild/Timed/Event)
- Parchment textures, gold accents, Aniron/Ringbearer fonts

### Development Features

- **Hot Reload System** - Auto-refresh UIs on file changes in dev mode
- **Template Suppliers** - `createUISupplier()` for hot-reloadable content
- **Java Builders** - Type-safe API for building UIs programmatically
- **VDD Compliance** - All UIs follow Visual Design Document specifications

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>com.argonathsystems.framework</groupId>
    <artifactId>argonath-window-ui</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Build from Source

```bash
# Clone the repository
git clone https://github.com/Argonath-Systems/05-framework-ui.git
cd 05-framework-ui

# Build with justfile
just compile

# Or build with Maven
mvn clean install
```

## 🚀 Usage

### NPC Dialogue Window

```java
import com.argonathsystems.framework.ui.dialogue.*;

// Create dialogue builder
DialoguePageBuilder builder = new DialoguePageBuilder();
builder.setTemplateSupplier(() -> loadTemplate("config/ui/huds/npc-dialogue.hyuiml"))
       .setNpcName("Gandalf the Grey")
       .setNpcAvatarId("Gandalf")
       .setDialogueText("A wizard is never late, Frodo Baggins...")
       .addChoice(1, "Tell me about the Ring", "quest-ring", true, "Quest")
       .addChoice(2, "What news from the Shire?", "news-shire", true, "Lore")
       .addChoice(3, "Goodbye", "farewell", true);

// Generate HTML
String html = builder.generateHtml();
// Pass to HyUI via adapter layer
```

### Quest Offer Mode

```java
QuestOfferData quest = QuestOfferData.builder("The Fellowship of the Ring")
    .addObjective("Travel to Rivendell")
    .addObjective("Attend the Council of Elrond")
    .addReward("hytale:ring_of_power", "Ring of Power", 1)
    .withXpReward(1000)
    .withGoldReward(500)
    .build();

builder.setQuestOffer(quest);
```

### Quest Book Journal

```java
import com.argonathsystems.framework.ui.quest.*;

QuestBookPageBuilder book = new QuestBookPageBuilder();
book.setTemplateSupplier(() -> loadTemplate("config/ui/pages/quest-book.hyuiml"))
    .setActiveTab("active");

// Add category
QuestCategory mainQuests = new QuestCategory("Main Story");
mainQuests.addQuest(new QuestListItem(
    "quest-fellowship",
    "The Fellowship of the Ring",
    50,
    "Main Quest",
    "icons/quest-main.png"
));
book.addCategory(mainQuests);

// Set selected quest details
QuestDetail detail = QuestDetail.builder("quest-fellowship", "The Fellowship of the Ring")
    .withLevel(50)
    .withType("Main Quest")
    .withGiver("Gandalf")
    .withDescription("Journey to Rivendell to attend the Council of Elrond...")
    .addObjective("Travel to Rivendell", false, 0, 1)
    .addObjective("Attend the Council", false, 0, 1)
    .withXpReward(1000)
    .withGoldReward(500)
    .build();
book.setSelectedQuest(detail);

String html = book.generateHtml();
```

### Using LOTR Theme Constants

```java
import com.argonathsystems.framework.ui.LotrTheme;

// Use color constants
String healthBarColor = LotrTheme.COLOR_HEALTH; // #dc143c
String goldAccent = LotrTheme.COLOR_GOLD;       // #d4a017

// Use component sizes
int slotSize = LotrTheme.ACTION_SLOT_SIZE;      // 46px
int spacing = LotrTheme.SPACING_MD;              // 12px

// Use quest type enum
LotrTheme.QuestType type = LotrTheme.QuestType.MAIN;
String color = type.getColor();                  // #ffd700
String name = type.getDisplayName();             // "Main Quest"
```

## 🛠️ Development

### Prerequisites

- Java 25 or higher
- Maven 3.9+
- just (command runner)

### Building

```bash
# Compile the project
just compile

# Run tests
just test

# Deploy to local Maven repository
just deploy
```

## 🏛️ Architecture

### Zero Hytale Imports Policy

This framework strictly adheres to the **Platform Agnostic** architecture principle:
- **NO** `hytale.*` imports allowed in this module
- All game engine interactions go through the Accessor API
- Business logic remains portable and testable

### Dependencies

- **See pom.xml for current dependencies**

## 📖 Documentation

- [Argonath Systems Wiki](https://argonath-systems.github.io/00-Argonath-Wiki)
- [API Documentation](./docs/api)
- [Architecture Documentation](../design/C4)

## 🤝 Contributing

Please read [CONTRIBUTING.md](../CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.

## 🔗 Related Projects

- [Argonath Systems](https://github.com/Argonath-Systems)
- [LordOfTheTales](https://github.com/K1ntus/LordOfTheTales)
- [HyUI](https://github.com/Argonath-Systems/HyUI)

---

**Built with ❤️ by the Argonath Systems Team**
