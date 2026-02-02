package com.argonathsystems.framework.ui.menu;

import java.util.Optional;

/**
 * Defines all sub-pages within the Main Character Panel tabs.
 * Each sub-page belongs to exactly one {@link MainPanelTab} and represents
 * a distinct UI page that can be displayed within the panel content area.
 * 
 * <p>This enum aligns with specification VDD-LAYOUT-003 which defines:
 * <ul>
 *   <li>Inventory: Equipment, Bags, Currency, Cosmetics</li>
 *   <li>Quest: Quest Book, Quest Designer (Admin)</li>
 *   <li>Social: Friends &amp; Party, Guild, Housing</li>
 *   <li>Faction: Reputation, War Overview</li>
 *   <li>Map: World Map, Zone Map, Discovered Locations</li>
 *   <li>Work: Professions, Recipes, Gathering Log</li>
 *   <li>Dungeon: Group Finder, Dungeon Journal, Raid Planner</li>
 *   <li>More: Achievements, Mounts, Pets, Titles, Collections, PvP</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 2.0.0
 * @see MainPanelTab
 * @see SubPageHandler
 */
public enum SubPage {
    
    // ========================================================================
    // INVENTORY TAB
    // ========================================================================
    
    /**
     * Equipment page showing worn items and equipment slots.
     * VDD-MISC-015: Inventory & Equipment page.
     */
    EQUIPMENT("equipment", MainPanelTab.INVENTORY, "🗡️", "Equipment", 
              "inventory-equipment.hyuiml", 0, false),
    
    /**
     * Bags page showing bag slots and inventory grid.
     */
    BAGS("bags", MainPanelTab.INVENTORY, "🎒", "Bags", 
         "inventory-bags.hyuiml", 1, false),
    
    /**
     * Currency page showing all currencies (gold, silver, copper, tokens).
     */
    CURRENCY("currency", MainPanelTab.INVENTORY, "💰", "Currency", 
             "inventory-currency.hyuiml", 2, false),
    
    /**
     * Cosmetics page for transmog and appearance items.
     */
    COSMETICS("cosmetics", MainPanelTab.INVENTORY, "👗", "Cosmetics", 
              "inventory-cosmetics.hyuiml", 3, false),
    
    // ========================================================================
    // QUEST TAB
    // ========================================================================
    
    /**
     * Quest Book page showing all quests.
     * VDD-MISC-024: Quest Book page.
     */
    QUEST_BOOK("quest-book", MainPanelTab.QUEST, "📖", "Quest Book", 
               "quest-book.hyuiml", 0, false),
    
    /**
     * Quest Designer page for creating/editing quests (admin only).
     * VDD-MISC-025: Quest Designer page.
     */
    QUEST_DESIGNER("quest-designer", MainPanelTab.QUEST, "✏️", "Quest Designer", 
                   "quest-designer.hyuiml", 1, true),
    
    // ========================================================================
    // SOCIAL TAB
    // ========================================================================
    
    /**
     * Friends &amp; Party page showing online friends and party management.
     * VDD-MISC-027: Social Panel page.
     */
    FRIENDS("friends", MainPanelTab.SOCIAL, "👫", "Friends & Party", 
            "social-friends.hyuiml", 0, false),
    
    /**
     * Guild management page showing roster, bank, logs.
     * VDD-MISC-026: Guild Management page.
     */
    GUILD("guild", MainPanelTab.SOCIAL, "🏛️", "Guild", 
          "guild-management.hyuiml", 1, false),
    
    /**
     * Housing page for player housing management.
     * HLR-WORK-020: Housing System.
     */
    HOUSING("housing", MainPanelTab.SOCIAL, "🏠", "Housing", 
            "social-housing.hyuiml", 2, false),
    
    // ========================================================================
    // FACTION TAB
    // ========================================================================
    
    /**
     * Reputation page showing faction standings.
     * VDD-MISC-020: Faction Panel page.
     */
    REPUTATION("reputation", MainPanelTab.FACTION, "⭐", "Reputation", 
               "faction-reputation.hyuiml", 0, false),
    
    /**
     * War Overview page showing ongoing conflicts.
     * HLR-WARFARE-012: War &amp; Diplomacy System.
     */
    WAR_OVERVIEW("war-overview", MainPanelTab.FACTION, "⚔️", "War Overview", 
                 "faction-war.hyuiml", 1, false),
    
    // ========================================================================
    // MAP TAB
    // ========================================================================
    
    /**
     * World Map page showing the full world.
     * HLR-WORLD-024: Map Rendering Library.
     */
    WORLD_MAP("world-map", MainPanelTab.MAP, "🌍", "World Map", 
              "map-world.hyuiml", 0, false),
    
    /**
     * Zone Map page showing the current zone.
     */
    ZONE_MAP("zone-map", MainPanelTab.MAP, "📍", "Zone Map", 
             "map-zone.hyuiml", 1, false),
    
    /**
     * Discovered Locations page showing explored areas.
     */
    DISCOVERED("discovered", MainPanelTab.MAP, "🔍", "Discovered", 
               "map-discovered.hyuiml", 2, false),
    
    // ========================================================================
    // WORK TAB
    // ========================================================================
    
    /**
     * Professions page showing crafting professions.
     * HLR-WORK-006: Resources &amp; Crafting System.
     */
    PROFESSIONS("professions", MainPanelTab.WORK, "📊", "Professions", 
                "work-professions.hyuiml", 0, false),
    
    /**
     * Recipes page showing learned recipes.
     */
    RECIPES("recipes", MainPanelTab.WORK, "📜", "Recipes", 
            "work-recipes.hyuiml", 1, false),
    
    /**
     * Gathering Log page showing gathered resources.
     */
    GATHERING_LOG("gathering-log", MainPanelTab.WORK, "🪵", "Gathering Log", 
                  "work-gathering.hyuiml", 2, false),
    
    // ========================================================================
    // DUNGEON TAB
    // ========================================================================
    
    /**
     * Group Finder page for finding dungeon/raid groups.
     * VDD-MISC-017: Group Finder page.
     */
    GROUP_FINDER("group-finder", MainPanelTab.DUNGEON, "🔎", "Group Finder", 
                 "dungeon-group-finder.hyuiml", 0, false),
    
    /**
     * Dungeon Journal page showing dungeon information.
     */
    DUNGEON_JOURNAL("dungeon-journal", MainPanelTab.DUNGEON, "📕", "Dungeon Journal", 
                    "dungeon-journal.hyuiml", 1, false),
    
    /**
     * Raid Planner page for organizing raids.
     */
    RAID_PLANNER("raid-planner", MainPanelTab.DUNGEON, "📋", "Raid Planner", 
                 "dungeon-raid-planner.hyuiml", 2, false),
    
    // ========================================================================
    // MORE TAB
    // ========================================================================
    
    /**
     * Achievements page showing unlocked achievements.
     * VDD-MISC-022: Achievements Panel page.
     */
    ACHIEVEMENTS("achievements", MainPanelTab.MORE, "🏆", "Achievements", 
                 "more-achievements.hyuiml", 0, false),
    
    /**
     * Mounts page showing mount collection.
     * VDD-MISC-029: Mount Collection page.
     */
    MOUNTS("mounts", MainPanelTab.MORE, "🐎", "Mounts", 
           "more-mounts.hyuiml", 1, false),
    
    /**
     * Pets page showing pet companions.
     */
    PETS("pets", MainPanelTab.MORE, "🐕", "Pets", 
         "more-pets.hyuiml", 2, false),
    
    /**
     * Titles page showing earned titles.
     */
    TITLES("titles", MainPanelTab.MORE, "👑", "Titles", 
           "more-titles.hyuiml", 3, false),
    
    /**
     * Collections page showing collected items/sets.
     */
    COLLECTIONS("collections", MainPanelTab.MORE, "📚", "Collections", 
                "more-collections.hyuiml", 4, false),
    
    /**
     * PvP page showing PvP stats and rankings.
     */
    PVP("pvp", MainPanelTab.MORE, "⚔️", "PvP", 
        "more-pvp.hyuiml", 5, false);
    
    private final String id;
    private final MainPanelTab parentTab;
    private final String icon;
    private final String displayName;
    private final String templateFile;
    private final int sortOrder;
    private final boolean adminOnly;
    
    SubPage(String id, MainPanelTab parentTab, String icon, String displayName,
            String templateFile, int sortOrder, boolean adminOnly) {
        this.id = id;
        this.parentTab = parentTab;
        this.icon = icon;
        this.displayName = displayName;
        this.templateFile = templateFile;
        this.sortOrder = sortOrder;
        this.adminOnly = adminOnly;
    }
    
    /**
     * Gets the unique identifier for this sub-page.
     * @return sub-page ID (e.g., "equipment", "quest-book")
     */
    public String id() {
        return id;
    }
    
    /**
     * Gets the parent tab that contains this sub-page.
     * @return parent tab
     */
    public MainPanelTab parentTab() {
        return parentTab;
    }
    
    /**
     * Gets the icon/emoji for this sub-page.
     * @return icon string
     */
    public String icon() {
        return icon;
    }
    
    /**
     * Gets the display name shown in the UI.
     * @return localized display name
     */
    public String displayName() {
        return displayName;
    }
    
    /**
     * Gets the HYUIML template file name for this sub-page.
     * @return template file name (relative to ui/pages/tabs/)
     */
    public String templateFile() {
        return templateFile;
    }
    
    /**
     * Gets the sort order within the parent tab.
     * @return sort order (0-based)
     */
    public int sortOrder() {
        return sortOrder;
    }
    
    /**
     * Checks if this sub-page requires admin permissions.
     * @return true if admin-only
     */
    public boolean isAdminOnly() {
        return adminOnly;
    }
    
    /**
     * Finds a sub-page by its ID.
     * 
     * @param id the sub-page ID (e.g., "equipment", "guild")
     * @return Optional containing the matching sub-page, or empty if no match
     */
    public static Optional<SubPage> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (SubPage page : values()) {
            if (page.id.equals(id)) {
                return Optional.of(page);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Finds a sub-page by parent tab and ID.
     * 
     * @param tab the parent tab
     * @param id the sub-page ID
     * @return Optional containing the matching sub-page, or empty if no match
     */
    public static Optional<SubPage> fromTabAndId(MainPanelTab tab, String id) {
        if (tab == null || id == null) {
            return Optional.empty();
        }
        for (SubPage page : values()) {
            if (page.parentTab == tab && page.id.equals(id)) {
                return Optional.of(page);
            }
        }
        return Optional.empty();
    }
}
