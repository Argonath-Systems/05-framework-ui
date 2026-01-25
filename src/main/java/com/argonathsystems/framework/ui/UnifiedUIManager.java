package com.argonathsystems.framework.ui;

import com.argonathsystems.framework.accessorapi.UIAccessor;
import com.argonathsystems.framework.ui.hud.KeybindHintsHUD;
import com.argonathsystems.framework.ui.layout.HudLayoutConfig;
import com.argonathsystems.framework.ui.layout.HudLayoutManager;
import com.argonathsystems.framework.ui.layout.HudLayoutSerializer;
import com.argonathsystems.framework.ui.menu.MainMenuManager;
import com.argonathsystems.framework.ui.menu.MenuTab;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform-agnostic manager for the Unified UI System.
 * Handles app registration, navigation logic, and HUD edit mode management.
 * 
 * <p>Key handling is delegated to {@link MainMenuManager} for menu-related keys.
 */
public class UnifiedUIManager {
    private static final UnifiedUIManager INSTANCE = new UnifiedUIManager();
    private final Map<String, AppManifest> apps = new ConcurrentHashMap<>();
    private final Set<UUID> playersInEditMode = ConcurrentHashMap.newKeySet();
    private UIAccessor accessor;

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
            HudLayoutSerializer serializer = new HudLayoutSerializer();
            Map<String, Object> serialized = serializer.serialize(defaultConfig);
            accessor.updateHudLayout(playerId, serialized);
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
        HudLayoutSerializer serializer = new HudLayoutSerializer();
        Map<String, Object> serialized = serializer.serialize(config);
        
        accessor.updateHudLayout(playerId, serialized);
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
