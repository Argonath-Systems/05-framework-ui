package com.argonathsystems.framework.ui.layout;

import com.argonathsystems.framework.storage.json.JsonFileRepository;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HudLayoutManager {
    private static final HudLayoutManager INSTANCE = new HudLayoutManager();
    private final Map<UUID, HudLayoutConfig> layouts = new ConcurrentHashMap<>();
    private final JsonFileRepository<HudLayoutConfig, UUID> repository;
    
    private HudLayoutManager() {
        // Initialize repository at config/hud_layouts/
        this.repository = new JsonFileRepository<>(
            HudLayoutConfig.class,
            Paths.get("config", "hud_layouts"),
            HudLayoutConfig::playerId
        );
    }
    
    public static HudLayoutManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Load layout from disk asynchronously.
     */
    public void loadLayout(UUID playerId) {
        repository.findById(playerId).thenAccept(opt -> {
            if (opt.isPresent()) {
                layouts.put(playerId, opt.get());
            }
        });
    }

    public HudLayoutConfig getLayout(UUID playerId) {
        return layouts.getOrDefault(playerId, new HudLayoutConfig(playerId, Map.of()));
    }
    
    public void setLayout(UUID playerId, HudLayoutConfig config) {
        // Ensure the config has the correct playerId
        HudLayoutConfig configWithId = new HudLayoutConfig(playerId, config.elements());
        layouts.put(playerId, configWithId);
        repository.save(configWithId);
    }

    /**
     * Reset a player's HUD layout to the default configuration.
     * @param playerId The player whose layout to reset
     */
    public void resetToDefault(UUID playerId) {
        // Create a fresh default config with no custom element positions
        HudLayoutConfig defaultConfig = new HudLayoutConfig(playerId, Map.of());
        layouts.put(playerId, defaultConfig);
        repository.save(defaultConfig);
    }

    /**
     * Remove a player's layout from memory (e.g., on disconnect).
     */
    public void unloadLayout(UUID playerId) {
        layouts.remove(playerId);
    }
}
