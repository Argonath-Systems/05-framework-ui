package com.argonathsystems.framework.ui.menu;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Defines the 8 top-level tabs in the Main Character Panel.
 * Each tab contains one or more {@link SubPage} entries accessible via dropdown.
 * 
 * <p>This enum aligns with specification VDD-LAYOUT-003 which defines:
 * <ul>
 *   <li>Inventory (I) - Equipment, Bags, Currency, Cosmetics</li>
 *   <li>Quest (J) - Quest Book, Quest Designer</li>
 *   <li>Social (O) - Friends &amp; Party, Guild, Housing</li>
 *   <li>Faction (U) - Reputation, War Overview</li>
 *   <li>Map (M) - World Map, Zone Map, Discovered Locations</li>
 *   <li>Work (K) - Professions, Recipes, Gathering Log</li>
 *   <li>Dungeon (Shift+I) - Group Finder, Dungeon Journal, Raid Planner</li>
 *   <li>More - Achievements, Mounts, Pets, Titles, Collections, PvP</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 2.0.0
 * @see SubPage
 * @see MainCharacterPanelManager
 */
public enum MainPanelTab {
    
    /**
     * Inventory tab containing equipment, bags, currency, and cosmetics.
     * Default hotkey: I
     */
    INVENTORY("inventory", "Inventory", "📦", "I", 0),
    
    /**
     * Quest tab containing quest book and quest designer (admin).
     * Default hotkey: J
     */
    QUEST("quest", "Quest", "📜", "J", 1),
    
    /**
     * Social tab containing friends, party, guild, and housing.
     * Default hotkey: O
     */
    SOCIAL("social", "Social", "👥", "O", 2),
    
    /**
     * Faction tab containing reputation and war overview.
     * Default hotkey: U
     */
    FACTION("faction", "Faction", "⚔️", "U", 3),
    
    /**
     * Map tab containing world map, zone map, and discovered locations.
     * Default hotkey: M
     */
    MAP("map", "Map", "🗺️", "M", 4),
    
    /**
     * Work tab containing professions, recipes, and gathering log.
     * Default hotkey: K
     */
    WORK("work", "Work", "⚒️", "K", 5),
    
    /**
     * Dungeon tab containing group finder, dungeon journal, and raid planner.
     * Default hotkey: Shift+I
     */
    DUNGEON("dungeon", "Dungeon", "🏰", "SHIFT_I", 6),
    
    /**
     * More tab containing achievements, mounts, pets, titles, collections, and PvP.
     * No default hotkey (accessed via tab bar only).
     */
    MORE("more", "More", "🏆", null, 7);
    
    private final String id;
    private final String displayName;
    private final String icon;
    private final String hotkey;
    private final int sortOrder;
    
    MainPanelTab(String id, String displayName, String icon, String hotkey, int sortOrder) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.hotkey = hotkey;
        this.sortOrder = sortOrder;
    }
    
    /**
     * Gets the unique identifier for this tab.
     * @return tab ID (e.g., "inventory", "quest")
     */
    public String id() {
        return id;
    }
    
    /**
     * Gets the display name shown in the UI.
     * @return localized display name
     */
    public String displayName() {
        return displayName;
    }
    
    /**
     * Gets the icon/emoji for this tab.
     * @return icon string (emoji or icon reference)
     */
    public String icon() {
        return icon;
    }
    
    /**
     * Gets the hotkey that opens the panel directly to this tab.
     * @return hotkey string (e.g., "I", "J", "SHIFT_I") or null if no hotkey
     */
    public String hotkey() {
        return hotkey;
    }
    
    /**
     * Gets the sort order for displaying tabs.
     * @return sort order (0-7)
     */
    public int sortOrder() {
        return sortOrder;
    }
    
    /**
     * Gets all sub-pages belonging to this tab.
     * @return unmodifiable list of sub-pages
     */
    public List<SubPage> getSubPages() {
        return Collections.unmodifiableList(
            Arrays.stream(SubPage.values())
                .filter(sp -> sp.parentTab() == this)
                .sorted((a, b) -> Integer.compare(a.sortOrder(), b.sortOrder()))
                .toList()
        );
    }
    
    /**
     * Gets the default sub-page for this tab (first in sort order).
     * @return the default sub-page
     */
    public SubPage getDefaultSubPage() {
        List<SubPage> subPages = getSubPages();
        if (subPages.isEmpty()) {
            throw new IllegalStateException("Tab " + id + " has no sub-pages defined");
        }
        return subPages.get(0);
    }
    
    /**
     * Finds a tab by its hotkey.
     * 
     * @param key the key pressed (e.g., "I", "J", "SHIFT_I")
     * @return Optional containing the matching tab, or empty if no match
     */
    public static Optional<MainPanelTab> fromHotkey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        String normalizedKey = key.toUpperCase().replace("+", "_").replace(" ", "_");
        for (MainPanelTab tab : values()) {
            if (normalizedKey.equals(tab.hotkey)) {
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Finds a tab by its ID.
     * 
     * @param id the tab ID (e.g., "inventory", "quest")
     * @return Optional containing the matching tab, or empty if no match
     */
    public static Optional<MainPanelTab> fromId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        for (MainPanelTab tab : values()) {
            if (tab.id.equals(id)) {
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Gets all tabs sorted by display order.
     * @return list of tabs in display order
     */
    public static List<MainPanelTab> getSortedTabs() {
        return Arrays.stream(values())
            .sorted((a, b) -> Integer.compare(a.sortOrder, b.sortOrder))
            .toList();
    }
}
