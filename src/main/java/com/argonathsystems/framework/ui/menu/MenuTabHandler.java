package com.argonathsystems.framework.ui.menu;

import java.util.UUID;

/**
 * Handler interface for generating menu tab content.
 * Each mod can register a handler for its tabs.
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 */
public interface MenuTabHandler {
    
    /**
     * Generate the HTML/HYUIML content for this tab.
     * 
     * @param playerId The player viewing the tab
     * @param tab The tab being rendered
     * @return HTML/HYUIML string for the content area
     */
    String generateContent(UUID playerId, MenuTab tab);
    
    /**
     * Handle a UI event from within this tab's content.
     * 
     * @param playerId The player who triggered the event
     * @param tab The current tab
     * @param eventId The event/button ID that was triggered
     * @param data Optional event data
     */
    default void handleEvent(UUID playerId, MenuTab tab, String eventId, Object data) {
        // Default no-op
    }
}
