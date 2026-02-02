/*
 * Copyright (c) 2024-2025 Argonath Systems. All rights reserved.
 * This file is part of the Argonath Framework UI module.
 */
package com.argonathsystems.framework.ui.menu.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a group listing in the group finder (LFG system).
 * Used by dungeon-group-finder.hyuiml template.
 * 
 * @param listingId Unique listing identifier
 * @param dungeonId Dungeon identifier
 * @param dungeonName Dungeon display name
 * @param dungeonIcon Dungeon icon
 * @param difficulty Dungeon difficulty (NORMAL, HEROIC, MYTHIC)
 * @param leaderId Party leader's player ID
 * @param leaderName Party leader's display name
 * @param roleSlots Current role composition
 * @param requiredItemLevel Minimum item level requirement
 * @param note Group description/note
 * @param isAutoAccept Whether applications are auto-accepted
 * @param isFriendsOnly Whether restricted to friends/guild
 * @param currentSize Current party size
 * @param maxSize Maximum party size
 * @param createdAt When the listing was created
 * @param applicantIds Players who have applied
 */
public record GroupListingData(
    String listingId,
    String dungeonId,
    String dungeonName,
    String dungeonIcon,
    DungeonDifficulty difficulty,
    UUID leaderId,
    String leaderName,
    List<RoleSlot> roleSlots,
    int requiredItemLevel,
    Optional<String> note,
    boolean isAutoAccept,
    boolean isFriendsOnly,
    int currentSize,
    int maxSize,
    long createdAt,
    List<UUID> applicantIds
) {
    
    /**
     * Dungeon difficulty levels.
     */
    public enum DungeonDifficulty {
        NORMAL("normal", "Normal", "#5a9d5a", 1.0),
        HEROIC("heroic", "Heroic", "#dbdb5a", 1.5),
        MYTHIC("mythic", "Mythic", "#a335ee", 2.0),
        MYTHIC_PLUS("mythic-plus", "Mythic+", "#ff8000", 2.5);
        
        private final String cssClass;
        private final String displayName;
        private final String color;
        private final double rewardMultiplier;
        
        DungeonDifficulty(String cssClass, String displayName, String color, double rewardMultiplier) {
            this.cssClass = cssClass;
            this.displayName = displayName;
            this.color = color;
            this.rewardMultiplier = rewardMultiplier;
        }
        
        public String getCssClass() {
            return cssClass;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
        
        public double getRewardMultiplier() {
            return rewardMultiplier;
        }
    }
    
    /**
     * A role slot in the group.
     * 
     * @param role The combat role
     * @param isFilled Whether this slot is filled
     * @param playerId The player ID if filled
     * @param playerName The player name if filled
     */
    public record RoleSlot(
        PartyMemberData.CombatRole role,
        boolean isFilled,
        Optional<UUID> playerId,
        Optional<String> playerName
    ) {
        /**
         * Creates an empty role slot.
         */
        public static RoleSlot empty(PartyMemberData.CombatRole role) {
            return new RoleSlot(role, false, Optional.empty(), Optional.empty());
        }
        
        /**
         * Creates a filled role slot.
         */
        public static RoleSlot filled(PartyMemberData.CombatRole role, UUID playerId, String playerName) {
            return new RoleSlot(role, true, Optional.of(playerId), Optional.of(playerName));
        }
        
        /**
         * Converts to template map.
         */
        public Map<String, Object> toTemplateMap() {
            return Map.of(
                "role", role.getId(),
                "icon", role.getIcon(),
                "color", role.getColor(),
                "filled", isFilled,
                "playerName", playerName.orElse("")
            );
        }
    }
    
    /**
     * Creates a new group listing with default values.
     */
    public static GroupListingData create(
        String dungeonId,
        String dungeonName,
        String dungeonIcon,
        DungeonDifficulty difficulty,
        UUID leaderId,
        String leaderName,
        int requiredItemLevel
    ) {
        // Default 5-man composition: 1 tank, 1 healer, 3 dps
        List<RoleSlot> defaultSlots = List.of(
            RoleSlot.empty(PartyMemberData.CombatRole.TANK),
            RoleSlot.empty(PartyMemberData.CombatRole.HEALER),
            RoleSlot.empty(PartyMemberData.CombatRole.DPS),
            RoleSlot.empty(PartyMemberData.CombatRole.DPS),
            RoleSlot.empty(PartyMemberData.CombatRole.DPS)
        );
        
        return new GroupListingData(
            UUID.randomUUID().toString(),
            dungeonId,
            dungeonName,
            dungeonIcon,
            difficulty,
            leaderId,
            leaderName,
            defaultSlots,
            requiredItemLevel,
            Optional.empty(),
            false,
            false,
            0,
            5,
            System.currentTimeMillis(),
            List.of()
        );
    }
    
    /**
     * Returns the number of unfilled slots.
     */
    public int getOpenSlots() {
        return (int) roleSlots.stream().filter(s -> !s.isFilled()).count();
    }
    
    /**
     * Returns whether a specific role is needed.
     */
    public boolean needsRole(PartyMemberData.CombatRole role) {
        return roleSlots.stream()
            .anyMatch(s -> s.role() == role && !s.isFilled());
    }
    
    /**
     * Returns whether the viewer can apply.
     * 
     * @param viewerId The viewing player's ID
     * @param viewerItemLevel The viewer's item level
     * @param viewerIsFriend Whether viewer is friends with leader
     * @param viewerInSameGuild Whether viewer is in same guild
     */
    public boolean canApply(UUID viewerId, int viewerItemLevel, boolean viewerIsFriend, boolean viewerInSameGuild) {
        // Cannot apply to own listing
        if (leaderId.equals(viewerId)) {
            return false;
        }
        // Check item level
        if (viewerItemLevel < requiredItemLevel) {
            return false;
        }
        // Check friends/guild restriction
        if (isFriendsOnly && !viewerIsFriend && !viewerInSameGuild) {
            return false;
        }
        // Check if already applied
        if (applicantIds.contains(viewerId)) {
            return false;
        }
        // Check if group is full
        return getOpenSlots() > 0;
    }
    
    /**
     * Returns whether the viewer has already applied.
     */
    public boolean hasApplied(UUID viewerId) {
        return applicantIds.contains(viewerId);
    }
    
    /**
     * Formats the time since listing creation.
     */
    public String formatAge() {
        long now = System.currentTimeMillis();
        long seconds = (now - createdAt) / 1000;
        
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else {
            return (seconds / 3600) + "h";
        }
    }
    
    /**
     * Converts to a map for template rendering.
     * 
     * @param viewerId The viewing player's ID
     * @param isAltRow Whether this is an alternate row
     */
    public Map<String, Object> toTemplateMap(UUID viewerId, boolean isAltRow) {
        List<Map<String, Object>> slotMaps = roleSlots.stream()
            .map(RoleSlot::toTemplateMap)
            .toList();
        
        return Map.ofEntries(
            Map.entry("id", listingId),
            Map.entry("dungeonId", dungeonId),
            Map.entry("dungeonName", dungeonName),
            Map.entry("dungeonIcon", dungeonIcon),
            Map.entry("difficulty", difficulty.getCssClass()),
            Map.entry("difficultyLabel", difficulty.getDisplayName()),
            Map.entry("difficultyColor", difficulty.getColor()),
            Map.entry("leaderId", leaderId.toString()),
            Map.entry("leaderName", leaderName),
            Map.entry("roleSlots", slotMaps),
            Map.entry("requiredIlvl", requiredItemLevel),
            Map.entry("note", note.orElse("")),
            Map.entry("openSlots", getOpenSlots()),
            Map.entry("currentSize", currentSize),
            Map.entry("maxSize", maxSize),
            Map.entry("age", formatAge()),
            Map.entry("applied", hasApplied(viewerId)),
            Map.entry("isAlt", isAltRow),
            Map.entry("needsTank", needsRole(PartyMemberData.CombatRole.TANK)),
            Map.entry("needsHealer", needsRole(PartyMemberData.CombatRole.HEALER)),
            Map.entry("needsDps", needsRole(PartyMemberData.CombatRole.DPS))
        );
    }
    
    /**
     * Returns this listing with a player added to a role slot.
     */
    public GroupListingData withPlayerAdded(PartyMemberData.CombatRole role, UUID playerId, String playerName) {
        List<RoleSlot> newSlots = roleSlots.stream()
            .map(slot -> {
                if (slot.role() == role && !slot.isFilled()) {
                    return RoleSlot.filled(role, playerId, playerName);
                }
                return slot;
            })
            .toList();
        
        return new GroupListingData(
            listingId, dungeonId, dungeonName, dungeonIcon, difficulty,
            leaderId, leaderName, newSlots, requiredItemLevel, note,
            isAutoAccept, isFriendsOnly, currentSize + 1, maxSize,
            createdAt, applicantIds
        );
    }
    
    /**
     * Returns this listing with an applicant added.
     */
    public GroupListingData withApplicant(UUID applicantId) {
        List<UUID> newApplicants = new java.util.ArrayList<>(applicantIds);
        newApplicants.add(applicantId);
        
        return new GroupListingData(
            listingId, dungeonId, dungeonName, dungeonIcon, difficulty,
            leaderId, leaderName, roleSlots, requiredItemLevel, note,
            isAutoAccept, isFriendsOnly, currentSize, maxSize,
            createdAt, List.copyOf(newApplicants)
        );
    }
}
