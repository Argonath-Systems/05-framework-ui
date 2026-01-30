package com.argonathsystems.framework.ui.menu.mount;

import java.util.Map;

/**
 * Interface for providing mount collection page data to UI adapters.
 * 
 * <p>This interface enables proper dependency inversion - the adapter layer
 * depends on this framework interface, while mod layer implementations provide
 * the concrete data.
 * 
 * <h2>Dependency Flow</h2>
 * <pre>
 * MountCollectionPageBuilder (mod) → implements → MountCollectionDataProvider (framework) ← imports ← MountCollectionPageAdapter (adapter)
 * </pre>
 * 
 * <h2>Template Variables</h2>
 * <p>Implementations should provide these template variables:
 * <ul>
 *   <li>{@code allMounts} - List&lt;Map&gt;: All available mounts (owned, locked, hidden)</li>
 *   <li>{@code ownedMounts} - List&lt;Map&gt;: Mounts owned by player</li>
 *   <li>{@code categories} - List&lt;String&gt;: Available categories (all, ground, flying, aquatic, exotic)</li>
 *   <li>{@code selectedCategory} - String: Currently selected category</li>
 *   <li>{@code selectedMount} - Map: Currently selected mount for detail panel</li>
 *   <li>{@code searchQuery} - String: Current search query</li>
 *   <li>{@code sortMode} - String: Current sort mode (name, level, rarity, type)</li>
 *   <li>{@code totalCount} - Integer: Total mount count</li>
 *   <li>{@code ownedCount} - Integer: Owned mount count</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="VDD-MISC-029">VDD-MISC-029: Mount Collection Page</a>
 */
public interface MountCollectionDataProvider {
    
    /**
     * Builds template variables for the mount collection page.
     * 
     * <p>The returned map is passed to HyUI's TemplateProcessor for
     * variable interpolation in the mount-collection.hyuiml template.
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
