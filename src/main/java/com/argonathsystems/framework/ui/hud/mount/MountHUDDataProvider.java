package com.argonathsystems.framework.ui.hud.mount;

import java.util.Map;

/**
 * Interface for providing mount HUD data to UI adapters.
 * 
 * <p>This interface enables proper dependency inversion - the adapter layer
 * depends on this framework interface, while mod layer implementations provide
 * the concrete data.
 * 
 * <h2>Dependency Flow</h2>
 * <pre>
 * MountHUDBuilder (mod) → implements → MountHUDDataProvider (framework) ← imports ← MountHUDAdapter (adapter)
 * </pre>
 * 
 * <h2>Template Variables</h2>
 * <p>Implementations should provide these template variables:
 * <ul>
 *   <li>{@code mountName} - String: Display name of the mount</li>
 *   <li>{@code mountType} - String: Type icon identifier (e.g., "horse", "dragon")</li>
 *   <li>{@code staminaCurrent} - Double: Current stamina value</li>
 *   <li>{@code staminaMax} - Double: Maximum stamina value</li>
 *   <li>{@code staminaPercent} - Double: Stamina percentage (0-100)</li>
 *   <li>{@code isFlying} - Boolean: Whether mount is currently flying</li>
 *   <li>{@code altitudeCurrent} - Double: Current altitude (flying mounts only)</li>
 *   <li>{@code altitudeMax} - Double: Maximum altitude (flying mounts only)</li>
 *   <li>{@code speedBonus} - Integer: Speed bonus percentage</li>
 *   <li>{@code level} - Integer: Mount's current level</li>
 *   <li>{@code abilities} - List&lt;Map&gt;: Mount abilities (each with key, name, cooldown)</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="VDD-MISC-011">VDD-MISC-011: Mount HUD</a>
 */
public interface MountHUDDataProvider {
    
    /**
     * Builds template variables for the mount HUD.
     * 
     * <p>The returned map is passed to HyUI's TemplateProcessor for
     * variable interpolation in the mount-hud.hyuiml template.
     * 
     * @return map of template variable names to values
     */
    Map<String, Object> buildTemplateVariables();
    
    /**
     * Checks if this provider has a custom template supplier.
     * 
     * <p>Used for hot-reload development mode where templates
     * are loaded from external sources instead of classpath.
     * 
     * @return true if a custom template is available
     */
    default boolean hasTemplateSupplier() {
        return false;
    }
    
    /**
     * Gets the custom template content.
     * 
     * <p>Only called if {@link #hasTemplateSupplier()} returns true.
     * 
     * @return the HyUIML template content
     * @throws UnsupportedOperationException if no custom template is available
     */
    default String getTemplate() {
        throw new UnsupportedOperationException("No custom template supplier configured");
    }
}
