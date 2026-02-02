/*
 * Copyright (c) 2024-2025 Argonath Systems. All rights reserved.
 * This file is part of the Argonath Framework UI module.
 */
package com.argonathsystems.framework.ui.menu.model;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a map marker or waypoint on the world map.
 * Used by map-world.hyuiml template.
 * 
 * @param id Unique marker identifier
 * @param name Display name of the marker
 * @param type Type of marker (PLAYER, PARTY, QUEST, DUNGEON, CITY, POI, CUSTOM)
 * @param x X coordinate on the map (0-100 percentage or absolute)
 * @param y Y coordinate on the map (0-100 percentage or absolute)
 * @param worldX World X coordinate
 * @param worldY World Y coordinate (height)
 * @param worldZ World Z coordinate
 * @param icon Icon or emoji to display
 * @param isDiscovered Whether this location has been discovered
 * @param isTracked Whether this marker is currently being tracked
 * @param distance Distance from player in blocks
 * @param description Optional description or additional info
 */
public record MapMarkerData(
    String id,
    String name,
    MarkerType type,
    double x,
    double y,
    double worldX,
    double worldY,
    double worldZ,
    String icon,
    boolean isDiscovered,
    boolean isTracked,
    Optional<Double> distance,
    Optional<String> description
) {
    
    /**
     * Types of map markers with associated styling.
     */
    public enum MarkerType {
        PLAYER("player", "📍", "#5a9ddb", true),
        PARTY("party", "👥", "#5adb5a", true),
        QUEST("quest", "❗", "#d4af37", true),
        QUEST_COMPLETE("quest-complete", "❓", "#9ddb9d", true),
        DUNGEON("dungeon", "⚔️", "#db5a5a", false),
        RAID("raid", "💀", "#a335ee", false),
        CITY("city", "🏰", "#9d9ddb", false),
        TOWN("town", "🏠", "#888888", false),
        VENDOR("vendor", "🛒", "#cd7f32", false),
        FLIGHT("flight", "🦅", "#5a9d5a", false),
        POI("poi", "📌", "#9d9ddb", false),
        CUSTOM("custom", "📍", "#9ddb9d", true);
        
        private final String cssClass;
        private final String defaultIcon;
        private final String color;
        private final boolean canBeRemoved;
        
        MarkerType(String cssClass, String defaultIcon, String color, boolean canBeRemoved) {
            this.cssClass = cssClass;
            this.defaultIcon = defaultIcon;
            this.color = color;
            this.canBeRemoved = canBeRemoved;
        }
        
        public String getCssClass() {
            return cssClass;
        }
        
        public String getDefaultIcon() {
            return defaultIcon;
        }
        
        public String getColor() {
            return color;
        }
        
        public boolean canBeRemoved() {
            return canBeRemoved;
        }
    }
    
    /**
     * Creates a player position marker.
     */
    public static MapMarkerData playerMarker(double x, double y, double worldX, double worldY, double worldZ) {
        return new MapMarkerData(
            "player",
            "You",
            MarkerType.PLAYER,
            x, y,
            worldX, worldY, worldZ,
            MarkerType.PLAYER.getDefaultIcon(),
            true,
            false,
            Optional.empty(),
            Optional.empty()
        );
    }
    
    /**
     * Creates a quest marker.
     */
    public static MapMarkerData questMarker(
        String id,
        String questName,
        double x, double y,
        double worldX, double worldY, double worldZ,
        boolean isComplete
    ) {
        MarkerType type = isComplete ? MarkerType.QUEST_COMPLETE : MarkerType.QUEST;
        return new MapMarkerData(
            id,
            questName,
            type,
            x, y,
            worldX, worldY, worldZ,
            type.getDefaultIcon(),
            true,
            false,
            Optional.empty(),
            Optional.empty()
        );
    }
    
    /**
     * Creates a custom waypoint marker.
     */
    public static MapMarkerData customWaypoint(
        String id,
        String name,
        double x, double y,
        double worldX, double worldY, double worldZ,
        String customIcon
    ) {
        return new MapMarkerData(
            id,
            name,
            MarkerType.CUSTOM,
            x, y,
            worldX, worldY, worldZ,
            customIcon.isEmpty() ? MarkerType.CUSTOM.getDefaultIcon() : customIcon,
            true,
            false,
            Optional.empty(),
            Optional.empty()
        );
    }
    
    /**
     * Formats distance for display.
     */
    public String formatDistance() {
        if (distance.isEmpty()) {
            return "";
        }
        double d = distance.get();
        if (d < 1000) {
            return String.format("%.0fm", d);
        }
        return String.format("%.1fkm", d / 1000);
    }
    
    /**
     * Returns the effective icon (uses type default if empty).
     */
    public String getEffectiveIcon() {
        return icon.isEmpty() ? type.getDefaultIcon() : icon;
    }
    
    /**
     * Converts to a map for template rendering.
     */
    public Map<String, Object> toTemplateMap() {
        return Map.ofEntries(
            Map.entry("id", id),
            Map.entry("name", name),
            Map.entry("type", type.getCssClass()),
            Map.entry("x", x),
            Map.entry("y", y),
            Map.entry("worldX", worldX),
            Map.entry("worldY", worldY),
            Map.entry("worldZ", worldZ),
            Map.entry("icon", getEffectiveIcon()),
            Map.entry("color", type.getColor()),
            Map.entry("isDiscovered", isDiscovered),
            Map.entry("isTracked", isTracked),
            Map.entry("distance", formatDistance()),
            Map.entry("description", description.orElse("")),
            Map.entry("canRemove", type.canBeRemoved())
        );
    }
    
    /**
     * Returns this marker with tracking toggled.
     */
    public MapMarkerData withTrackingToggled() {
        return new MapMarkerData(
            id, name, type, x, y, worldX, worldY, worldZ,
            icon, isDiscovered, !isTracked, distance, description
        );
    }
    
    /**
     * Returns this marker with updated position.
     */
    public MapMarkerData withPosition(double newX, double newY, double newWorldX, double newWorldY, double newWorldZ) {
        return new MapMarkerData(
            id, name, type, newX, newY, newWorldX, newWorldY, newWorldZ,
            icon, isDiscovered, isTracked, distance, description
        );
    }
    
    /**
     * Returns this marker with updated distance from player.
     */
    public MapMarkerData withDistance(double newDistance) {
        return new MapMarkerData(
            id, name, type, x, y, worldX, worldY, worldZ,
            icon, isDiscovered, isTracked, Optional.of(newDistance), description
        );
    }
}
