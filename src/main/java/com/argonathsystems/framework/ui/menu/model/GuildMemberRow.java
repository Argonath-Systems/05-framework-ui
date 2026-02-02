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
 * Represents a guild member row in the guild roster.
 * Used by guild-management.hyuiml template.
 * 
 * @param playerId Unique player identifier
 * @param displayName Player's display name
 * @param level Current player level
 * @param className Player's class
 * @param race Player's race
 * @param rank Guild rank (e.g., "Guild Master", "Officer", "Member")
 * @param rankId Numeric rank ID for sorting (0 = leader)
 * @param note Officer/public note
 * @param isOnline Whether currently online
 * @param lastSeen Last seen timestamp if offline
 * @param location Current location if online
 * @param joinDate When they joined the guild
 * @param weeklyContribution Weekly contribution points
 * @param totalContribution Total lifetime contribution
 */
public record GuildMemberRow(
    UUID playerId,
    String displayName,
    int level,
    String className,
    String race,
    String rank,
    int rankId,
    Optional<String> note,
    boolean isOnline,
    Optional<Instant> lastSeen,
    Optional<String> location,
    Instant joinDate,
    int weeklyContribution,
    int totalContribution
) {
    
    /**
     * Standard guild rank tiers.
     */
    public enum RankTier {
        LEADER(0, "leader", "#d4af37"),
        OFFICER(1, "officer", "#9d9ddb"),
        VETERAN(2, "veteran", "#5a9d5a"),
        MEMBER(3, "member", "#888888"),
        INITIATE(4, "initiate", "#666666");
        
        private final int priority;
        private final String cssClass;
        private final String color;
        
        RankTier(int priority, String cssClass, String color) {
            this.priority = priority;
            this.cssClass = cssClass;
            this.color = color;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public String getCssClass() {
            return cssClass;
        }
        
        public String getColor() {
            return color;
        }
        
        /**
         * Gets tier from rank ID.
         */
        public static RankTier fromRankId(int rankId) {
            if (rankId == 0) return LEADER;
            if (rankId == 1) return OFFICER;
            if (rankId == 2) return VETERAN;
            if (rankId == 3) return MEMBER;
            return INITIATE;
        }
    }
    
    /**
     * Creates a guild member row.
     */
    public static GuildMemberRow create(
        UUID playerId,
        String displayName,
        int level,
        String className,
        String race,
        String rank,
        int rankId
    ) {
        return new GuildMemberRow(
            playerId,
            displayName,
            level,
            className,
            race,
            rank,
            rankId,
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty(),
            Instant.now(),
            0,
            0
        );
    }
    
    /**
     * Returns the rank tier for this member.
     */
    public RankTier getRankTier() {
        return RankTier.fromRankId(rankId);
    }
    
    /**
     * Returns whether this member is an officer or higher.
     */
    public boolean isOfficerOrHigher() {
        return rankId <= 1;
    }
    
    /**
     * Returns whether this member is the guild leader.
     */
    public boolean isGuildLeader() {
        return rankId == 0;
    }
    
    /**
     * Formats the last seen time for display.
     */
    public String formatLastSeen() {
        if (isOnline) {
            return "Online";
        }
        if (lastSeen.isEmpty()) {
            return "Unknown";
        }
        
        Instant now = Instant.now();
        long seconds = now.getEpochSecond() - lastSeen.get().getEpochSecond();
        
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h ago";
        } else if (seconds < 604800) {
            return (seconds / 86400) + "d ago";
        } else {
            return (seconds / 604800) + "w ago";
        }
    }
    
    /**
     * Formats contribution with K/M suffix.
     */
    private String formatContribution(int value) {
        if (value >= 1000000) {
            return String.format("%.1fM", value / 1000000.0);
        } else if (value >= 1000) {
            return String.format("%.1fK", value / 1000.0);
        }
        return String.valueOf(value);
    }
    
    /**
     * Determines whether the viewer can manage this member.
     * 
     * @param viewerRankId The viewer's rank ID
     * @param viewerId The viewer's player ID
     */
    public boolean canBeManagedBy(int viewerRankId, UUID viewerId) {
        // Cannot manage yourself
        if (playerId.equals(viewerId)) {
            return false;
        }
        // Can only manage lower ranks
        return viewerRankId < rankId;
    }
    
    /**
     * Converts to a map for template rendering.
     * 
     * @param viewerRankId The viewer's rank ID for permission checks
     * @param viewerId The viewer's player ID
     * @param isAltRow Whether this is an alternate row for styling
     */
    public Map<String, Object> toTemplateMap(int viewerRankId, UUID viewerId, boolean isAltRow) {
        RankTier tier = getRankTier();
        
        return Map.ofEntries(
            Map.entry("id", playerId.toString()),
            Map.entry("name", displayName),
            Map.entry("level", level),
            Map.entry("class", className),
            Map.entry("race", race),
            Map.entry("rank", rank),
            Map.entry("rankClass", tier.getCssClass()),
            Map.entry("rankColor", tier.getColor()),
            Map.entry("note", note.orElse("")),
            Map.entry("isOnline", isOnline),
            Map.entry("lastSeen", formatLastSeen()),
            Map.entry("location", location.orElse("")),
            Map.entry("weeklyContrib", formatContribution(weeklyContribution)),
            Map.entry("totalContrib", formatContribution(totalContribution)),
            Map.entry("canManage", canBeManagedBy(viewerRankId, viewerId)),
            Map.entry("isAlt", isAltRow),
            Map.entry("isLeader", isGuildLeader()),
            Map.entry("isOfficer", isOfficerOrHigher())
        );
    }
    
    /**
     * Returns this member with online status changed.
     */
    public GuildMemberRow withOnlineStatus(boolean newIsOnline, String newLocation) {
        return new GuildMemberRow(
            playerId, displayName, level, className, race, rank, rankId,
            note, newIsOnline,
            newIsOnline ? Optional.empty() : Optional.of(Instant.now()),
            newIsOnline ? Optional.ofNullable(newLocation) : Optional.empty(),
            joinDate, weeklyContribution, totalContribution
        );
    }
    
    /**
     * Returns this member with a different rank.
     */
    public GuildMemberRow withRank(String newRank, int newRankId) {
        return new GuildMemberRow(
            playerId, displayName, level, className, race, newRank, newRankId,
            note, isOnline, lastSeen, location, joinDate,
            weeklyContribution, totalContribution
        );
    }
}
