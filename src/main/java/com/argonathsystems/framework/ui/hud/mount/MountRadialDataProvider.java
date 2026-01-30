package com.argonathsystems.framework.ui.hud.mount;

import java.util.Map;

/**
 * Interface for providing mount summon radial menu data to UI adapters.
 * 
 * <p>This interface enables proper dependency inversion - the adapter layer
 * depends on this framework interface, while mod layer implementations provide
 * the concrete data.
 * 
 * <h2>Dependency Flow</h2>
 * <pre>
 * MountSummonRadialBuilder (mod) → implements → MountRadialDataProvider (framework) ← imports ← MountSummonRadialAdapter (adapter)
 * </pre>
 * 
 * <h2>Template Variables</h2>
 * <p>Implementations should provide these template variables:
 * <ul>
 *   <li>{@code favorites} - List&lt;Map&gt;: Up to 8 favorite mounts (each with id, name, icon, sector)</li>
 *   <li>{@code selectedSector} - Integer: Currently selected sector (0-7, -1 for none)</li>
 *   <li>{@code centerText} - String: Text displayed in center of radial menu</li>
 *   <li>{@code keybind} - String: The keybind to open/close radial (e.g., "G")</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="VDD-MISC-012">VDD-MISC-012: Mount Summon Radial</a>
 */
public interface MountRadialDataProvider {
    
    /**
     * Builds template variables for the mount summon radial menu.
     * 
     * <p>The returned map is passed to HyUI's TemplateProcessor for
     * variable interpolation in the mount-summon-radial.hyuiml template.
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
