package com.argonathsystems.framework.ui.menu;

import java.util.UUID;

/**
 * Listener interface for menu events.
 * Mods can register listeners to be notified when their tab is opened.
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 */
public interface MenuTabListener {
    
    /**
     * Called when a menu tab is opened.
     * 
     * @param playerId The player opening the tab
     * @param tab The tab being opened
     */
    void onTabOpened(UUID playerId, MenuTab tab);
    
    /**
     * Called when a menu tab is closed.
     * 
     * @param playerId The player closing the tab
     * @param tab The tab being closed
     */
    default void onTabClosed(UUID playerId, MenuTab tab) {
        // Default no-op
    }
    
    /**
     * Get the tab(s) this listener handles.
     * 
     * @return Array of tabs this listener is interested in
     */
    MenuTab[] getHandledTabs();
}
