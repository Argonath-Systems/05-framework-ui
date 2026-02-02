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
 * Represents a party member in the party panel.
 * Used by social-friends.hyuiml template's party section.
 * 
 * @param playerId Unique player identifier
 * @param displayName Player's display name
 * @param level Current player level
 * @param className Player's class
 * @param role Combat role (TANK, HEALER, DPS)
 * @param avatarIcon Avatar icon or emoji
 * @param isLeader Whether this member is the party leader
 * @param healthPercent Current health as percentage (0-100)
 * @param manaPercent Current mana/resource as percentage (0-100)
 * @param isOnline Whether the player is online
 * @param isDead Whether the player is dead
 * @param hasAggro Whether the player currently has enemy aggro
 */
public record PartyMemberData(
    UUID playerId,
    String displayName,
    int level,
    String className,
    CombatRole role,
    String avatarIcon,
    boolean isLeader,
    int healthPercent,
    int manaPercent,
    boolean isOnline,
    boolean isDead,
    boolean hasAggro
) {
    
    /**
     * Combat roles for party composition.
     */
    public enum CombatRole {
        TANK("tank", "Tank", "🛡️", "#5a9ddb"),
        HEALER("healer", "Healer", "💚", "#5adb5a"),
        DPS("dps", "Damage", "⚔️", "#db5a5a");
        
        private final String id;
        private final String displayName;
        private final String icon;
        private final String color;
        
        CombatRole(String id, String displayName, String icon, String color) {
            this.id = id;
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
        
        public String getId() {
            return id;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * Creates a party member data object.
     */
    public static PartyMemberData create(
        UUID playerId,
        String displayName,
        int level,
        String className,
        CombatRole role,
        String avatarIcon,
        boolean isLeader
    ) {
        return new PartyMemberData(
            playerId,
            displayName,
            level,
            className,
            role,
            avatarIcon,
            isLeader,
            100,
            100,
            true,
            false,
            false
        );
    }
    
    /**
     * Returns the CSS class for health bar color based on health percentage.
     */
    public String getHealthColorClass() {
        if (isDead) {
            return "dead";
        } else if (healthPercent < 25) {
            return "critical";
        } else if (healthPercent < 50) {
            return "low";
        } else {
            return "normal";
        }
    }
    
    /**
     * Returns whether this member can be kicked by the viewer.
     * 
     * @param viewerIsLeader Whether the viewing player is the party leader
     * @param viewerId The viewer's player ID
     */
    public boolean canBeKickedBy(boolean viewerIsLeader, UUID viewerId) {
        return viewerIsLeader && !playerId.equals(viewerId);
    }
    
    /**
     * Converts to a map for template rendering.
     * 
     * @param viewerIsLeader Whether the viewing player is the party leader
     * @param viewerId The viewer's player ID
     */
    public Map<String, Object> toTemplateMap(boolean viewerIsLeader, UUID viewerId) {
        return Map.ofEntries(
            Map.entry("id", playerId.toString()),
            Map.entry("name", displayName),
            Map.entry("level", level),
            Map.entry("class", className),
            Map.entry("role", role.getDisplayName()),
            Map.entry("roleIcon", role.getIcon()),
            Map.entry("roleColor", role.getColor()),
            Map.entry("avatarIcon", avatarIcon),
            Map.entry("isLeader", isLeader),
            Map.entry("healthPercent", healthPercent),
            Map.entry("manaPercent", manaPercent),
            Map.entry("healthColorClass", getHealthColorClass()),
            Map.entry("isOnline", isOnline),
            Map.entry("isDead", isDead),
            Map.entry("hasAggro", hasAggro),
            Map.entry("canKick", canBeKickedBy(viewerIsLeader, viewerId))
        );
    }
    
    /**
     * Returns this member with updated health.
     */
    public PartyMemberData withHealth(int newHealthPercent) {
        return new PartyMemberData(
            playerId, displayName, level, className, role, avatarIcon,
            isLeader, Math.max(0, Math.min(100, newHealthPercent)), manaPercent,
            isOnline, newHealthPercent <= 0, hasAggro
        );
    }
    
    /**
     * Returns this member with updated mana.
     */
    public PartyMemberData withMana(int newManaPercent) {
        return new PartyMemberData(
            playerId, displayName, level, className, role, avatarIcon,
            isLeader, healthPercent, Math.max(0, Math.min(100, newManaPercent)),
            isOnline, isDead, hasAggro
        );
    }
    
    /**
     * Returns this member with leader status changed.
     */
    public PartyMemberData withLeader(boolean newIsLeader) {
        return new PartyMemberData(
            playerId, displayName, level, className, role, avatarIcon,
            newIsLeader, healthPercent, manaPercent, isOnline, isDead, hasAggro
        );
    }
}
