/*
 * Copyright (c) 2024-2025 Argonath Systems. All rights reserved.
 * This file is part of the Argonath Framework UI module.
 */
package com.argonathsystems.framework.ui.menu.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a dungeon entry in the dungeon finder selection list.
 * Used by dungeon-group-finder.hyuiml template.
 * 
 * @param dungeonId Unique dungeon identifier
 * @param name Dungeon display name
 * @param icon Dungeon icon
 * @param minLevel Minimum level requirement
 * @param maxLevel Maximum level (for level scaling)
 * @param minItemLevel Minimum item level requirement
 * @param bossCount Number of bosses in the dungeon
 * @param estimatedTime Estimated completion time in minutes
 * @param difficulty Current selected difficulty
 * @param availableDifficulties List of available difficulties
 * @param isLocked Whether the player has unlocked this dungeon
 * @param lockReason Reason for lock if locked
 * @param isSelected Whether currently selected for queue
 * @param description Brief dungeon description
 * @param location Zone/region where dungeon is located
 */
public record DungeonEntry(
    String dungeonId,
    String name,
    String icon,
    int minLevel,
    int maxLevel,
    int minItemLevel,
    int bossCount,
    int estimatedTime,
    GroupListingData.DungeonDifficulty difficulty,
    List<GroupListingData.DungeonDifficulty> availableDifficulties,
    boolean isLocked,
    Optional<String> lockReason,
    boolean isSelected,
    Optional<String> description,
    Optional<String> location
) {
    
    /**
     * Creates an unlocked dungeon entry.
     */
    public static DungeonEntry unlocked(
        String dungeonId,
        String name,
        String icon,
        int minLevel,
        int bossCount,
        int estimatedTime
    ) {
        return new DungeonEntry(
            dungeonId,
            name,
            icon,
            minLevel,
            minLevel + 10,
            0,
            bossCount,
            estimatedTime,
            GroupListingData.DungeonDifficulty.NORMAL,
            List.of(
                GroupListingData.DungeonDifficulty.NORMAL,
                GroupListingData.DungeonDifficulty.HEROIC,
                GroupListingData.DungeonDifficulty.MYTHIC
            ),
            false,
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty()
        );
    }
    
    /**
     * Creates a locked dungeon entry.
     */
    public static DungeonEntry locked(
        String dungeonId,
        String name,
        String icon,
        int minLevel,
        String lockReason
    ) {
        return new DungeonEntry(
            dungeonId,
            name,
            icon,
            minLevel,
            minLevel + 10,
            0,
            0,
            0,
            GroupListingData.DungeonDifficulty.NORMAL,
            List.of(),
            true,
            Optional.of(lockReason),
            false,
            Optional.empty(),
            Optional.empty()
        );
    }
    
    /**
     * Checks if the player meets the level requirement.
     * 
     * @param playerLevel The player's current level
     */
    public boolean meetsLevelRequirement(int playerLevel) {
        return playerLevel >= minLevel;
    }
    
    /**
     * Checks if the player meets the item level requirement.
     * 
     * @param playerItemLevel The player's average item level
     */
    public boolean meetsItemLevelRequirement(int playerItemLevel) {
        return playerItemLevel >= minItemLevel;
    }
    
    /**
     * Checks if the player can queue for this dungeon.
     * 
     * @param playerLevel The player's current level
     * @param playerItemLevel The player's average item level
     */
    public boolean canQueue(int playerLevel, int playerItemLevel) {
        return !isLocked && 
               meetsLevelRequirement(playerLevel) && 
               meetsItemLevelRequirement(playerItemLevel);
    }
    
    /**
     * Formats estimated time for display.
     */
    public String formatEstimatedTime() {
        if (estimatedTime <= 0) {
            return "Unknown";
        } else if (estimatedTime < 60) {
            return estimatedTime + " min";
        } else {
            int hours = estimatedTime / 60;
            int mins = estimatedTime % 60;
            return mins > 0 ? hours + "h " + mins + "m" : hours + "h";
        }
    }
    
    /**
     * Converts to a map for template rendering.
     * 
     * @param playerLevel The player's level for requirement checks
     * @param playerItemLevel The player's item level
     */
    public Map<String, Object> toTemplateMap(int playerLevel, int playerItemLevel) {
        return Map.ofEntries(
            Map.entry("id", dungeonId),
            Map.entry("name", name),
            Map.entry("icon", icon),
            Map.entry("minLevel", minLevel),
            Map.entry("maxLevel", maxLevel),
            Map.entry("minItemLevel", minItemLevel),
            Map.entry("bosses", bossCount),
            Map.entry("time", formatEstimatedTime()),
            Map.entry("difficulty", difficulty.getCssClass()),
            Map.entry("difficultyLabel", difficulty.getDisplayName()),
            Map.entry("locked", isLocked),
            Map.entry("lockReason", lockReason.orElse("")),
            Map.entry("selected", isSelected),
            Map.entry("description", description.orElse("")),
            Map.entry("location", location.orElse("")),
            Map.entry("meetsLevel", meetsLevelRequirement(playerLevel)),
            Map.entry("meetsItemLevel", meetsItemLevelRequirement(playerItemLevel)),
            Map.entry("canQueue", canQueue(playerLevel, playerItemLevel))
        );
    }
    
    /**
     * Returns this entry with selection toggled.
     */
    public DungeonEntry withSelectionToggled() {
        return new DungeonEntry(
            dungeonId, name, icon, minLevel, maxLevel, minItemLevel,
            bossCount, estimatedTime, difficulty, availableDifficulties,
            isLocked, lockReason, !isSelected, description, location
        );
    }
    
    /**
     * Returns this entry with a different difficulty.
     */
    public DungeonEntry withDifficulty(GroupListingData.DungeonDifficulty newDifficulty) {
        if (!availableDifficulties.contains(newDifficulty)) {
            return this;
        }
        return new DungeonEntry(
            dungeonId, name, icon, minLevel, maxLevel, minItemLevel,
            bossCount, estimatedTime, newDifficulty, availableDifficulties,
            isLocked, lockReason, isSelected, description, location
        );
    }
    
    /**
     * Returns this entry with detailed information added.
     */
    public DungeonEntry withDetails(String description, String location) {
        return new DungeonEntry(
            dungeonId, name, icon, minLevel, maxLevel, minItemLevel,
            bossCount, estimatedTime, difficulty, availableDifficulties,
            isLocked, lockReason, isSelected, 
            Optional.of(description), 
            Optional.of(location)
        );
    }
}
