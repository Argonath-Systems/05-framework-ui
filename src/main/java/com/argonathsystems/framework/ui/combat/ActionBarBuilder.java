package com.argonathsystems.framework.ui.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Platform-agnostic data builder for Action Bar HUD using HyUIML.
 * 
 * <p>This class builds the data model for combat action bars. It does NOT process
 * templates or render UIs directly - that is handled by the adapter layer using
 * HyUI's {@code TemplateProcessor} and {@code HudBuilder}.
 * 
 * <h2>Architecture</h2>
 * <pre>{@code
 *   ActionBarBuilder (this class)
 *          ↓ builds data
 *   Map<String, Object> templateVariables
 *          ↓ passed to
 *   02-adapter-hytale/ActionBarAdapter
 *          ↓ uses HyUI
 *   TemplateProcessor → HudBuilder → Player HUD
 * }</pre>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>12 action slots with cooldowns and charges</li>
 *   <li>Health and resource bars</li>
 *   <li>Keybind display</li>
 *   <li>Hot reload support via template supplier</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @see ActionSlot
 * @see ResourceBar
 * @since 1.1.0
 */
public class ActionBarBuilder {
    
    private static final int SLOT_COUNT = 12;
    
    private final ActionSlot[] slots;
    private ResourceBar healthBar;
    private ResourceBar primaryResource;
    private ResourceBar secondaryResource;
    private boolean inCombat;
    private boolean showResourceBars = true;
    private Supplier<String> templateSupplier;
    
    /**
     * Create a new action bar builder with empty slots.
     */
    public ActionBarBuilder() {
        this.slots = new ActionSlot[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.slots[i] = ActionSlot.empty(i + 1);
        }
    }
    
    /**
     * Set the template supplier for hot reload support.
     * 
     * @param templateSupplier Supplier providing the base HyUIML template
     * @return this builder
     */
    public ActionBarBuilder setTemplateSupplier(Supplier<String> templateSupplier) {
        this.templateSupplier = templateSupplier;
        return this;
    }
    
    /**
     * Set an action slot.
     * 
     * @param index slot index (1-12)
     * @param slot the action slot
     * @return this builder
     */
    public ActionBarBuilder setSlot(int index, ActionSlot slot) {
        if (index >= 1 && index <= SLOT_COUNT) {
            this.slots[index - 1] = slot;
        }
        return this;
    }
    
    /**
     * Clear all slots.
     * 
     * @return this builder
     */
    public ActionBarBuilder clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.slots[i] = ActionSlot.empty(i + 1);
        }
        return this;
    }
    
    /**
     * Set the health bar.
     * 
     * @param current current HP
     * @param maximum max HP
     * @return this builder
     */
    public ActionBarBuilder setHealth(int current, int maximum) {
        this.healthBar = ResourceBar.health(current, maximum);
        return this;
    }
    
    /**
     * Set the primary resource bar (mana, energy, rage, etc).
     * 
     * @param type resource type
     * @param current current value
     * @param maximum max value
     * @return this builder
     */
    public ActionBarBuilder setPrimaryResource(String type, int current, int maximum) {
        this.primaryResource = new ResourceBar(type, current, maximum);
        return this;
    }
    
    /**
     * Set the secondary resource bar.
     * 
     * @param type resource type
     * @param current current value
     * @param maximum max value
     * @return this builder
     */
    public ActionBarBuilder setSecondaryResource(String type, int current, int maximum) {
        this.secondaryResource = new ResourceBar(type, current, maximum);
        return this;
    }
    
    /**
     * Set combat state.
     * 
     * @param inCombat true if player is in combat
     * @return this builder
     */
    public ActionBarBuilder setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
        return this;
    }
    
    /**
     * Set whether to show resource bars.
     * 
     * @param show true to show resource bars
     * @return this builder
     */
    public ActionBarBuilder setShowResourceBars(boolean show) {
        this.showResourceBars = show;
        return this;
    }
    
    /**
     * Build the template variables map for HyUI TemplateProcessor.
     * 
     * @return Map of template variables for TemplateProcessor
     */
    public Map<String, Object> buildTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        // Slots
        List<Map<String, Object>> slotsList = new ArrayList<>();
        for (ActionSlot slot : slots) {
            slotsList.add(slot.toMap());
        }
        variables.put("slots", slotsList);
        
        // Player stats / resource bars
        Map<String, Object> playerStats = new HashMap<>();
        if (healthBar != null) {
            playerStats.put("health", healthBar.toMap());
        }
        if (primaryResource != null) {
            playerStats.put("primary", primaryResource.toMap());
        }
        if (secondaryResource != null) {
            playerStats.put("secondary", secondaryResource.toMap());
        }
        variables.put("playerStats", playerStats);
        
        // State flags
        variables.put("inCombat", inCombat);
        variables.put("showResourceBars", showResourceBars);
        
        return variables;
    }
    
    /**
     * Get the raw template content from the supplier.
     * 
     * @return Raw HyUIML template string
     * @throws IllegalStateException if template supplier not set
     */
    public String getTemplate() {
        if (templateSupplier == null) {
            throw new IllegalStateException("Template supplier not set.");
        }
        return templateSupplier.get();
    }
    
    /**
     * Check if a template supplier has been set.
     * 
     * @return true if template supplier is configured
     */
    public boolean hasTemplateSupplier() {
        return templateSupplier != null;
    }
    
    // Getters
    
    public ActionSlot getSlot(int index) {
        if (index >= 1 && index <= SLOT_COUNT) {
            return slots[index - 1];
        }
        return null;
    }
    
    public ResourceBar getHealthBar() {
        return healthBar;
    }
    
    public ResourceBar getPrimaryResource() {
        return primaryResource;
    }
    
    public ResourceBar getSecondaryResource() {
        return secondaryResource;
    }
    
    public boolean isInCombat() {
        return inCombat;
    }
    
    public boolean isShowResourceBars() {
        return showResourceBars;
    }
}
