package com.argonathsystems.framework.ui;

import com.argonathsystems.framework.ui.layout.HudLayoutManager;
import com.argonathsystems.framework.ui.menu.MainMenuManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.logging.Logger;

/**
 * Hytale plugin entry point for the UI Framework.
 * 
 * <p>This class is responsible for initializing the platform-agnostic 
 * UI systems when loaded by the Hytale server.</p>
 */
public class UiFrameworkPlugin extends JavaPlugin {
    private static final Logger LOGGER = Logger.getLogger(UiFrameworkPlugin.class.getName());
    private static UiFrameworkPlugin instance;
    
    public UiFrameworkPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }
    
    public static UiFrameworkPlugin getInstance() {
        return instance;
    }
    
    public void setup() {
        LOGGER.info("Initializing UI Framework...");
        
        // HudLayoutManager and MainMenuManager are singletons
        // They are accessed via their getInstance() methods
        LOGGER.info("UI Framework - HudLayoutManager available.");
        LOGGER.info("UI Framework - MainMenuManager available.");
        
        LOGGER.info("UI Framework initialized successfully.");
    }
    
    public void shutdown() {
        LOGGER.info("Shutting down UI Framework...");
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
