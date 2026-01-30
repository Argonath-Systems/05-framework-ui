package com.argonathsystems.framework.ui.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Platform-agnostic data builder for Combat Frames HUD using HyUIML.
 * 
 * <p>This class builds the data model for combat unit frames. It does NOT process
 * templates or render UIs directly - that is handled by the adapter layer using
 * HyUI's {@code TemplateProcessor} and {@code HudBuilder}.
 * 
 * <h2>Architecture</h2>
 * <pre>{@code
 *   CombatFramesBuilder (this class)
 *          ↓ builds data
 *   Map<String, Object> templateVariables
 *          ↓ passed to
 *   02-adapter-hytale/CombatFramesAdapter
 *          ↓ uses HyUI
 *   TemplateProcessor → HudBuilder → Player HUD
 * }</pre>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Player frame (top-left)</li>
 *   <li>Target frame (top-center)</li>
 *   <li>Target-of-target frame</li>
 *   <li>Party/raid frames</li>
 *   <li>Buffs and debuffs display</li>
 *   <li>Hot reload support via template supplier</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @see UnitFrame
 * @since 1.1.0
 */
public class CombatFramesBuilder {
    
    private UnitFrame playerFrame;
    private UnitFrame targetFrame;
    private UnitFrame targetOfTargetFrame;
    private final List<UnitFrame> partyFrames;
    private boolean showPartyFrames = true;
    private boolean showTargetOfTarget = true;
    private Supplier<String> templateSupplier;
    
    /**
     * Create a new combat frames builder.
     */
    public CombatFramesBuilder() {
        this.partyFrames = new ArrayList<>();
    }
    
    /**
     * Set the template supplier for hot reload support.
     * 
     * @param templateSupplier Supplier providing the base HyUIML template
     * @return this builder
     */
    public CombatFramesBuilder setTemplateSupplier(Supplier<String> templateSupplier) {
        this.templateSupplier = templateSupplier;
        return this;
    }
    
    /**
     * Set the player's unit frame.
     * 
     * @param frame player unit frame
     * @return this builder
     */
    public CombatFramesBuilder setPlayerFrame(UnitFrame frame) {
        this.playerFrame = frame;
        return this;
    }
    
    /**
     * Set the target's unit frame.
     * 
     * @param frame target unit frame, or null to clear
     * @return this builder
     */
    public CombatFramesBuilder setTargetFrame(UnitFrame frame) {
        this.targetFrame = frame;
        return this;
    }
    
    /**
     * Set the target-of-target frame.
     * 
     * @param frame target-of-target frame, or null to clear
     * @return this builder
     */
    public CombatFramesBuilder setTargetOfTargetFrame(UnitFrame frame) {
        this.targetOfTargetFrame = frame;
        return this;
    }
    
    /**
     * Add a party member frame.
     * 
     * @param frame party member unit frame
     * @return this builder
     */
    public CombatFramesBuilder addPartyMember(UnitFrame frame) {
        this.partyFrames.add(frame);
        return this;
    }
    
    /**
     * Clear all party member frames.
     * 
     * @return this builder
     */
    public CombatFramesBuilder clearPartyMembers() {
        this.partyFrames.clear();
        return this;
    }
    
    /**
     * Set whether to show party frames.
     * 
     * @param show true to show party frames
     * @return this builder
     */
    public CombatFramesBuilder setShowPartyFrames(boolean show) {
        this.showPartyFrames = show;
        return this;
    }
    
    /**
     * Set whether to show target-of-target.
     * 
     * @param show true to show ToT frame
     * @return this builder
     */
    public CombatFramesBuilder setShowTargetOfTarget(boolean show) {
        this.showTargetOfTarget = show;
        return this;
    }
    
    /**
     * Build the template variables map for HyUI TemplateProcessor.
     * 
     * @return Map of template variables for TemplateProcessor
     */
    public Map<String, Object> buildTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        // Player frame
        if (playerFrame != null) {
            variables.put("player", playerFrame.toMap());
            variables.put("hasPlayer", true);
        } else {
            variables.put("hasPlayer", false);
        }
        
        // Target frame
        if (targetFrame != null) {
            variables.put("target", targetFrame.toMap());
            variables.put("hasTarget", true);
        } else {
            variables.put("hasTarget", false);
        }
        
        // Target-of-target
        if (showTargetOfTarget && targetOfTargetFrame != null) {
            variables.put("targetOfTarget", targetOfTargetFrame.toMap());
            variables.put("hasTargetOfTarget", true);
        } else {
            variables.put("hasTargetOfTarget", false);
        }
        variables.put("showTargetOfTarget", showTargetOfTarget);
        
        // Party frames
        if (showPartyFrames && !partyFrames.isEmpty()) {
            List<Map<String, Object>> partyList = new ArrayList<>();
            for (UnitFrame frame : partyFrames) {
                partyList.add(frame.toMap());
            }
            variables.put("partyMembers", partyList);
            variables.put("hasParty", true);
            variables.put("partySize", partyFrames.size());
        } else {
            variables.put("partyMembers", List.of());
            variables.put("hasParty", false);
            variables.put("partySize", 0);
        }
        variables.put("showPartyFrames", showPartyFrames);
        
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
    
    public UnitFrame getPlayerFrame() {
        return playerFrame;
    }
    
    public UnitFrame getTargetFrame() {
        return targetFrame;
    }
    
    public UnitFrame getTargetOfTargetFrame() {
        return targetOfTargetFrame;
    }
    
    public List<UnitFrame> getPartyFrames() {
        return List.copyOf(partyFrames);
    }
    
    public boolean isShowPartyFrames() {
        return showPartyFrames;
    }
    
    public boolean isShowTargetOfTarget() {
        return showTargetOfTarget;
    }
}
