package com.argonathsystems.framework.ui.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Loads and caches HYUIML template files from resources.
 * 
 * <p>This class provides centralized template loading with caching support.
 * Templates are loaded from the classpath under the configured base path.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * TemplateLoader loader = new TemplateLoader("config/ui");
 * 
 * // Load a HUD template
 * Optional<String> template = loader.loadTemplate("huds/npc-dialogue.hyuiml");
 * 
 * // Load with caching enabled (default)
 * String cached = loader.loadTemplateOrThrow("pages/quest-book.hyuiml");
 * }</pre>
 * 
 * <h2>Template Locations</h2>
 * <ul>
 *   <li>HUDs: huds/*.hyuiml</li>
 *   <li>Pages: pages/*.hyuiml</li>
 *   <li>Components: components/*.hyuiml</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.1.0
 */
public class TemplateLoader {
    
    /** Default base path for templates in resources. */
    public static final String DEFAULT_BASE_PATH = "config/ui";
    
    /** Template file extension. */
    public static final String TEMPLATE_EXTENSION = ".hyuiml";
    
    private final String basePath;
    private final Map<String, String> templateCache;
    private final boolean cachingEnabled;
    
    /**
     * Creates a TemplateLoader with the default base path and caching enabled.
     */
    public TemplateLoader() {
        this(DEFAULT_BASE_PATH, true);
    }
    
    /**
     * Creates a TemplateLoader with a custom base path.
     * 
     * @param basePath the base path for templates (relative to resources)
     */
    public TemplateLoader(String basePath) {
        this(basePath, true);
    }
    
    /**
     * Creates a TemplateLoader with configurable caching.
     * 
     * @param basePath the base path for templates
     * @param cachingEnabled whether to cache loaded templates
     */
    public TemplateLoader(String basePath, boolean cachingEnabled) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        this.cachingEnabled = cachingEnabled;
        this.templateCache = cachingEnabled ? new ConcurrentHashMap<>() : new HashMap<>();
    }
    
    /**
     * Loads a template from the configured base path.
     * 
     * @param relativePath the path relative to the base path (e.g., "huds/npc-dialogue.hyuiml")
     * @return Optional containing the template content, or empty if not found
     */
    public Optional<String> loadTemplate(String relativePath) {
        String fullPath = basePath + relativePath;
        
        // Check cache first
        if (cachingEnabled && templateCache.containsKey(fullPath)) {
            return Optional.of(templateCache.get(fullPath));
        }
        
        // Load from resources
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fullPath)) {
            if (is == null) {
                return Optional.empty();
            }
            
            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
            
            // Cache if enabled
            if (cachingEnabled) {
                templateCache.put(fullPath, content);
            }
            
            return Optional.of(content);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Loads a template or throws an exception if not found.
     * 
     * @param relativePath the path relative to the base path
     * @return the template content
     * @throws TemplateNotFoundException if the template cannot be loaded
     */
    public String loadTemplateOrThrow(String relativePath) {
        return loadTemplate(relativePath)
            .orElseThrow(() -> new TemplateNotFoundException(basePath + relativePath));
    }
    
    /**
     * Loads a HUD template by name.
     * 
     * <p>Convenience method that automatically prepends "huds/" and appends ".hyuiml".
     * 
     * @param hudName the HUD name (e.g., "npc-dialogue")
     * @return Optional containing the template content
     */
    public Optional<String> loadHudTemplate(String hudName) {
        return loadTemplate("huds/" + hudName + TEMPLATE_EXTENSION);
    }
    
    /**
     * Loads a page template by name.
     * 
     * <p>Convenience method that automatically prepends "pages/" and appends ".hyuiml".
     * 
     * @param pageName the page name (e.g., "quest-book")
     * @return Optional containing the template content
     */
    public Optional<String> loadPageTemplate(String pageName) {
        return loadTemplate("pages/" + pageName + TEMPLATE_EXTENSION);
    }
    
    /**
     * Loads a component template by name.
     * 
     * <p>Convenience method that automatically prepends "components/" and appends ".hyuiml".
     * 
     * @param componentName the component name
     * @return Optional containing the template content
     */
    public Optional<String> loadComponentTemplate(String componentName) {
        return loadTemplate("components/" + componentName + TEMPLATE_EXTENSION);
    }
    
    /**
     * Clears the template cache.
     * 
     * <p>Use this during development to force templates to be reloaded.
     */
    public void clearCache() {
        templateCache.clear();
    }
    
    /**
     * Gets the number of cached templates.
     * 
     * @return the cache size
     */
    public int getCacheSize() {
        return templateCache.size();
    }
    
    /**
     * Checks if a template exists at the given path.
     * 
     * @param relativePath the path relative to the base path
     * @return true if the template exists
     */
    public boolean templateExists(String relativePath) {
        String fullPath = basePath + relativePath;
        
        // Check cache first
        if (cachingEnabled && templateCache.containsKey(fullPath)) {
            return true;
        }
        
        // Check resources
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fullPath)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Exception thrown when a template cannot be found.
     */
    public static class TemplateNotFoundException extends RuntimeException {
        
        private final String templatePath;
        
        /**
         * Creates a new TemplateNotFoundException.
         * 
         * @param templatePath the path that was not found
         */
        public TemplateNotFoundException(String templatePath) {
            super("Template not found: " + templatePath);
            this.templatePath = templatePath;
        }
        
        /**
         * Gets the template path that was not found.
         * 
         * @return the template path
         */
        public String getTemplatePath() {
            return templatePath;
        }
    }
}
