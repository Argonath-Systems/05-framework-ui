package com.argonathsystems.framework.ui.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quest category for quest book sidebar organization.
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public class QuestCategory {
    
    private final String name;
    private final List<QuestListItem> quests;
    
    public QuestCategory(String name) {
        this.name = name;
        this.quests = new ArrayList<>();
    }
    
    public void addQuest(QuestListItem quest) {
        this.quests.add(quest);
    }
    
    public String getName() {
        return name;
    }
    
    public List<QuestListItem> getQuests() {
        return List.copyOf(quests);
    }
    
    public int getCount() {
        return quests.size();
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("count", quests.size());
        
        List<Map<String, Object>> questsList = new ArrayList<>();
        for (QuestListItem quest : quests) {
            questsList.add(quest.toMap());
        }
        map.put("quests", questsList);
        
        return map;
    }
}
