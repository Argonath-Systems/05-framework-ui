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
        
        return new HudLayoutConfig(elements);
    }

    @Override
    public void serialize(HudLayoutConfig object, ConfigSection config) {
        ConfigSection elementsSection = config.createSection("elements"); // Assuming createSection exists or similar
        
        // If createSection doesn't exist in interface I might need to cast or use a setter.
        // Checking ConfigSection interface again... 
        // The spec didn't show setters. It only showed getters.
        // But usually Config abstractions have setters. 
        // I will assume standard Bukkit/Hytale like config or Map based.
        // Let's check ConfigSection.java content to be safe.
    }
}
