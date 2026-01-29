package com.argonathsystems.framework.ui.dialogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data object for quest offer display in dialogue UI.
 * 
 * <p>Contains all quest information needed to render the quest offer panel:
 * <ul>
 *   <li>Quest name and description
 *   <li>Objective list
 *   <li>Reward items
 *   <li>XP and gold rewards
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public class QuestOfferData {
    
    private final String name;
    private final List<String> objectives;
    private final List<RewardItem> rewards;
    private final Integer xpReward;
    private final Integer goldReward;
    
    private QuestOfferData(Builder builder) {
        this.name = builder.name;
        this.objectives = List.copyOf(builder.objectives);
        this.rewards = List.copyOf(builder.rewards);
        this.xpReward = builder.xpReward;
        this.goldReward = builder.goldReward;
    }
    
    /**
     * Get the quest name.
     * 
     * @return Quest name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the list of quest objectives.
     * 
     * @return Objective text list
     */
    public List<String> getObjectives() {
        return objectives;
    }
    
    /**
     * Get the list of reward items.
     * 
     * @return Reward items
     */
    public List<RewardItem> getRewards() {
        return rewards;
    }
    
    /**
     * Get the XP reward amount.
     * 
     * @return XP amount or null if no XP reward
     */
    public Integer getXpReward() {
        return xpReward;
    }
    
    /**
     * Get the gold reward amount.
     * 
     * @return Gold amount or null if no gold reward
     */
    public Integer getGoldReward() {
        return goldReward;
    }
    
    /**
     * Convert this quest offer to a map for template processing.
     * 
     * @return Map of quest properties
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        
        List<Map<String, String>> objectivesList = new ArrayList<>();
        for (String objective : objectives) {
            Map<String, String> objMap = new HashMap<>();
            objMap.put("text", objective);
            objectivesList.add(objMap);
        }
        map.put("objectives", objectivesList);
        
        List<Map<String, Object>> rewardsList = new ArrayList<>();
        for (RewardItem reward : rewards) {
            rewardsList.add(reward.toMap());
        }
        map.put("rewards", rewardsList);
        
        map.put("xpReward", xpReward);
        map.put("goldReward", goldReward);
        
        return map;
    }
    
    /**
     * Create a new builder for quest offer data.
     * 
     * @param name Quest name
     * @return New builder
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    /**
     * Builder for {@link QuestOfferData}.
     */
    public static class Builder {
        private final String name;
        private final List<String> objectives = new ArrayList<>();
        private final List<RewardItem> rewards = new ArrayList<>();
        private Integer xpReward;
        private Integer goldReward;
        
        private Builder(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Quest name cannot be null or empty");
            }
            this.name = name;
        }
        
        /**
         * Add an objective to the quest.
         * 
         * @param objective Objective text
         * @return this builder
         */
        public Builder addObjective(String objective) {
            this.objectives.add(objective);
            return this;
        }
        
        /**
         * Add a reward item.
         * 
         * @param itemId Item ID
         * @param name Item name
         * @param quantity Item quantity
         * @return this builder
         */
        public Builder addReward(String itemId, String name, int quantity) {
            this.rewards.add(new RewardItem(itemId, name, quantity));
            return this;
        }
        
        /**
         * Set the XP reward.
         * 
         * @param xp XP amount
         * @return this builder
         */
        public Builder withXpReward(int xp) {
            this.xpReward = xp;
            return this;
        }
        
        /**
         * Set the gold reward.
         * 
         * @param gold Gold amount
         * @return this builder
         */
        public Builder withGoldReward(int gold) {
            this.goldReward = gold;
            return this;
        }
        
        /**
         * Build the quest offer data.
         * 
         * @return Quest offer data
         */
        public QuestOfferData build() {
            return new QuestOfferData(this);
        }
    }
    
    /**
     * Represents a reward item in a quest offer.
     */
    public static class RewardItem {
        private final String itemId;
        private final String name;
        private final int quantity;
        
        /**
         * Create a new reward item.
         * 
         * @param itemId Item ID
         * @param name Item name
         * @param quantity Item quantity
         */
        public RewardItem(String itemId, String name, int quantity) {
            this.itemId = itemId;
            this.name = name;
            this.quantity = quantity;
        }
        
        public String getItemId() {
            return itemId;
        }
        
        public String getName() {
            return name;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        /**
         * Convert to map for template processing.
         * 
         * @return Map of item properties
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", itemId);
            map.put("name", name);
            map.put("quantity", quantity);
            return map;
        }
    }
}
