package com.argonathsystems.framework.ui.quest;

import java.util.HashMap;
import java.util.Map;

/**
 * Quest list item for sidebar display.
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public class QuestListItem {
    
    private final String id;
    private final String name;
    private final int level;
    private final String type;
    private final String iconUrl;
    
    public QuestListItem(String id, String name, int level, String type, String iconUrl) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.type = type;
        this.iconUrl = iconUrl;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getType() {
        return type;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("level", level);
        map.put("type", type);
        map.put("iconUrl", iconUrl);
        return map;
    }
}
