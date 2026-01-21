package com.argonathsystems.framework.ui;

import com.argonathsystems.framework.accessorapi.UIAccessor;
import com.argonathsystems.framework.ui.layout.HudLayoutConfig;
import com.argonathsystems.framework.ui.layout.HudLayoutManager;
import com.argonathsystems.framework.ui.layout.HudLayoutSerializer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform-agnostic manager for the Unified UI System.
 * Handles app registration and navigation logic.
 */
public class UnifiedUIManager {
    private static final UnifiedUIManager INSTANCE = new UnifiedUIManager();
    private final Map<String, AppManifest> apps = new ConcurrentHashMap<>();
    private UIAccessor accessor;

    private UnifiedUIManager() {}

    public static UnifiedUIManager getInstance() {
        return INSTANCE;
    }

    public void init(UIAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Register a new application to the main dashboard.
     * @param manifest The application manifest.
     */
    public void registerApp(AppManifest manifest) {
        apps.put(manifest.id(), manifest);
    }

    public Collection<AppManifest> getApps() {
        return apps.values();
    }

    public Optional<AppManifest> getApp(String id) {
        return Optional.ofNullable(apps.get(id));
    }

    /**
     * Opens the Main Dashboard for a player.
     */
    public void openDashboard(UUID playerId) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        
        // Pass the list of apps as context so the UI can render the grid
        accessor.openUI(playerId, "unified_dashboard", apps.values());
    }

    /**
     * Launch a specific app.
     */
    public void launchApp(UUID playerId, String appId) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        
        AppManifest app = apps.get(appId);
        if (app != null) {
            accessor.openUI(playerId, app.entryScreenId(), null);
        }
    }

    /**
     * Initialize the HUD for a player.
     * Should be called on player join.
     */
    public void initializeHud(UUID playerId) {
        if (accessor == null) return;
        
        // Load persistency
        HudLayoutManager.getInstance().loadLayout(playerId);
        
        // Add the shortcut helper
        accessor.addHud(playerId, "shortcut_helper", "resource:/ui/hud_shortcut_helper.xaml");
    }

    /**
     * Enter HUD Edit mode.
     */
    public void enterEditMode(UUID playerId) {
         if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
         accessor.openHudEditor(playerId);
    }
    
    /**
     * Save a new HUD layout.
     */
    public void saveLayout(UUID playerId, HudLayoutConfig config) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        
        HudLayoutManager.getInstance().setLayout(playerId, config);
        
        // Serialize and sync to client (adapter handles the actual application of coords)
        HudLayoutSerializer serializer = new HudLayoutSerializer();
        Map<String, Object> serialized = serializer.serialize(config);
        
        accessor.updateHudLayout(playerId, serialized);
        
        // TODO: Persist to disk via Core Lib Config
    }
}
