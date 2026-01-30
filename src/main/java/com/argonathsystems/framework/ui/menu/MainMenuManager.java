package com.argonathsystems.framework.ui.menu;

import com.argonathsystems.framework.accessorapi.UIAccessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the main menu system including tab navigation and keybind handling.
 * This is the central entry point for all menu-related operations.
 * 
 * <p>Usage:
 * <pre>
 * // Initialize with accessor
 * MainMenuManager.getInstance().init(uiAccessor);
 * 
 * // Register a tab handler (from a mod)
 * MainMenuManager.getInstance().registerTabHandler(MenuTab.GUILD, guildTabHandler);
 * 
 * // Handle key press (called from adapter key event)
 * MainMenuManager.getInstance().handleKeyPress(playerId, "G");
 * </pre>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 */
public class MainMenuManager {
    
    private static final MainMenuManager INSTANCE = new MainMenuManager();
    
    /** The main menu hotkey (opens/closes the full menu) */
    public static final String MENU_HOTKEY = "TAB";
    
    /** ESC key for menu toggle */
    public static final String ESC_KEY = "ESCAPE";
    
    /** Registered tab handlers by tab */
    private final Map<MenuTab, MenuTabHandler> tabHandlers = new ConcurrentHashMap<>();
    
    /** Registered tab listeners */
    private final List<MenuTabListener> tabListeners = new ArrayList<>();
    
    /** Current open tab per player */
    private final Map<UUID, MenuTab> playerCurrentTab = new ConcurrentHashMap<>();
    
    /** Whether menu is open per player */
    private final Map<UUID, Boolean> menuOpenState = new ConcurrentHashMap<>();
    
    /** UI Accessor for rendering */
    private UIAccessor accessor;
    
    /** Enabled tabs (can be configured per-server) */
    private final Set<MenuTab> enabledTabs = EnumSet.allOf(MenuTab.class);
    
    private MainMenuManager() {}
    
    public static MainMenuManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize the manager with a UI accessor.
     */
    public void init(UIAccessor accessor) {
        this.accessor = accessor;
    }
    
    /**
     * Register a handler for a specific tab.
     * The handler is responsible for generating the tab's content.
     * 
     * @param tab The tab to handle
     * @param handler The handler implementation
     */
    public void registerTabHandler(MenuTab tab, MenuTabHandler handler) {
        tabHandlers.put(tab, handler);
    }
    
    /**
     * Unregister a tab handler.
     */
    public void unregisterTabHandler(MenuTab tab) {
        tabHandlers.remove(tab);
    }
    
    /**
     * Register a listener for tab events.
     */
    public void addTabListener(MenuTabListener listener) {
        tabListeners.add(listener);
    }
    
    /**
     * Remove a tab listener.
     */
    public void removeTabListener(MenuTabListener listener) {
        tabListeners.remove(listener);
    }
    
    /**
     * Enable or disable a tab.
     */
    public void setTabEnabled(MenuTab tab, boolean enabled) {
        if (enabled) {
            enabledTabs.add(tab);
        } else {
            enabledTabs.remove(tab);
        }
    }
    
    /**
     * Check if a tab is enabled.
     */
    public boolean isTabEnabled(MenuTab tab) {
        return enabledTabs.contains(tab);
    }
    
    /**
     * Handle a key press event.
     * Routes the key to the appropriate menu action.
     * 
     * @param playerId The player who pressed the key
     * @param key The key pressed (e.g., "G", "M", "TAB", "ESCAPE")
     * @return true if the key was handled, false otherwise
     */
    public boolean handleKeyPress(UUID playerId, String key) {
        if (key == null) return false;
        
        String upperKey = key.toUpperCase();
        
        // Check for menu toggle (TAB or ESC)
        if (MENU_HOTKEY.equals(upperKey) || ESC_KEY.equals(upperKey)) {
            if (isMenuOpen(playerId)) {
                closeMenu(playerId);
            } else if (MENU_HOTKEY.equals(upperKey)) {
                // ESC only closes, TAB toggles
                openMenu(playerId, null);
            }
            return true;
        }
        
        // Check for direct tab hotkey
        var tabOpt = MenuTab.fromHotkey(upperKey);
        if (tabOpt.isPresent() && isTabEnabled(tabOpt.get())) {
            MenuTab tab = tabOpt.get();
            if (isMenuOpen(playerId) && playerCurrentTab.get(playerId) == tab) {
                // Same tab pressed again - close menu
                closeMenu(playerId);
            } else {
                // Open menu to this tab
                openMenu(playerId, tab);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Open the main menu, optionally to a specific tab.
     * 
     * @param playerId The player to show the menu to
     * @param tab The tab to open to, or null for default (CHARACTER)
     */
    public void openMenu(UUID playerId, MenuTab tab) {
        if (accessor == null) {
            throw new IllegalStateException("MainMenuManager not initialized with accessor");
        }
        
        MenuTab targetTab = tab != null ? tab : MenuTab.CHARACTER;
        
        // Store state
        menuOpenState.put(playerId, true);
        playerCurrentTab.put(playerId, targetTab);
        
        // Generate menu UI
        String menuHtml = generateMenuHtml(playerId, targetTab);
        accessor.openUI(playerId, "main_menu", new MenuUIContext(menuHtml));
        
        // Notify listeners
        notifyTabOpened(playerId, targetTab);
    }
    
    /**
     * Close the main menu for a player.
     */
    public void closeMenu(UUID playerId) {
        if (accessor == null) return;
        
        MenuTab previousTab = playerCurrentTab.get(playerId);
        
        menuOpenState.put(playerId, false);
        playerCurrentTab.remove(playerId);
        
        accessor.closeUI(playerId);
        
        // Notify listeners
        if (previousTab != null) {
            notifyTabClosed(playerId, previousTab);
        }
    }
    
    /**
     * Switch to a different tab within the open menu.
     */
    public void switchTab(UUID playerId, MenuTab newTab) {
        if (!isMenuOpen(playerId)) return;
        
        MenuTab previousTab = playerCurrentTab.get(playerId);
        if (previousTab == newTab) return;
        
        // Notify close of old tab
        if (previousTab != null) {
            notifyTabClosed(playerId, previousTab);
        }
        
        // Update state
        playerCurrentTab.put(playerId, newTab);
        
        // Refresh UI
        String menuHtml = generateMenuHtml(playerId, newTab);
        accessor.openUI(playerId, "main_menu", new MenuUIContext(menuHtml));
        
        // Notify open of new tab
        notifyTabOpened(playerId, newTab);
    }
    
    /**
     * Check if menu is open for a player.
     */
    public boolean isMenuOpen(UUID playerId) {
        return Boolean.TRUE.equals(menuOpenState.get(playerId));
    }
    
    /**
     * Get the currently open tab for a player.
     */
    public MenuTab getCurrentTab(UUID playerId) {
        return playerCurrentTab.get(playerId);
    }
    
    /**
     * Get all enabled tabs sorted by display order.
     */
    public List<MenuTab> getEnabledTabs() {
        return enabledTabs.stream()
            .sorted(Comparator.comparingInt(MenuTab::sortOrder))
            .toList();
    }
    
    /**
     * Clean up player state on disconnect.
     */
    public void onPlayerQuit(UUID playerId) {
        menuOpenState.remove(playerId);
        playerCurrentTab.remove(playerId);
    }
    
    // ========================================================================
    // Private Methods
    // ========================================================================
    
    private void notifyTabOpened(UUID playerId, MenuTab tab) {
        for (MenuTabListener listener : tabListeners) {
            for (MenuTab handled : listener.getHandledTabs()) {
                if (handled == tab) {
                    listener.onTabOpened(playerId, tab);
                    break;
                }
            }
        }
    }
    
    private void notifyTabClosed(UUID playerId, MenuTab tab) {
        for (MenuTabListener listener : tabListeners) {
            for (MenuTab handled : listener.getHandledTabs()) {
                if (handled == tab) {
                    listener.onTabClosed(playerId, tab);
                    break;
                }
            }
        }
    }
    
    /**
     * Generate the full menu HTML/HYUIML.
     */
    private String generateMenuHtml(UUID playerId, MenuTab activeTab) {
        StringBuilder html = new StringBuilder();
        
        // Styles
        html.append("<style>");
        html.append(getMenuStyles());
        html.append("</style>");
        
        // Main container (fullscreen overlay)
        html.append("<div class=\"menu-overlay\">");
        
        // Backdrop
        html.append("<div class=\"menu-backdrop\"></div>");
        
        // Menu panel
        html.append("<div class=\"menu-panel\">");
        
        // Header
        html.append(generateHeader());
        
        // Body: Sidebar + Content
        html.append("<div class=\"menu-body\">");
        
        // Left sidebar with tabs
        html.append(generateSidebar(activeTab));
        
        // Content area
        html.append(generateContent(playerId, activeTab));
        
        html.append("</div>"); // menu-body
        
        // Footer
        html.append(generateFooter());
        
        html.append("</div>"); // menu-panel
        html.append("</div>"); // menu-overlay
        
        return html.toString();
    }
    
    private String getMenuStyles() {
        return """
            .menu-overlay {
                layout-mode: Overlay;
                anchor-left: 0; anchor-right: 0;
                anchor-top: 0; anchor-bottom: 0;
            }
            .menu-backdrop {
                layout-mode: Overlay;
                anchor-left: 0; anchor-right: 0;
                anchor-top: 0; anchor-bottom: 0;
                background-color: rgba(0, 0, 0, 0.7);
            }
            .menu-panel {
                layout-mode: Top;
                anchor-left: 50%; anchor-top: 50%;
                margin-left: -400; margin-top: -300;
                anchor-width: 800; anchor-height: 600;
                background-color: #1a1a1a;
                border: 2px solid #8d6e63;
                border-radius: 8;
            }
            .menu-header {
                layout-mode: Left;
                anchor-height: 48;
                background-color: #2a2a2a;
                border-bottom: 2px solid #8d6e63;
                padding: 12;
            }
            .menu-title {
                color: #ffd700;
                font-size: 18;
                font-weight: bold;
            }
            .menu-close {
                anchor-width: 32; anchor-height: 32;
                background-color: transparent;
                border: 1px solid #666;
                border-radius: 4;
                color: #fff;
                font-size: 18;
            }
            .menu-body {
                layout-mode: Left;
                flex-grow: 1;
            }
            .menu-sidebar {
                layout-mode: Top;
                anchor-width: 180;
                background-color: #1e1e1e;
                border-right: 1px solid #444;
                padding-top: 8;
            }
            .menu-tab-btn {
                layout-mode: Left;
                anchor-height: 36;
                padding-left: 12;
                padding-right: 12;
                align-items: center;
                background-color: transparent;
                border: none;
                border-bottom: 1px solid #333;
            }
            .menu-tab-btn:hover {
                background-color: rgba(255, 255, 255, 0.05);
            }
            .menu-tab-btn.active {
                background-color: rgba(255, 215, 0, 0.1);
                border-left: 3px solid #ffd700;
            }
            .menu-tab-icon {
                anchor-width: 20; anchor-height: 20;
                margin-right: 8;
            }
            .menu-tab-label {
                color: #ccc;
                font-size: 13;
            }
            .menu-tab-btn.active .menu-tab-label {
                color: #ffd700;
                font-weight: bold;
            }
            .menu-tab-hotkey {
                color: #666;
                font-size: 11;
                margin-left: auto;
            }
            .menu-content {
                layout-mode: Top;
                flex-grow: 1;
                padding: 16;
            }
            .menu-footer {
                layout-mode: Left;
                anchor-height: 48;
                background-color: #2a2a2a;
                border-top: 1px solid #444;
                padding: 8 16;
                justify-content: space-between;
                align-items: center;
            }
            .menu-footer-btn {
                anchor-height: 32;
                padding-left: 16; padding-right: 16;
                background-color: #3a3a3a;
                border: 1px solid #555;
                border-radius: 4;
                color: #fff;
                font-size: 12;
            }
            .menu-footer-btn:hover {
                background-color: #4a4a4a;
            }
            .menu-separator {
                anchor-height: 1;
                background-color: #444;
                margin: 8 12;
            }
            """;
    }
    
    private String generateHeader() {
        return """
            <div class="menu-header">
                <p class="menu-title">ARGONATH SYSTEMS</p>
                <div style="flex-grow: 1;"></div>
                <button id="menu-close" class="menu-close" onclick="closeMenu">×</button>
            </div>
            """;
    }
    
    private String generateSidebar(MenuTab activeTab) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"menu-sidebar\">");
        
        int lastGroup = -1;
        for (MenuTab tab : getEnabledTabs()) {
            int group = tab.sortOrder() / 10;
            
            // Add separator between groups
            if (lastGroup >= 0 && group != lastGroup) {
                sb.append("<div class=\"menu-separator\"></div>");
            }
            lastGroup = group;
            
            String activeClass = (tab == activeTab) ? " active" : "";
            String hotkey = tab.hotkey() != null ? tab.hotkey() : "";
            
            sb.append(String.format("""
                <button id="tab-%s" class="menu-tab-btn%s" data-tab="%s" onclick="switchTab">
                    <img src="%s" class="menu-tab-icon" />
                    <p class="menu-tab-label">%s</p>
                    <p class="menu-tab-hotkey">%s</p>
                </button>
                """, 
                tab.id(), activeClass, tab.id(), 
                tab.iconPath(), tab.displayName(), hotkey));
        }
        
        sb.append("</div>");
        return sb.toString();
    }
    
    private String generateContent(UUID playerId, MenuTab tab) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"menu-content\">");
        
        // Check for registered handler
        MenuTabHandler handler = tabHandlers.get(tab);
        if (handler != null) {
            sb.append(handler.generateContent(playerId, tab));
        } else {
            // Default placeholder content
            sb.append(generateDefaultContent(tab));
        }
        
        sb.append("</div>");
        return sb.toString();
    }
    
    private String generateDefaultContent(MenuTab tab) {
        return String.format("""
            <div style="layout-mode: Center; flex-grow: 1;">
                <p style="color: #888; font-size: 16;">%s</p>
                <p style="color: #666; font-size: 12; margin-top: 8;">Coming Soon</p>
            </div>
            """, tab.displayName());
    }
    
    private String generateFooter() {
        return """
            <div class="menu-footer">
                <div style="layout-mode: Left; gap: 8;">
                    <button id="btn-resume" class="menu-footer-btn" onclick="closeMenu">Resume Game</button>
                </div>
                <div style="layout-mode: Left; gap: 8;">
                    <button id="btn-logout" class="menu-footer-btn" onclick="logout">Logout</button>
                    <button id="btn-exit" class="menu-footer-btn" onclick="exitGame">Exit Game</button>
                </div>
            </div>
            """;
    }
}
