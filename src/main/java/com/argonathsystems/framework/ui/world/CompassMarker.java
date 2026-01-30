package com.argonathsystems.framework.ui.world;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a marker on the compass (quest objective, POI, etc).
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
public class CompassMarker {
    
    private final String id;
    private final String type;
    private final String name;
    private final String iconUrl;
    private final float worldX;
    private final float worldZ;
    private final float distance;
    private final boolean isTracked;
    
    private CompassMarker(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.iconUrl = builder.iconUrl;
        this.worldX = builder.worldX;
        this.worldZ = builder.worldZ;
        this.distance = builder.distance;
        this.isTracked = builder.isTracked;
    }
    
    /**
     * Get the marker ID.
     * 
     * @return Marker ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the marker type.
     * 
     * @return Type (main, side, poi, discovery, etc)
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get the display name.
     * 
     * @return Marker name
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
     * Get the world X coordinate.
     * 
     * @return X position
     */
    public float getWorldX() {
        return worldX;
    }
    
    /**
     * Get the world Z coordinate.
     * 
     * @return Z position
     */
    public float getWorldZ() {
        return worldZ;
    }
    
    /**
     * Get the distance from player.
     * 
     * @return Distance in blocks
     */
    public float getDistance() {
        return distance;
    }
    
    /**
     * Check if this marker is actively tracked.
     * 
     * @return true if tracked
     */
    public boolean isTracked() {
        return isTracked;
    }
    
    /**
     * Calculate the bearing angle from player position.
     * 
     * @param playerX player X position
     * @param playerZ player Z position
     * @return bearing angle in degrees (0-360)
     */
    public float getBearing(float playerX, float playerZ) {
        float dx = worldX - playerX;
        float dz = worldZ - playerZ;
        float angle = (float) Math.toDegrees(Math.atan2(dx, -dz));
        return (angle + 360) % 360;
    }
    
    /**
     * Convert to a map for template processing.
     * 
     * @return Map of template variables
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("type", type);
        map.put("name", name);
        map.put("iconUrl", iconUrl != null ? iconUrl : "");
        map.put("worldX", worldX);
        map.put("worldZ", worldZ);
        map.put("distance", distance);
        map.put("distanceText", formatDistance(distance));
        map.put("isTracked", isTracked);
        return map;
    }
    
    private String formatDistance(float dist) {
        if (dist < 100) {
            return String.format("%.0f", dist);
        } else if (dist < 1000) {
            return String.format("%.0f", dist);
        } else {
            return String.format("%.1fk", dist / 1000);
        }
    }
    
    /**
     * Create a new builder.
     * 
     * @param id marker ID
     * @param type marker type
     * @return a new builder instance
     */
    public static Builder builder(String id, String type) {
        return new Builder(id, type);
    }
    
    /**
     * Builder for CompassMarker.
     */
    public static class Builder {
        private final String id;
        private final String type;
        private String name = "";
        private String iconUrl;
        private float worldX;
        private float worldZ;
        private float distance;
        private boolean isTracked = false;
        
        private Builder(String id, String type) {
            this.id = id;
            this.type = type;
        }
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder withIcon(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }
        
        public Builder atPosition(float worldX, float worldZ) {
            this.worldX = worldX;
            this.worldZ = worldZ;
            return this;
        }
        
        public Builder withDistance(float distance) {
            this.distance = distance;
            return this;
        }
        
        public Builder tracked(boolean isTracked) {
            this.isTracked = isTracked;
            return this;
        }
        
        public CompassMarker build() {
            return new CompassMarker(this);
        }
    }
}
