package com.argonathsystems.framework.ui;

import com.argonathsystems.framework.accessorapi.UIAccessor;
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
}
