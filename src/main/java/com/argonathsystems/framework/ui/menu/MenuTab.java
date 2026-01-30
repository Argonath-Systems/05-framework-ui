package com.argonathsystems.framework.ui.menu;

import java.util.Optional;

/**
 * Defines a tab in the main menu.
 * Each tab corresponds to a major game system.
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.0.0
 */
public enum MenuTab {
    // Primary Tabs
    CHARACTER("character", "Character", "C", "icon-character.png", 0),
    INVENTORY("inventory", "Inventory", "I", "icon-inventory.png", 1),
    SKILLS("skills", "Skills", "K", "icon-skills.png", 2),
    QUESTS("quests", "Quests", "J", "icon-quests.png", 3),
    MOUNTS("mounts", "Mounts", "H", "icon-mounts.png", 4),
    
    // Social Tabs
    GUILD("guild", "Guild", "G", "icon-guild.png", 10),
    SOCIAL("social", "Social", "O", "icon-social.png", 11),
    PARTY("party", "Party", "P", "icon-party.png", 12),
    
    // Systems Tabs
    CRAFTING("crafting", "Crafting", "N", "icon-crafting.png", 20),
    FACTIONS("factions", "Factions", "F", "icon-factions.png", 21),
    MARKETPLACE("marketplace", "Marketplace", "B", "icon-marketplace.png", 22),
    MAP("map", "Map", "M", "icon-map.png", 23),
    
    // Collections
    ACHIEVEMENTS("achievements", "Achievements", "Y", "icon-achievements.png", 30),
    COLLECTIONS("collections", "Collections", "U", "icon-collections.png", 31),
    
    // Meta
    SETTINGS("settings", "Settings", null, "icon-settings.png", 100),
    HELP("help", "Help", null, "icon-help.png", 101);
    
    private final String id;
    private final String displayName;
    private final String hotkey;
    private final String iconPath;
    private final int sortOrder;
    
    MenuTab(String id, String displayName, String hotkey, String iconPath, int sortOrder) {
        this.id = id;
        this.displayName = displayName;
        this.hotkey = hotkey;
        this.iconPath = iconPath;
        this.sortOrder = sortOrder;
    }
    
    public String id() { return id; }
    public String displayName() { return displayName; }
    public String hotkey() { return hotkey; }
    public String iconPath() { return iconPath; }
    public int sortOrder() { return sortOrder; }
    
    /**
     * Find a tab by its hotkey.
     * 
     * @param key The key pressed (e.g., "G", "M", "J")
     * @return Optional containing the matching tab, or empty if no match
     */
    public static Optional<MenuTab> fromHotkey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        String upperKey = key.toUpperCase();
        for (MenuTab tab : values()) {
            if (upperKey.equals(tab.hotkey)) {
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find a tab by its ID.
     * 
     * @param id The tab ID (e.g., "character", "quests")
     * @return Optional containing the matching tab, or empty if no match
     */
    public static Optional<MenuTab> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (MenuTab tab : values()) {
            if (tab.id.equals(id)) {
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }
}
