package com.argonathsystems.framework.ui.menu;

import com.argonathsystems.framework.accessorapi.data.DataValue;
import java.util.UUID;

/**
 * Handler interface for generating menu tab content.
 * Each mod can register a handler for its tabs.
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.0.0
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
     * <p>Event data is provided as type-safe {@link DataValue} per accessor v2.0.0.
     * Use {@code data.asString()}, {@code data.asInt()}, etc. to extract values.
     * 
     * @param playerId The player who triggered the event
     * @param tab The current tab
     * @param eventId The event/button ID that was triggered
     * @param data Optional event data (may be null)
     */
    default void handleEvent(UUID playerId, MenuTab tab, String eventId, DataValue data) {
        // Default no-op
    }
}
