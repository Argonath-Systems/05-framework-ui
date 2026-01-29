package com.argonathsystems.framework.ui.dialogue;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single dialogue choice option in the NPC dialogue UI.
 * 
 * <p>Each choice has:
 * <ul>
 *   <li>An index (1-9) for keyboard shortcuts
 *   <li>Display text shown to the player
 *   <li>A unique ID for identifying the choice when selected
 *   <li>An availability flag (disabled choices are grayed out)
 *   <li>An optional type label (e.g., "Quest", "Lore", "Shop")
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public class DialogueChoice {
    
    private final int index;
    private final String text;
    private final String id;
    private final boolean available;
    private final String type;
    
    /**
     * Create a new dialogue choice.
     * 
     * @param index Choice number (1-9)
     * @param text Display text
     * @param id Unique choice identifier
     * @param available Whether this choice is selectable
     * @param type Optional type label (can be null)
     */
    public DialogueChoice(int index, String text, String id, boolean available, String type) {
        if (index < 1 || index > 9) {
            throw new IllegalArgumentException("Choice index must be between 1 and 9, got: " + index);
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Choice text cannot be null or empty");
        }
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Choice ID cannot be null or empty");
        }
        
        this.index = index;
        this.text = text;
        this.id = id;
        this.available = available;
        this.type = type;
    }
    
    /**
     * Get the choice index (for keyboard shortcuts).
     * 
     * @return Choice index (1-9)
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Get the display text.
     * 
     * @return Choice text
     */
    public String getText() {
        return text;
    }
    
    /**
     * Get the unique choice identifier.
     * 
     * @return Choice ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Check if this choice is available (selectable).
     * 
     * @return true if available
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Get the optional type label.
     * 
     * @return Type label or null
     */
    public String getType() {
        return type;
    }
    
    /**
     * Convert this choice to a map for template processing.
     * 
     * @return Map of choice properties
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("index", index);
        map.put("text", text);
        map.put("id", id);
        map.put("available", available);
        map.put("type", type);
        return map;
    }
    
    @Override
    public String toString() {
        return String.format("DialogueChoice[%d] \"%s\" (id=%s, available=%b, type=%s)", 
                             index, text, id, available, type);
    }
}
