package com.argonathsystems.framework.ui.layout;

import com.argonathsystems.framework.core.config.ConfigSection;
import com.argonathsystems.framework.core.config.ConfigSerializer;
import java.util.HashMap;
import java.util.Map;

public class HudLayoutSerializer implements ConfigSerializer<HudLayoutConfig> {

    @Override
    public HudLayoutConfig deserialize(ConfigSection config) {
        Map<String, HudElementPosition> elements = new HashMap<>();
        
        ConfigSection elementsSection = config.getSection("elements");
        if (elementsSection != null) {
            for (String key : elementsSection.getKeys()) {
                ConfigSection elConfig = elementsSection.getSection(key);
                HudElementPosition pos = new HudElementPosition(
                    key,
                    (float) elConfig.getDouble("x", 0.0),
                    (float) elConfig.getDouble("y", 0.0),
                    elConfig.getString("anchor", "TOP_LEFT"),
                    (float) elConfig.getDouble("scale", 1.0),
                    elConfig.getBoolean("visible", true)
                );
                elements.put(key, pos);
            }
        }
        
        // Note: UUID is not typically in the config section for layout, 
        // but we default to null or read it if present.
        // This method works for config-lib usages.
        return new HudLayoutConfig(null, elements);
    }

    @Override
    public Map<String, Object> serialize(HudLayoutConfig object) {
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> elementsMap = new HashMap<>();
        
        for (Map.Entry<String, HudElementPosition> entry : object.elements().entrySet()) {
            HudElementPosition pos = entry.getValue();
            Map<String, Object> elData = new HashMap<>();
            elData.put("x", pos.x());
            elData.put("y", pos.y());
            elData.put("anchor", pos.anchor());
            elData.put("scale", pos.scale());
            elData.put("visible", pos.visible());
            
            elementsMap.put(entry.getKey(), elData);
        }
        
        root.put("elements", elementsMap);
        return root;
    }
}
