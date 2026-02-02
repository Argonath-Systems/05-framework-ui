/*
 * Copyright (c) 2024-2025 Argonath Systems. All rights reserved.
 * This file is part of the Argonath Framework UI module.
 */
package com.argonathsystems.framework.ui.menu.model;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a friend entry in the social panel friends list.
 * Used by social-friends.hyuiml template.
 * 
 * @param playerId Unique player identifier
 * @param displayName Player's display name
 * @param level Current player level
 * @param race Player's race (e.g., "Human", "Elf", "Dwarf")
 * @param className Player's class (e.g., "Warrior", "Mage")
 * @param status Online status (ONLINE, AWAY, BUSY, OFFLINE)
 * @param location Current zone/location if online
 * @param avatarIcon Avatar icon or emoji
 * @param lastOnline Last online timestamp if offline
 * @param note Personal note about the friend
 * @param isFavorite Whether marked as favorite/best friend
 * @param mutualGuild Guild name if in same guild
 */
public record FriendEntry(
    UUID playerId,
    String displayName,
    int level,
    String race,
    String className,
    OnlineStatus status,
    Optional<String> location,
    String avatarIcon,
    Optional<Instant> lastOnline,
    Optional<String> note,
    boolean isFavorite,
    Optional<String> mutualGuild
) {
    
    /**
     * Online status types with CSS class mappings.
     */
    public enum OnlineStatus {
        ONLINE("online", "Online", "#5a9d5a"),
        AWAY("away", "Away", "#9d9d5a"),
        BUSY("busy", "Do Not Disturb", "#9d5a5a"),
        OFFLINE("offline", "Offline", "#666666");
        
        private final String cssClass;
        private final String displayText;
        private final String color;
        
        OnlineStatus(String cssClass, String displayText, String color) {
            this.cssClass = cssClass;
            this.displayText = displayText;
            this.color = color;
        }
        
        public String getCssClass() {
            return cssClass;
        }
        
        public String getDisplayText() {
            return displayText;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * Creates a friend entry for an online player.
     */
    public static FriendEntry online(
        UUID playerId,
        String displayName,
        int level,
        String race,
        String className,
        String location,
        String avatarIcon
    ) {
        return new FriendEntry(
            playerId,
            displayName,
            level,
            race,
            className,
            OnlineStatus.ONLINE,
            Optional.of(location),
            avatarIcon,
            Optional.empty(),
            Optional.empty(),
            false,
            Optional.empty()
        );
    }
    
    /**
     * Creates a friend entry for an offline player.
     */
    public static FriendEntry offline(
        UUID playerId,
        String displayName,
        int level,
        String race,
        String className,
        String avatarIcon,
        Instant lastOnline
    ) {
        return new FriendEntry(
            playerId,
            displayName,
            level,
            race,
            className,
            OnlineStatus.OFFLINE,
            Optional.empty(),
            avatarIcon,
            Optional.of(lastOnline),
            Optional.empty(),
            false,
            Optional.empty()
        );
    }
    
    /**
     * Returns whether the friend is currently online (any non-offline status).
     */
    public boolean isOnline() {
        return status != OnlineStatus.OFFLINE;
    }
    
    /**
     * Formats the last online time for display.
     */
    public String formatLastOnline() {
        if (lastOnline.isEmpty()) {
            return "Unknown";
        }
        
        Instant now = Instant.now();
        long seconds = now.getEpochSecond() - lastOnline.get().getEpochSecond();
        
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h ago";
        } else {
            return (seconds / 86400) + "d ago";
        }
    }
    
    /**
     * Converts to a map for template rendering.
     */
    public Map<String, Object> toTemplateMap() {
        return Map.ofEntries(
            Map.entry("id", playerId.toString()),
            Map.entry("name", displayName),
            Map.entry("level", level),
            Map.entry("race", race),
            Map.entry("class", className),
            Map.entry("status", status.getCssClass()),
            Map.entry("statusText", status.getDisplayText()),
            Map.entry("location", location.orElse("")),
            Map.entry("avatarIcon", avatarIcon),
            Map.entry("lastOnline", formatLastOnline()),
            Map.entry("note", note.orElse("")),
            Map.entry("isFavorite", isFavorite),
            Map.entry("isOnline", isOnline()),
            Map.entry("mutualGuild", mutualGuild.orElse(""))
        );
    }
    
    /**
     * Returns this entry with favorite status toggled.
     */
    public FriendEntry withFavoriteToggled() {
        return new FriendEntry(
            playerId, displayName, level, race, className,
            status, location, avatarIcon, lastOnline, note,
            !isFavorite, mutualGuild
        );
    }
    
    /**
     * Returns this entry with updated status.
     */
    public FriendEntry withStatus(OnlineStatus newStatus) {
        return new FriendEntry(
            playerId, displayName, level, race, className,
            newStatus, location, avatarIcon, lastOnline, note,
            isFavorite, mutualGuild
        );
    }
}
