package com.argonathsystems.framework.ui.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quest detail data for detail panel display.
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public class QuestDetail {
    
    private final String id;
    private final String name;
    private final int level;
    private final String type;
    private final String giver;
    private final String description;
    private final List<ObjectiveItem> objectives;
    private final List<RewardItem> rewards;
    private final Integer xpReward;
    private final Integer goldReward;
    
    private QuestDetail(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.level = builder.level;
        this.type = builder.type;
        this.giver = builder.giver;
        this.description = builder.description;
        this.objectives = List.copyOf(builder.objectives);
        this.rewards = List.copyOf(builder.rewards);
        this.xpReward = builder.xpReward;
        this.goldReward = builder.goldReward;
    }
    
    public String getId() {
        return id;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("level", level);
        map.put("type", type);
        map.put("giver", giver);
        map.put("description", description);
        
        List<Map<String, Object>> objectivesList = new ArrayList<>();
        for (ObjectiveItem obj : objectives) {
            objectivesList.add(obj.toMap());
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
    
    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }
    
    public static class Builder {
        private final String id;
        private final String name;
        private int level;
        private String type;
        private String giver;
        private String description;
        private final List<ObjectiveItem> objectives = new ArrayList<>();
        private final List<RewardItem> rewards = new ArrayList<>();
        private Integer xpReward;
        private Integer goldReward;
        
        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Builder withLevel(int level) {
            this.level = level;
            return this;
        }
        
        public Builder withType(String type) {
            this.type = type;
            return this;
        }
        
        public Builder withGiver(String giver) {
            this.giver = giver;
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder addObjective(String text, boolean completed, Integer current, Integer required) {
            this.objectives.add(new ObjectiveItem(text, completed, current, required));
            return this;
        }
        
        public Builder addReward(String itemId, String name, int quantity) {
            this.rewards.add(new RewardItem(itemId, name, quantity));
            return this;
        }
        
        public Builder withXpReward(int xp) {
            this.xpReward = xp;
            return this;
        }
        
        public Builder withGoldReward(int gold) {
            this.goldReward = gold;
            return this;
        }
        
        public QuestDetail build() {
            return new QuestDetail(this);
        }
    }
    
    public static class ObjectiveItem {
        private final String text;
        private final boolean completed;
        private final Integer current;
        private final Integer required;
        
        public ObjectiveItem(String text, boolean completed, Integer current, Integer required) {
            this.text = text;
            this.completed = completed;
            this.current = current;
            this.required = required;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("text", text);
            map.put("completed", completed);
            if (current != null && required != null) {
                Map<String, Integer> progress = new HashMap<>();
                progress.put("current", current);
                progress.put("required", required);
                map.put("progress", progress);
            }
            return map;
        }
    }
    
    public static class RewardItem {
        private final String itemId;
        private final String name;
        private final int quantity;
        
        public RewardItem(String itemId, String name, int quantity) {
            this.itemId = itemId;
            this.name = name;
            this.quantity = quantity;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", itemId);
            map.put("name", name);
            map.put("quantity", quantity);
            return map;
        }
    }
}
