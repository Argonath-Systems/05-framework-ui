package com.argonathsystems.framework.ui;

import com.argonathsystems.framework.accessorapi.UIAccessor;
import com.argonathsystems.framework.accessorapi.ui.HudLayoutData;
import com.argonathsystems.framework.ui.dev.DevModeConfig;
import com.argonathsystems.framework.ui.dev.UIHotReloadService;
import com.argonathsystems.framework.ui.hud.KeybindHintsHUD;
import com.argonathsystems.framework.ui.layout.HudLayoutConfig;
import com.argonathsystems.framework.ui.layout.HudLayoutManager;
import com.argonathsystems.framework.ui.layout.HudLayoutSerializer;
import com.argonathsystems.framework.ui.menu.MainMenuManager;
import com.argonathsystems.framework.ui.menu.MenuTab;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Platform-agnostic manager for the Unified UI System.
 * Handles app registration, navigation logic, HUD edit mode management,
 * and development features like UI hot reload.
 * 
 * <p>Key handling is delegated to {@link MainMenuManager} for menu-related keys.
 * 
 * <h2>Hot Reload (Development Mode)</h2>
 * <p>When development mode is enabled, this manager provides UI hot reloading:
 * <pre>{@code
 * // Initialize with dev mode
 * UnifiedUIManager.getInstance().init(accessor);
 * UnifiedUIManager.getInstance().initDevMode(DevModeConfig.development());
 * 
 * // Create hot-reloadable UI supplier
 * Supplier<String> supplier = UnifiedUIManager.getInstance()
 *     .createUISupplier("quest-tracker", "ui/pages/quest-tracker.hyuiml");
 * 
 * // Use with PageBuilder
 * PageBuilder.fromHtml(supplier.get()).open(store);
 * }</pre>
 * 
 * @author Argonath Systems
 * @since 1.0.0
 */
public class UnifiedUIManager {
    
    private static final Logger LOG = Logger.getLogger(UnifiedUIManager.class.getName());
    
    private static final UnifiedUIManager INSTANCE = new UnifiedUIManager();
    private final Map<String, AppManifest> apps = new ConcurrentHashMap<>();
    private final Set<UUID> playersInEditMode = ConcurrentHashMap.newKeySet();
    private UIAccessor accessor;
    
    // === Hot Reload Support ===
    /** Development hot reload service (null if disabled) */
    private UIHotReloadService hotReloadService;
    
    /** Development mode configuration */
    private DevModeConfig devConfig;
    
    /** Track open UI instances per player for refresh targeting */
    private final Map<UUID, Set<String>> playerOpenUIs = new ConcurrentHashMap<>();

    /** Default keybind for HUD edit toggle */
    public static final String DEFAULT_EDIT_KEYBIND = "KEY_F7";

    private UnifiedUIManager() {}

    public static UnifiedUIManager getInstance() {
        return INSTANCE;
    }

    public void init(UIAccessor accessor) {
        this.accessor = accessor;
        // Initialize the main menu manager with the same accessor
        MainMenuManager.getInstance().init(accessor);
    }
    
    // ========== Development Mode / Hot Reload ==========
    
    /**
     * Initialize development mode features including UI hot reload.
     * Call this after {@link #init(UIAccessor)} during server startup.
     * 
     * <p>Safety: Hot reload will be force-disabled if the environment
     * variable {@code ARGONATH_ENV} is set to "production".
     * 
     * @param devConfig Development mode configuration
     */
    public void initDevMode(DevModeConfig devConfig) {
        this.devConfig = devConfig;
        
        if (devConfig == null || !devConfig.isHotReloadActive()) {
            LOG.info("UI Hot Reload is disabled by configuration");
            return;
        }
        
        // Safety check: NEVER enable in production environment
        String env = System.getenv("ARGONATH_ENV");
        if ("production".equals(env)) {
            LOG.warning("UI Hot Reload requested but ARGONATH_ENV=production. Ignoring.");
            return;
        }
        
        try {
            this.hotReloadService = new UIHotReloadService(
                devConfig.uiDirectory(),
                devConfig.pollIntervalMs(),
                devConfig.logChanges()
            );
            
            // Register listener for auto-refresh
            if (devConfig.autoRefreshPlayers()) {
                this.hotReloadService.onReload(this::handleUIFileChange);
            }
            
            LOG.info("UI Hot Reload enabled. Watching: " + devConfig.uiDirectory());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to initialize UI hot reload: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shutdown development mode features.
     * Called automatically on server shutdown if initialized.
     */
    public void shutdownDevMode() {
        if (hotReloadService != null) {
            hotReloadService.close();
            hotReloadService = null;
            LOG.info("UI Hot Reload service stopped");
        }
    }
    
    /**
     * Check if UI hot reload is currently active.
     * 
     * @return true if hot reload is enabled and running
     */
    public boolean isHotReloadEnabled() {
        return hotReloadService != null && hotReloadService.isRunning();
    }
    
    /**
     * Get the hot reload service if available.
     * 
     * <p>Use this to register custom reload listeners or access advanced features.
     * 
     * @return Optional containing the hot reload service, or empty if not enabled
     */
    public Optional<UIHotReloadService> getHotReloadService() {
        return Optional.ofNullable(hotReloadService);
    }
    
    /**
     * Create a content supplier for dynamic UI loading.
     * 
     * <p>In development mode (hot reload enabled), the supplier reads
     * from the watched directory on each call, enabling live updates.
     * 
     * <p>In production mode, the supplier loads once from the classpath
     * and caches the result for performance.
     * 
     * @param pageId The page identifier (used for hot reload lookup)
     * @param fallbackClasspath Classpath resource to use in production
     * @return Supplier that provides HYUIML content
     */
    public Supplier<String> createUISupplier(String pageId, String fallbackClasspath) {
        if (hotReloadService != null) {
            // Development: use hot reload service
            return hotReloadService.createSupplier(pageId);
        }
        
        // Production: load once from classpath and cache
        String cached = loadFromClasspath(fallbackClasspath);
        return () -> cached;
    }
    
    /**
     * Force reload UI files from disk.
     * Only works when hot reload is enabled.
     * 
     * @param pageId Specific page ID to reload, or "all" for all files
     * @param refreshPlayers Whether to refresh currently open player UIs
     * @return Number of files reloaded
     * @throws IllegalStateException if hot reload is not enabled
     */
    public int reloadUI(String pageId, boolean refreshPlayers) {
        if (hotReloadService == null) {
            throw new IllegalStateException("UI Hot Reload is not enabled");
        }
        
        int count;
        if ("all".equals(pageId)) {
            count = hotReloadService.reloadAll();
            if (refreshPlayers) {
                refreshAllPlayerUIs();
            }
        } else {
            count = hotReloadService.reload(pageId) ? 1 : 0;
            if (refreshPlayers) {
                refreshPlayerUIs(pageId);
            }
        }
        
        return count;
    }
    
    /**
     * Get all page IDs currently registered in the hot reload service.
     * 
     * @return Set of page IDs, or empty set if hot reload is disabled
     */
    public Set<String> getRegisteredPageIds() {
        if (hotReloadService != null) {
            return hotReloadService.getRegisteredPageIds();
        }
        return Set.of();
    }
    
    /**
     * Track when a player opens a UI.
     * Used for targeted refresh when UI files change.
     * 
     * @param playerId The player who opened the UI
     * @param uiId The UI identifier
     */
    public void trackUIOpen(UUID playerId, String uiId) {
        playerOpenUIs.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                     .add(uiId);
    }
    
    /**
     * Track when a player closes a UI.
     * 
     * @param playerId The player who closed the UI
     * @param uiId The UI identifier
     */
    public void trackUIClose(UUID playerId, String uiId) {
        Set<String> uis = playerOpenUIs.get(playerId);
        if (uis != null) {
            uis.remove(uiId);
        }
    }
    
    /**
     * Check which UIs a player currently has open.
     * 
     * @param playerId The player to check
     * @return Set of open UI IDs, or empty set if none
     */
    public Set<String> getOpenUIs(UUID playerId) {
        Set<String> uis = playerOpenUIs.get(playerId);
        return uis != null ? Set.copyOf(uis) : Set.of();
    }
    
    // --- Private Hot Reload Helpers ---
    
    private void handleUIFileChange(String pageId) {
        LOG.fine("UI file changed: " + pageId);
        
        // Auto-refresh if configured
        if (devConfig != null && devConfig.autoRefreshPlayers()) {
            refreshPlayerUIs(pageId);
        }
    }
    
    private void refreshPlayerUIs(String pageId) {
        int refreshed = 0;
        
        for (Map.Entry<UUID, Set<String>> entry : playerOpenUIs.entrySet()) {
            if (entry.getValue().contains(pageId)) {
                UUID playerId = entry.getKey();
                
                // Close the current UI
                if (accessor != null) {
                    accessor.closeUI(playerId);
                    refreshed++;
                    
                    // Note: The UI should be reopened by the caller using the supplier
                    // which will now return the updated content.
                    // We remove from tracking since it's now closed.
                    entry.getValue().remove(pageId);
                }
            }
        }
        
        if (refreshed > 0) {
            LOG.info("Refreshed UI '" + pageId + "' for " + refreshed + " player(s)");
        }
    }
    
    private void refreshAllPlayerUIs() {
        Set<String> allPageIds = getRegisteredPageIds();
        for (String pageId : allPageIds) {
            refreshPlayerUIs(pageId);
        }
    }
    
    private String loadFromClasspath(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOG.warning("Resource not found on classpath: " + resourcePath);
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to load resource: " + resourcePath, e);
            return "";
        }
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
     * Opens the Main Menu for a player.
     * @param playerId The player to show the menu to
     */
    public void openMainMenu(UUID playerId) {
        MainMenuManager.getInstance().openMenu(playerId, null);
    }
    
    /**
     * Opens the Main Menu to a specific tab.
     * @param playerId The player to show the menu to
     * @param tab The tab to open to
     */
    public void openMainMenu(UUID playerId, MenuTab tab) {
        MainMenuManager.getInstance().openMenu(playerId, tab);
    }
    
    /**
     * Close the main menu.
     */
    public void closeMainMenu(UUID playerId) {
        MainMenuManager.getInstance().closeMenu(playerId);
    }

    /**
     * Opens the Main Dashboard for a player.
     * @deprecated Use {@link #openMainMenu(UUID)} instead
     */
    @Deprecated
    public void openDashboard(UUID playerId) {
        openMainMenu(playerId);
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
        
        // Add the shortcut helper (shows F7 keybind hint)
        accessor.addHud(playerId, "shortcut_helper", "resource:/ui/hud_shortcut_helper.xaml");
        
        // Add keybind hints HUD (bottom-left corner)
        String keybindHintsHyuiml = KeybindHintsHUD.getInstance().generateHyuiml();
        accessor.addHud(playerId, "keybind_hints", keybindHintsHyuiml);
    }

    /**
     * Enter HUD Edit mode.
     */
    public void enterEditMode(UUID playerId) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        if (playersInEditMode.contains(playerId)) return; // Already in edit mode
        
        playersInEditMode.add(playerId);
        accessor.openHudEditor(playerId);
    }

    /**
     * Exit HUD Edit mode.
     */
    public void exitEditMode(UUID playerId) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        if (!playersInEditMode.contains(playerId)) return; // Not in edit mode
        
        playersInEditMode.remove(playerId);
        accessor.closeHudEditor(playerId);
    }

    /**
     * Toggle HUD Edit mode on/off.
     */
    public void toggleEditMode(UUID playerId) {
        if (isInEditMode(playerId)) {
            exitEditMode(playerId);
        } else {
            enterEditMode(playerId);
        }
    }

    /**
     * Check if a player is currently in HUD edit mode.
     * @param playerId The player to check
     * @return true if in edit mode
     */
    public boolean isInEditMode(UUID playerId) {
        return playersInEditMode.contains(playerId);
    }

    /**
     * Handle keybind press for HUD editing and menu navigation.
     * Should be called from event listener on key press.
     * 
     * @param playerId The player who pressed the key
     * @param key The key that was pressed (e.g., "KEY_F7", "G", "M")
     * @return true if the key was handled
     */
    public boolean handleKeyPress(UUID playerId, String key) {
        // Check for HUD edit toggle first
        if (DEFAULT_EDIT_KEYBIND.equals(key)) {
            toggleEditMode(playerId);
            return true;
        }
        
        // Delegate to MainMenuManager for menu-related keys
        // Extract just the key letter if it's in KEY_X format
        String keyLetter = key;
        if (key != null && key.startsWith("KEY_")) {
            keyLetter = key.substring(4);
        }
        
        return MainMenuManager.getInstance().handleKeyPress(playerId, keyLetter);
    }

    /**
     * Reset HUD layout to defaults for a player.
     */
    public void resetLayout(UUID playerId) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        
        HudLayoutManager.getInstance().resetToDefault(playerId);
        
        // Sync reset layout to client
        HudLayoutConfig defaultConfig = HudLayoutManager.getInstance().getLayout(playerId);
        if (defaultConfig != null) {
            HudLayoutData layoutData = convertToHudLayoutData(defaultConfig);
            accessor.updateHudLayout(playerId, layoutData);
        }
    }

    /**
     * Force save the current HUD layout.
     */
    public void forceSaveLayout(UUID playerId) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        
        HudLayoutConfig config = HudLayoutManager.getInstance().getLayout(playerId);
        if (config != null) {
            HudLayoutManager.getInstance().setLayout(playerId, config);
        }
    }

    /**
     * Called when a player disconnects to clean up state.
     */
    public void onPlayerQuit(UUID playerId) {
        playersInEditMode.remove(playerId);
        playerOpenUIs.remove(playerId);  // Clean up UI tracking
        MainMenuManager.getInstance().onPlayerQuit(playerId);
    }
    
    /**
     * Save a new HUD layout.
     */
    public void saveLayout(UUID playerId, HudLayoutConfig config) {
        if (accessor == null) throw new IllegalStateException("UnifiedUIManager not initialized with Accessor");
        
        // Persist to disk via HudLayoutManager (uses JsonFileRepository internally)
        HudLayoutManager.getInstance().setLayout(playerId, config);
        
        // Serialize and sync to client (adapter handles the actual application of coords)
        HudLayoutData layoutData = convertToHudLayoutData(config);
        accessor.updateHudLayout(playerId, layoutData);
    }
    
    /**
     * Convert framework's HudLayoutConfig to accessor's HudLayoutData.
     * Maps between the two different HudElementPosition representations.
     */
    private HudLayoutData convertToHudLayoutData(HudLayoutConfig config) {
        Map<String, HudLayoutData.HudElementPosition> elements = new HashMap<>();
        
        // Base dimensions for HUD elements (scaled by element scale factor)
        final int BASE_WIDTH = 100;
        final int BASE_HEIGHT = 50;
        
        for (Map.Entry<String, com.argonathsystems.framework.ui.layout.HudElementPosition> entry : config.elements().entrySet()) {
            com.argonathsystems.framework.ui.layout.HudElementPosition frameworkPos = entry.getValue();
            
            // Convert from framework format (float x, y, scale) to accessor format (int x, y, width, height)
            // Width/height are derived from base dimensions scaled by the element's scale factor
            int scaledWidth = (int) (BASE_WIDTH * frameworkPos.scale());
            int scaledHeight = (int) (BASE_HEIGHT * frameworkPos.scale());
            
            HudLayoutData.HudElementPosition accessorPos = new HudLayoutData.HudElementPosition(
                (int) frameworkPos.x(),
                (int) frameworkPos.y(),
                scaledWidth,
                scaledHeight,
                frameworkPos.anchor(),
                frameworkPos.visible()
            );
            
            elements.put(entry.getKey(), accessorPos);
        }
        
        return new HudLayoutData(elements);
    }
    
    /**
     * Refresh the keybind hints HUD for a player.
     * Call this after adding/removing contextual hints.
     * 
     * @param playerId The player to refresh hints for
     */
    public void refreshKeybindHints(UUID playerId) {
        if (accessor == null) return;
        
        String keybindHintsHyuiml = KeybindHintsHUD.getInstance().generateHyuiml();
        accessor.updateHud(playerId, "keybind_hints", keybindHintsHyuiml);
    }
    
    /**
     * Set a contextual keybind hint (e.g., "Press E to interact").
     * The hint will be displayed at the top of the keybind hints HUD.
     * 
     * @param playerId The player to show the hint to
     * @param hintId Unique identifier for this hint
     * @param key The key to display (e.g., "E", "F")
     * @param action The action description (e.g., "Interact", "Talk")
     */
    public void setContextualHint(UUID playerId, String hintId, String key, String action) {
        KeybindHintsHUD.getInstance().setContextualHint(hintId, 
            KeybindHintsHUD.KeybindHint.interaction(key, action));
        refreshKeybindHints(playerId);
    }
    
    /**
     * Remove a contextual keybind hint.
     * 
     * @param playerId The player to remove the hint from
     * @param hintId The identifier of the hint to remove
     */
    public void removeContextualHint(UUID playerId, String hintId) {
        KeybindHintsHUD.getInstance().removeContextualHint(hintId);
        refreshKeybindHints(playerId);
    }
}
