package com.argonathsystems.framework.ui.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages keybind hints displayed in the bottom-left corner of the screen.
 * 
 * <p>This HUD element shows contextual keybind hints like:
 * <ul>
 *   <li>[E] Interact</li>
 *   <li>[M] Map</li>
 *   <li>[I] Inventory</li>
 *   <li>[J] Quest Log</li>
 * </ul>
 * 
 * <p>Hints can be dynamically added/removed based on game context
 * (e.g., "Press E to interact" only shows when near an interactable).
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 */
public class KeybindHintsHUD {
    
    /** Singleton instance */
    private static final KeybindHintsHUD INSTANCE = new KeybindHintsHUD();
    
    /** Active hints by category */
    private final Map<String, List<KeybindHint>> hintsByCategory = new ConcurrentHashMap<>();
    
    /** Persistent hints always shown */
    private final List<KeybindHint> persistentHints = new ArrayList<>();
    
    /** Contextual hints shown based on game state */
    private final Map<String, KeybindHint> contextualHints = new ConcurrentHashMap<>();
    
    private KeybindHintsHUD() {
        // Initialize with default persistent hints
        initializeDefaultHints();
    }
    
    public static KeybindHintsHUD getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize default keybind hints that are always visible.
     */
    private void initializeDefaultHints() {
        // Menu keybinds
        addPersistentHint(new KeybindHint("M", "Map", "menu", 10));
        addPersistentHint(new KeybindHint("I", "Inventory", "menu", 20));
        addPersistentHint(new KeybindHint("J", "Quests", "menu", 30));
        addPersistentHint(new KeybindHint("K", "Skills", "menu", 40));
        addPersistentHint(new KeybindHint("G", "Guild", "menu", 50));
        addPersistentHint(new KeybindHint("P", "Party", "menu", 60));
    }
    
    /**
     * Add a persistent hint that is always shown.
     */
    public void addPersistentHint(KeybindHint hint) {
        persistentHints.add(hint);
        persistentHints.sort((a, b) -> Integer.compare(a.priority(), b.priority()));
    }
    
    /**
     * Remove a persistent hint.
     */
    public void removePersistentHint(String key) {
        persistentHints.removeIf(h -> h.key().equals(key));
    }
    
    /**
     * Set a contextual hint (e.g., "Press E to interact").
     * Contextual hints are shown based on game state and auto-removed.
     * 
     * @param id Unique identifier for this contextual hint
     * @param hint The hint to display
     */
    public void setContextualHint(String id, KeybindHint hint) {
        contextualHints.put(id, hint);
    }
    
    /**
     * Remove a contextual hint.
     * 
     * @param id The identifier of the hint to remove
     */
    public void removeContextualHint(String id) {
        contextualHints.remove(id);
    }
    
    /**
     * Clear all contextual hints.
     */
    public void clearContextualHints() {
        contextualHints.clear();
    }
    
    /**
     * Get all currently visible hints (persistent + contextual).
     * 
     * @return List of hints sorted by priority
     */
    public List<KeybindHint> getVisibleHints() {
        List<KeybindHint> allHints = new ArrayList<>();
        
        // Add contextual hints first (higher priority)
        allHints.addAll(contextualHints.values());
        
        // Add persistent hints
        allHints.addAll(persistentHints);
        
        // Sort by priority
        allHints.sort((a, b) -> Integer.compare(a.priority(), b.priority()));
        
        return allHints;
    }
    
    /**
     * Generate the HYUIML markup for the keybind hints HUD.
     * 
     * @return HYUIML string for the HUD element
     */
    public String generateHyuiml() {
        StringBuilder html = new StringBuilder();
        
        // Styles
        html.append("<style>");
        html.append(".keybind-hints-container { ");
        html.append("layout-mode: Top; ");
        html.append("anchor-left: 20; ");
        html.append("anchor-bottom: 120; ");
        html.append("anchor-width: 200; ");
        html.append("} ");
        
        html.append(".hint-row { ");
        html.append("layout-mode: Left; ");
        html.append("anchor-height: 24; ");
        html.append("margin-bottom: 4; ");
        html.append("align-items: center; ");
        html.append("} ");
        
        html.append(".hint-key { ");
        html.append("anchor-width: 28; ");
        html.append("anchor-height: 22; ");
        html.append("background-color: #2A2A2A; ");
        html.append("border: 1px solid #666666; ");
        html.append("border-radius: 4; ");
        html.append("layout-mode: Center; ");
        html.append("margin-right: 8; ");
        html.append("} ");
        
        html.append(".hint-key-text { ");
        html.append("color: #FFDD00; ");
        html.append("font-size: 12; ");
        html.append("font-weight: bold; ");
        html.append("} ");
        
        html.append(".hint-action { ");
        html.append("color: #CCCCCC; ");
        html.append("font-size: 14; ");
        html.append("} ");
        
        html.append(".hint-contextual .hint-key { ");
        html.append("border-color: #FFAA00; ");
        html.append("background-color: #3A2A1A; ");
        html.append("} ");
        
        html.append(".hint-separator { ");
        html.append("anchor-height: 1; ");
        html.append("background-color: #444444; ");
        html.append("margin-top: 4; ");
        html.append("margin-bottom: 8; ");
        html.append("} ");
        
        html.append("</style>");
        
        // Container
        html.append("<div id=\"keybind-hints\" class=\"keybind-hints-container\">");
        
        // Contextual hints (if any)
        List<KeybindHint> contextual = new ArrayList<>(contextualHints.values());
        if (!contextual.isEmpty()) {
            for (KeybindHint hint : contextual) {
                html.append(generateHintRow(hint, true));
            }
            // Separator between contextual and persistent
            html.append("<div class=\"hint-separator\"></div>");
        }
        
        // Persistent hints (menu keybinds)
        for (KeybindHint hint : persistentHints) {
            html.append(generateHintRow(hint, false));
        }
        
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Generate a single hint row.
     */
    private String generateHintRow(KeybindHint hint, boolean isContextual) {
        StringBuilder row = new StringBuilder();
        String contextClass = isContextual ? " hint-contextual" : "";
        
        row.append("<div class=\"hint-row").append(contextClass).append("\">");
        row.append("<div class=\"hint-key\">");
        row.append("<p class=\"hint-key-text\">").append(hint.key()).append("</p>");
        row.append("</div>");
        row.append("<p class=\"hint-action\">").append(hint.action()).append("</p>");
        row.append("</div>");
        
        return row.toString();
    }
    
    /**
     * A single keybind hint entry.
     * 
     * @param key The key to press (e.g., "E", "M", "Ctrl+I")
     * @param action The action description (e.g., "Interact", "Open Map")
     * @param category Category for grouping (e.g., "menu", "interaction", "combat")
     * @param priority Display priority (lower = higher on screen)
     */
    public record KeybindHint(String key, String action, String category, int priority) {
        
        /**
         * Create a simple hint with default priority.
         */
        public static KeybindHint of(String key, String action) {
            return new KeybindHint(key, action, "default", 100);
        }
        
        /**
         * Create an interaction hint (shown at top).
         */
        public static KeybindHint interaction(String key, String action) {
            return new KeybindHint(key, action, "interaction", 0);
        }
    }
}
