package com.argonathsystems.framework.ui.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a unit frame (player, target, or party member).
 * 
 * <p>Unit frames display:
 * <ul>
 *   <li>Name and level</li>
 *   <li>Portrait/avatar</li>
 *   <li>Health and resource bars</li>
 *   <li>Buffs and debuffs</li>
 *   <li>Combat state indicators</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
public class UnitFrame {
    
    private final String unitId;
    private final String name;
    private final int level;
    private final String portraitUrl;
    private final String faction;
    private final ResourceBar health;
    private final ResourceBar resource;
    private final List<BuffDebuff> buffs;
    private final List<BuffDebuff> debuffs;
    private final boolean isPlayer;
    private final boolean isHostile;
    private final boolean inCombat;
    private final boolean isDead;
    
    private UnitFrame(Builder builder) {
        this.unitId = builder.unitId;
        this.name = builder.name;
        this.level = builder.level;
        this.portraitUrl = builder.portraitUrl;
        this.faction = builder.faction;
        this.health = builder.health;
        this.resource = builder.resource;
        this.buffs = List.copyOf(builder.buffs);
        this.debuffs = List.copyOf(builder.debuffs);
        this.isPlayer = builder.isPlayer;
        this.isHostile = builder.isHostile;
        this.inCombat = builder.inCombat;
        this.isDead = builder.isDead;
    }
    
    /**
     * Get the unit ID.
     * 
     * @return Unit ID
     */
    public String getUnitId() {
        return unitId;
    }
    
    /**
     * Get the display name.
     * 
     * @return Unit name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the unit level.
     * 
     * @return Level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Get the portrait URL.
     * 
     * @return Portrait resource path
     */
    public String getPortraitUrl() {
        return portraitUrl;
    }
    
    /**
     * Get the faction.
     * 
     * @return Faction name
     */
    public String getFaction() {
        return faction;
    }
    
    /**
     * Get the health bar.
     * 
     * @return Health ResourceBar
     */
    public ResourceBar getHealth() {
        return health;
    }
    
    /**
     * Get the resource bar.
     * 
     * @return Resource bar, or null if none
     */
    public ResourceBar getResource() {
        return resource;
    }
    
    /**
     * Get active buffs.
     * 
     * @return List of buffs
     */
    public List<BuffDebuff> getBuffs() {
        return buffs;
    }
    
    /**
     * Get active debuffs.
     * 
     * @return List of debuffs
     */
    public List<BuffDebuff> getDebuffs() {
        return debuffs;
    }
    
    /**
     * Check if this is the player.
     * 
     * @return true if player frame
     */
    public boolean isPlayer() {
        return isPlayer;
    }
    
    /**
     * Check if unit is hostile.
     * 
     * @return true if hostile
     */
    public boolean isHostile() {
        return isHostile;
    }
    
    /**
     * Check if unit is in combat.
     * 
     * @return true if in combat
     */
    public boolean isInCombat() {
        return inCombat;
    }
    
    /**
     * Check if unit is dead.
     * 
     * @return true if dead
     */
    public boolean isDead() {
        return isDead;
    }
    
    /**
     * Convert to a map for template processing.
     * 
     * @return Map of template variables
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("unitId", unitId);
        map.put("name", name);
        map.put("level", level);
        map.put("portraitUrl", portraitUrl != null ? portraitUrl : "");
        map.put("faction", faction != null ? faction : "");
        
        if (health != null) {
            map.put("health", health.toMap());
        }
        if (resource != null) {
            map.put("resource", resource.toMap());
            map.put("hasResource", true);
        } else {
            map.put("hasResource", false);
        }
        
        List<Map<String, Object>> buffsList = new ArrayList<>();
        for (BuffDebuff buff : buffs) {
            buffsList.add(buff.toMap());
        }
        map.put("buffs", buffsList);
        
        List<Map<String, Object>> debuffsList = new ArrayList<>();
        for (BuffDebuff debuff : debuffs) {
            debuffsList.add(debuff.toMap());
        }
        map.put("debuffs", debuffsList);
        
        map.put("isPlayer", isPlayer);
        map.put("isHostile", isHostile);
        map.put("inCombat", inCombat);
        map.put("isDead", isDead);
        
        return map;
    }
    
    /**
     * Create a new builder.
     * 
     * @param unitId the unit ID
     * @param name the display name
     * @return a new builder instance
     */
    public static Builder builder(String unitId, String name) {
        return new Builder(unitId, name);
    }
    
    /**
     * Represents a buff or debuff effect.
     */
    public static class BuffDebuff {
        private final String id;
        private final String name;
        private final String iconUrl;
        private final int stacks;
        private final float remainingDuration;
        private final boolean isBuff;
        
        /**
         * Create a new buff/debuff.
         * 
         * @param id effect ID
         * @param name display name
         * @param iconUrl icon resource
         * @param stacks stack count
         * @param remainingDuration seconds remaining
         * @param isBuff true if buff, false if debuff
         */
        public BuffDebuff(String id, String name, String iconUrl, int stacks, 
                         float remainingDuration, boolean isBuff) {
            this.id = id;
            this.name = name;
            this.iconUrl = iconUrl;
            this.stacks = stacks;
            this.remainingDuration = remainingDuration;
            this.isBuff = isBuff;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("iconUrl", iconUrl);
            map.put("stacks", stacks);
            map.put("hasStacks", stacks > 1);
            map.put("remainingDuration", remainingDuration);
            map.put("isBuff", isBuff);
            return map;
        }
        
        public static BuffDebuff buff(String id, String name, String iconUrl, int stacks, float duration) {
            return new BuffDebuff(id, name, iconUrl, stacks, duration, true);
        }
        
        public static BuffDebuff debuff(String id, String name, String iconUrl, int stacks, float duration) {
            return new BuffDebuff(id, name, iconUrl, stacks, duration, false);
        }
    }
    
    /**
     * Builder for UnitFrame.
     */
    public static class Builder {
        private final String unitId;
        private final String name;
        private int level = 1;
        private String portraitUrl;
        private String faction;
        private ResourceBar health;
        private ResourceBar resource;
        private final List<BuffDebuff> buffs = new ArrayList<>();
        private final List<BuffDebuff> debuffs = new ArrayList<>();
        private boolean isPlayer = false;
        private boolean isHostile = false;
        private boolean inCombat = false;
        private boolean isDead = false;
        
        private Builder(String unitId, String name) {
            this.unitId = unitId;
            this.name = name;
        }
        
        public Builder withLevel(int level) {
            this.level = level;
            return this;
        }
        
        public Builder withPortrait(String portraitUrl) {
            this.portraitUrl = portraitUrl;
            return this;
        }
        
        public Builder withFaction(String faction) {
            this.faction = faction;
            return this;
        }
        
        public Builder withHealth(int current, int maximum) {
            this.health = ResourceBar.health(current, maximum);
            return this;
        }
        
        public Builder withResource(String type, int current, int maximum) {
            this.resource = new ResourceBar(type, current, maximum);
            return this;
        }
        
        public Builder addBuff(BuffDebuff buff) {
            this.buffs.add(buff);
            return this;
        }
        
        public Builder addDebuff(BuffDebuff debuff) {
            this.debuffs.add(debuff);
            return this;
        }
        
        public Builder asPlayer() {
            this.isPlayer = true;
            return this;
        }
        
        public Builder asHostile() {
            this.isHostile = true;
            return this;
        }
        
        public Builder inCombat(boolean inCombat) {
            this.inCombat = inCombat;
            return this;
        }
        
        public Builder dead(boolean isDead) {
            this.isDead = isDead;
            return this;
        }
        
        public UnitFrame build() {
            return new UnitFrame(this);
        }
    }
}
