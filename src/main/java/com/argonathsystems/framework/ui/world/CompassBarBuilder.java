package com.argonathsystems.framework.ui.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Platform-agnostic data builder for Compass Bar HUD using HyUIML.
 * 
 * <p>This class builds the data model for navigation compass HUDs. It does NOT process
 * templates or render UIs directly - that is handled by the adapter layer using
 * HyUI's {@code TemplateProcessor} and {@code HudBuilder}.
 * 
 * <h2>Architecture</h2>
 * <pre>{@code
 *   CompassBarBuilder (this class)
 *          ↓ builds data
 *   Map<String, Object> templateVariables
 *          ↓ passed to
 *   02-adapter-hytale/CompassBarAdapter
 *          ↓ uses HyUI
 *   TemplateProcessor → HudBuilder → Player HUD
 * }</pre>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Skyrim-style horizontal compass</li>
 *   <li>Cardinal and ordinal direction markers</li>
 *   <li>Quest objective markers with distance</li>
 *   <li>POI discovery markers</li>
 *   <li>Hot reload support via template supplier</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @see CompassMarker
 * @since 1.1.0
 */
public class CompassBarBuilder {
    
    private float playerHeading;
    private float playerX;
    private float playerZ;
    private final List<CompassMarker> markers;
    private boolean showDistance = true;
    private float visibleRange = 90.0f; // degrees visible on compass
    private Supplier<String> templateSupplier;
    
    /**
     * Create a new compass bar builder.
     */
    public CompassBarBuilder() {
        this.markers = new ArrayList<>();
    }
    
    /**
     * Set the template supplier for hot reload support.
     * 
     * @param templateSupplier Supplier providing the base HyUIML template
     * @return this builder
     */
    public CompassBarBuilder setTemplateSupplier(Supplier<String> templateSupplier) {
        this.templateSupplier = templateSupplier;
        return this;
    }
    
    /**
     * Set the player's heading (rotation).
     * 
     * @param heading heading in degrees (0-360, 0=North)
     * @return this builder
     */
    public CompassBarBuilder setPlayerHeading(float heading) {
        this.playerHeading = (heading % 360 + 360) % 360;
        return this;
    }
    
    /**
     * Set the player's world position.
     * 
     * @param x X coordinate
     * @param z Z coordinate
     * @return this builder
     */
    public CompassBarBuilder setPlayerPosition(float x, float z) {
        this.playerX = x;
        this.playerZ = z;
        return this;
    }
    
    /**
     * Add a compass marker.
     * 
     * @param marker the marker to add
     * @return this builder
     */
    public CompassBarBuilder addMarker(CompassMarker marker) {
        this.markers.add(marker);
        return this;
    }
    
    /**
     * Clear all markers.
     * 
     * @return this builder
     */
    public CompassBarBuilder clearMarkers() {
        this.markers.clear();
        return this;
    }
    
    /**
     * Set whether to show distance on markers.
     * 
     * @param show true to show distance
     * @return this builder
     */
    public CompassBarBuilder setShowDistance(boolean show) {
        this.showDistance = show;
        return this;
    }
    
    /**
     * Set the visible range of the compass in degrees.
     * 
     * @param degrees visible arc (default 90)
     * @return this builder
     */
    public CompassBarBuilder setVisibleRange(float degrees) {
        this.visibleRange = degrees;
        return this;
    }
    
    /**
     * Build the template variables map for HyUI TemplateProcessor.
     * 
     * @return Map of template variables for TemplateProcessor
     */
    public Map<String, Object> buildTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        // Player heading
        variables.put("playerHeading", playerHeading);
        variables.put("playerX", playerX);
        variables.put("playerZ", playerZ);
        
        // Direction markers (N, NE, E, SE, S, SW, W, NW)
        List<Map<String, Object>> directions = buildDirectionMarkers();
        variables.put("directions", directions);
        
        // Quest/POI markers with relative positions
        List<Map<String, Object>> markersList = new ArrayList<>();
        for (CompassMarker marker : markers) {
            Map<String, Object> markerMap = marker.toMap();
            
            // Calculate relative position on compass
            float bearing = marker.getBearing(playerX, playerZ);
            float relativeBearing = normalizeAngle(bearing - playerHeading);
            
            // Only include markers within visible range
            if (Math.abs(relativeBearing) <= visibleRange / 2) {
                markerMap.put("bearing", bearing);
                markerMap.put("relativeBearing", relativeBearing);
                markerMap.put("compassPosition", (relativeBearing / (visibleRange / 2)) * 50 + 50);
                markersList.add(markerMap);
            }
        }
        variables.put("markers", markersList);
        
        // Settings
        variables.put("showDistance", showDistance);
        variables.put("visibleRange", visibleRange);
        
        return variables;
    }
    
    /**
     * Build the cardinal/ordinal direction markers.
     */
    private List<Map<String, Object>> buildDirectionMarkers() {
        List<Map<String, Object>> directions = new ArrayList<>();
        
        String[] labels = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        float[] angles = {0, 45, 90, 135, 180, 225, 270, 315};
        
        for (int i = 0; i < labels.length; i++) {
            float relativeBearing = normalizeAngle(angles[i] - playerHeading);
            
            // Only include if within visible range
            if (Math.abs(relativeBearing) <= visibleRange / 2) {
                Map<String, Object> dir = new HashMap<>();
                dir.put("label", labels[i]);
                dir.put("angle", angles[i]);
                dir.put("relativeBearing", relativeBearing);
                dir.put("isCardinal", i % 2 == 0);
                dir.put("compassPosition", (relativeBearing / (visibleRange / 2)) * 50 + 50);
                directions.add(dir);
            }
        }
        
        return directions;
    }
    
    /**
     * Normalize angle to -180 to 180 range.
     */
    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
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
    
    public float getPlayerHeading() {
        return playerHeading;
    }
    
    public float getPlayerX() {
        return playerX;
    }
    
    public float getPlayerZ() {
        return playerZ;
    }
    
    public List<CompassMarker> getMarkers() {
        return List.copyOf(markers);
    }
    
    public boolean isShowDistance() {
        return showDistance;
    }
    
    public float getVisibleRange() {
        return visibleRange;
    }
}
