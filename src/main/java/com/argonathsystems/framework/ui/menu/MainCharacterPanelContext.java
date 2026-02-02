package com.argonathsystems.framework.ui.menu;

import com.argonathsystems.framework.accessorapi.ui.UIContext;

/**
 * Context object for the Main Character Panel UI.
 * Contains the rendered HTML and current navigation state.
 * 
 * @param html the rendered panel HTML
 * @param activeTab the currently active tab
 * @param activeSubPage the currently active sub-page
 * 
 * @author Argonath Systems
 * @since 2.0.0
 */
public record MainCharacterPanelContext(
    String html,
    MainPanelTab activeTab,
    SubPage activeSubPage
) implements UIContext {
    
    /**
     * Gets the tab ID for event routing.
     * @return tab ID string
     */
    public String getTabId() {
        return activeTab != null ? activeTab.id() : "inventory";
    }
    
    /**
     * Gets the sub-page ID for event routing.
     * @return sub-page ID string
     */
    public String getSubPageId() {
        return activeSubPage != null ? activeSubPage.id() : "equipment";
    }
}
