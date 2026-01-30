package com.argonathsystems.framework.ui.combat;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a resource bar (health, mana, energy, etc).
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
public class ResourceBar {
    
    private final String type;
    private final int current;
    private final int maximum;
    private final String color;
    
    /**
     * Create a new resource bar.
     * 
     * @param type resource type (health, mana, energy, etc.)
     * @param current current value
     * @param maximum maximum value
     */
    public ResourceBar(String type, int current, int maximum) {
        this(type, current, maximum, getDefaultColor(type));
    }
    
    /**
     * Create a new resource bar with custom color.
     * 
     * @param type resource type
     * @param current current value
     * @param maximum maximum value
     * @param color CSS color value
     */
    public ResourceBar(String type, int current, int maximum, String color) {
        this.type = type;
        this.current = current;
        this.maximum = maximum;
        this.color = color;
    }
    
    private static String getDefaultColor(String type) {
        return switch (type.toLowerCase()) {
            case "health" -> "#dc143c";
            case "mana" -> "#4169e1";
            case "energy" -> "#ffd700";
            case "rage" -> "#ff4500";
            case "focus" -> "#32cd32";
            default -> "#808080";
        };
    }
    
    /**
     * Get the resource type.
     * 
     * @return Resource type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get the current value.
     * 
     * @return Current value
     */
    public int getCurrent() {
        return current;
    }
    
    /**
     * Get the maximum value.
     * 
     * @return Maximum value
     */
    public int getMaximum() {
        return maximum;
    }
    
    /**
     * Get the fill percentage (0-100).
     * 
     * @return Fill percentage
     */
    public int getPercentage() {
        return maximum > 0 ? (current * 100) / maximum : 0;
    }
    
    /**
     * Get the CSS color value.
     * 
     * @return CSS color
     */
    public String getColor() {
        return color;
    }
    
    /**
     * Convert to a map for template processing.
     * 
     * @return Map of template variables
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("current", current);
        map.put("maximum", maximum);
        map.put("percentage", getPercentage());
        map.put("color", color);
        map.put("text", current + "/" + maximum);
        return map;
    }
    
    /**
     * Create a health bar.
     * 
     * @param current current HP
     * @param maximum max HP
     * @return health ResourceBar
     */
    public static ResourceBar health(int current, int maximum) {
        return new ResourceBar("health", current, maximum);
    }
    
    /**
     * Create a mana bar.
     * 
     * @param current current mana
     * @param maximum max mana
     * @return mana ResourceBar
     */
    public static ResourceBar mana(int current, int maximum) {
        return new ResourceBar("mana", current, maximum);
    }
    
    /**
     * Create an energy bar.
     * 
     * @param current current energy
     * @param maximum max energy
     * @return energy ResourceBar
     */
    public static ResourceBar energy(int current, int maximum) {
        return new ResourceBar("energy", current, maximum);
    }
}
