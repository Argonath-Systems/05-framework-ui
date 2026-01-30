package com.argonathsystems.framework.ui.combat;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an action slot in the action bar.
 * 
 * <p>Each slot can contain an ability, item, or macro with:
 * <ul>
 *   <li>Icon for display</li>
 *   <li>Cooldown tracking</li>
 *   <li>Keybind display</li>
 *   <li>Usable state</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
public class ActionSlot {
    
    private final int index;
    private final String actionId;
    private final String name;
    private final String iconUrl;
    private final String keybind;
    private final boolean onCooldown;
    private final float cooldownProgress; // 0.0 to 1.0
    private final float cooldownRemaining; // seconds
    private final boolean usable;
    private final int charges;
    private final int maxCharges;
    
    private ActionSlot(Builder builder) {
        this.index = builder.index;
        this.actionId = builder.actionId;
        this.name = builder.name;
        this.iconUrl = builder.iconUrl;
        this.keybind = builder.keybind;
        this.onCooldown = builder.onCooldown;
        this.cooldownProgress = builder.cooldownProgress;
        this.cooldownRemaining = builder.cooldownRemaining;
        this.usable = builder.usable;
        this.charges = builder.charges;
        this.maxCharges = builder.maxCharges;
    }
    
    /**
     * Get the slot index (1-12).
     * 
     * @return Slot index
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Get the action ID (ability/item/macro ID).
     * 
     * @return Action ID, or null if empty slot
     */
    public String getActionId() {
        return actionId;
    }
    
    /**
     * Get the display name.
     * 
     * @return Action name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the icon URL.
     * 
     * @return Icon resource path
     */
    public String getIconUrl() {
        return iconUrl;
    }
    
    /**
     * Get the keybind display text.
     * 
     * @return Keybind (e.g., "1", "F1", "Shift+1")
     */
    public String getKeybind() {
        return keybind;
    }
    
    /**
     * Check if the action is on cooldown.
     * 
     * @return true if cooling down
     */
    public boolean isOnCooldown() {
        return onCooldown;
    }
    
    /**
     * Get the cooldown progress (0.0 to 1.0).
     * 
     * @return Progress where 0 = just started, 1 = complete
     */
    public float getCooldownProgress() {
        return cooldownProgress;
    }
    
    /**
     * Get remaining cooldown time in seconds.
     * 
     * @return Remaining seconds
     */
    public float getCooldownRemaining() {
        return cooldownRemaining;
    }
    
    /**
     * Check if the action is currently usable.
     * 
     * @return true if can be activated
     */
    public boolean isUsable() {
        return usable;
    }
    
    /**
     * Get current charge count.
     * 
     * @return Number of charges available
     */
    public int getCharges() {
        return charges;
    }
    
    /**
     * Get maximum charge count.
     * 
     * @return Maximum charges
     */
    public int getMaxCharges() {
        return maxCharges;
    }
    
    /**
     * Check if this is an empty slot.
     * 
     * @return true if no action assigned
     */
    public boolean isEmpty() {
        return actionId == null || actionId.isEmpty();
    }
    
    /**
     * Convert to a map for template processing.
     * 
     * @return Map of template variables
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("index", index);
        map.put("actionId", actionId != null ? actionId : "");
        map.put("name", name != null ? name : "");
        map.put("iconUrl", iconUrl != null ? iconUrl : "");
        map.put("keybind", keybind != null ? keybind : String.valueOf(index));
        map.put("empty", isEmpty());
        map.put("onCooldown", onCooldown);
        map.put("cooldownProgress", cooldownProgress);
        map.put("cooldownPercent", Math.round(cooldownProgress * 100));
        map.put("cooldownRemaining", cooldownRemaining);
        map.put("usable", usable);
        map.put("charges", charges);
        map.put("maxCharges", maxCharges);
        map.put("hasCharges", maxCharges > 1);
        return map;
    }
    
    /**
     * Create an empty slot.
     * 
     * @param index slot index
     * @return empty ActionSlot
     */
    public static ActionSlot empty(int index) {
        return new Builder(index).build();
    }
    
    /**
     * Create a new builder.
     * 
     * @param index slot index (1-12)
     * @return a new builder instance
     */
    public static Builder builder(int index) {
        return new Builder(index);
    }
    
    /**
     * Builder for ActionSlot.
     */
    public static class Builder {
        private final int index;
        private String actionId;
        private String name;
        private String iconUrl;
        private String keybind;
        private boolean onCooldown = false;
        private float cooldownProgress = 1.0f;
        private float cooldownRemaining = 0f;
        private boolean usable = true;
        private int charges = 0;
        private int maxCharges = 0;
        
        private Builder(int index) {
            this.index = index;
            this.keybind = String.valueOf(index <= 10 ? index % 10 : index);
        }
        
        /**
         * Set the action ID.
         * 
         * @param actionId ability/item/macro ID
         * @return this builder
         */
        public Builder withAction(String actionId) {
            this.actionId = actionId;
            return this;
        }
        
        /**
         * Set the display name.
         * 
         * @param name action name
         * @return this builder
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        /**
         * Set the icon URL.
         * 
         * @param iconUrl icon resource path
         * @return this builder
         */
        public Builder withIcon(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }
        
        /**
         * Set the keybind display.
         * 
         * @param keybind keybind text
         * @return this builder
         */
        public Builder withKeybind(String keybind) {
            this.keybind = keybind;
            return this;
        }
        
        /**
         * Set cooldown state.
         * 
         * @param progress 0.0 (just started) to 1.0 (complete)
         * @param remaining seconds remaining
         * @return this builder
         */
        public Builder withCooldown(float progress, float remaining) {
            this.onCooldown = progress < 1.0f;
            this.cooldownProgress = progress;
            this.cooldownRemaining = remaining;
            return this;
        }
        
        /**
         * Set usability state.
         * 
         * @param usable true if can be activated
         * @return this builder
         */
        public Builder withUsable(boolean usable) {
            this.usable = usable;
            return this;
        }
        
        /**
         * Set charge counts.
         * 
         * @param current current charges
         * @param max maximum charges
         * @return this builder
         */
        public Builder withCharges(int current, int max) {
            this.charges = current;
            this.maxCharges = max;
            return this;
        }
        
        /**
         * Build the ActionSlot.
         * 
         * @return the built ActionSlot
         */
        public ActionSlot build() {
            return new ActionSlot(this);
        }
    }
}
