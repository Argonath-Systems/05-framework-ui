package com.argonathsystems.framework.ui;

import com.argonathsystems.framework.ui.layout.HudLayoutManager;
import com.argonathsystems.framework.ui.menu.MainMenuManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * Hytale plugin entry point for the UI Framework.
 * 
 * <p>This class is responsible for initializing the platform-agnostic 
 * UI systems when loaded by the Hytale server.</p>
 */
public class UiFrameworkPlugin extends JavaPlugin {
    
    private static UiFrameworkPlugin instance;
    
    public UiFrameworkPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }
    
    public static UiFrameworkPlugin getInstance() {
        return instance;
    }
    
    @Override
    protected void setup() {
        getLogger().atInfo().log("Initializing UI Framework...");
        
        // HudLayoutManager and MainMenuManager are singletons
        // They are accessed via their getInstance() methods
        getLogger().atInfo().log("UI Framework - HudLayoutManager available.");
        getLogger().atInfo().log("UI Framework - MainMenuManager available.");
        
        getLogger().atInfo().log("UI Framework initialized successfully.");
    }
    
    @Override
    protected void shutdown() {
        getLogger().atInfo().log("Shutting down UI Framework...");
    }
    
    /**
     * Gets the HUD Layout Manager instance.
     * @return the hud layout manager singleton
     */
    public HudLayoutManager getHudLayoutManager() {
        return HudLayoutManager.getInstance();
    }
    
    /**
     * Gets the Main Menu Manager instance.
     * @return the main menu manager singleton
     */
    public MainMenuManager getMainMenuManager() {
        return MainMenuManager.getInstance();
    }
}
