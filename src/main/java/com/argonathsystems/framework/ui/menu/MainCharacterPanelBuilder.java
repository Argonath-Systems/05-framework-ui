package com.argonathsystems.framework.ui.menu;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;

/**
 * Builder for generating the Main Character Panel HYUIML content.
 * 
 * <p>This builder generates the full panel structure including:
 * <ul>
 *   <li>Header with title and close button</li>
 *   <li>Tab bar with 8 tabs (icons + labels + dropdown arrows)</li>
 *   <li>Sub-navigation bar for the active tab</li>
 *   <li>Content area with dynamic sub-page content</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 2.0.0
 */
public class MainCharacterPanelBuilder {
    
    private MainPanelTab activeTab = MainPanelTab.INVENTORY;
    private SubPage activeSubPage = SubPage.EQUIPMENT;
    private String subPageContent = "";
    private BiFunction<UUID, SubPage, Boolean> visibilityChecker;
    private ToIntBiFunction<UUID, SubPage> badgeCountProvider;
    private UUID playerId;
    
    /**
     * Sets the active tab.
     * @param tab the active tab
     * @return this builder for chaining
     */
    public MainCharacterPanelBuilder withActiveTab(MainPanelTab tab) {
        this.activeTab = tab;
        return this;
    }
    
    /**
     * Sets the active sub-page.
     * @param subPage the active sub-page
     * @return this builder for chaining
     */
    public MainCharacterPanelBuilder withActiveSubPage(SubPage subPage) {
        this.activeSubPage = subPage;
        return this;
    }
    
    /**
     * Sets the sub-page content HTML.
     * @param content the content HTML
     * @return this builder for chaining
     */
    public MainCharacterPanelBuilder withSubPageContent(String content) {
        this.subPageContent = content != null ? content : "";
        return this;
    }
    
    /**
     * Sets the visibility checker for sub-pages.
     * @param playerId the player to check visibility for
     * @param checker function that returns true if sub-page is visible
     * @return this builder for chaining
     */
    public MainCharacterPanelBuilder withVisibleSubPages(UUID playerId, 
            BiFunction<UUID, SubPage, Boolean> checker) {
        this.playerId = playerId;
        this.visibilityChecker = checker;
        return this;
    }
    
    /**
     * Sets the badge count provider for sub-pages.
     * @param playerId the player to get badge counts for
     * @param provider function that returns badge count for a sub-page
     * @return this builder for chaining
     */
    public MainCharacterPanelBuilder withBadgeCounts(UUID playerId,
            ToIntBiFunction<UUID, SubPage> provider) {
        this.playerId = playerId;
        this.badgeCountProvider = provider;
        return this;
    }
    
    /**
     * Builds the panel HTML.
     * @return the complete panel HTML string
     */
    public String build() {
        StringBuilder html = new StringBuilder();
        
        // Styles
        html.append("<style>");
        html.append(getPanelStyles());
        html.append("</style>");
        
        // Page overlay
        html.append("<div class=\"page-overlay\">");
        
        // Main panel container
        html.append("<div class=\"main-character-panel\">");
        
        // Header
        html.append(buildHeader());
        
        // Tab bar
        html.append(buildTabBar());
        
        // Sub-navigation bar (dropdown items for active tab)
        html.append(buildSubNavigation());
        
        // Content area
        html.append(buildContentArea());
        
        html.append("</div>"); // main-character-panel
        html.append("</div>"); // page-overlay
        
        return html.toString();
    }
    
    private String buildHeader() {
        return """
            <div class="panel-header">
                <p class="panel-title">CHARACTER PANEL</p>
                <div style="flex-weight: 1;"></div>
                <button id="btn-close" class="panel-close-btn">×</button>
            </div>
            """;
    }
    
    private String buildTabBar() {
        StringBuilder sb = new StringBuilder();
        sb.append("<nav class=\"panel-tabs\">");
        
        for (MainPanelTab tab : MainPanelTab.getSortedTabs()) {
            String activeClass = (tab == activeTab) ? " active" : "";
            String tabId = "tab-" + tab.id();
            
            sb.append(String.format("""
                <button id="%s" class="tab-btn%s" data-tab="%s">
                    <p class="tab-icon">%s</p>
                    <p class="tab-label">%s</p>
                    <p class="tab-dropdown-arrow">▼</p>
                </button>
                """,
                tabId, activeClass, tab.id(),
                tab.icon(), tab.displayName()));
        }
        
        sb.append("</nav>");
        return sb.toString();
    }
    
    private String buildSubNavigation() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"sub-navigation\">");
        
        List<SubPage> subPages = activeTab.getSubPages();
        for (SubPage subPage : subPages) {
            // Check visibility
            if (visibilityChecker != null && playerId != null) {
                if (!visibilityChecker.apply(playerId, subPage)) {
                    continue;
                }
            }
            
            String activeClass = (subPage == activeSubPage) ? " active" : "";
            String subPageId = "subpage-" + subPage.id();
            
            // Get badge count
            int badgeCount = 0;
            if (badgeCountProvider != null && playerId != null) {
                badgeCount = badgeCountProvider.applyAsInt(playerId, subPage);
            }
            
            String badgeHtml = badgeCount > 0 
                ? String.format("<span class=\"badge\">%d</span>", badgeCount)
                : "";
            
            sb.append(String.format("""
                <button id="%s" class="subpage-btn%s" data-subpage="%s">
                    <p class="subpage-icon">%s</p>
                    <p class="subpage-label">%s</p>
                    %s
                </button>
                """,
                subPageId, activeClass, subPage.id(),
                subPage.icon(), subPage.displayName(), badgeHtml));
        }
        
        sb.append("</div>");
        return sb.toString();
    }
    
    private String buildContentArea() {
        return String.format("""
            <div class="panel-content">
                %s
            </div>
            """, subPageContent);
    }
    
    private String getPanelStyles() {
        return """
            .page-overlay {
                layout-mode: MiddleCenter;
                anchor-left: 0; anchor-right: 0;
                anchor-top: 0; anchor-bottom: 0;
                background-color: rgba(0, 0, 0, 0.6);
            }
            .main-character-panel {
                layout-mode: Top;
                anchor-width: 900; anchor-height: 700;
                background-color: #1a1510;
                background-image: url(textures/ui/panel-bg-dark.png);
            }
            .panel-header {
                layout-mode: Left;
                anchor-height: 48;
                background-color: #2a2015;
                vertical-align: center;
            }
            .panel-title {
                color: #d4af37;
                font-size: 18;
                font-weight: bold;
                horizontal-align: left;
            }
            .panel-close-btn {
                anchor-width: 36; anchor-height: 36;
                background-color: transparent;
                color: #ccc;
                font-size: 24;
                horizontal-align: center;
                vertical-align: center;
            }
            .panel-tabs {
                layout-mode: Left;
                anchor-height: 44;
                background-color: #252015;
            }
            .tab-btn {
                layout-mode: Left;
                flex-weight: 1;
                anchor-height: 44;
                background-color: transparent;
                vertical-align: center;
                horizontal-align: center;
            }
            .tab-btn.active {
                background-color: rgba(212, 175, 55, 0.15);
            }
            .tab-icon {
                font-size: 16;
                color: #aaa;
            }
            .tab-btn.active .tab-icon {
                color: #d4af37;
            }
            .tab-label {
                font-size: 11;
                color: #888;
            }
            .tab-btn.active .tab-label {
                color: #d4af37;
                font-weight: bold;
            }
            .tab-dropdown-arrow {
                font-size: 8;
                color: #666;
            }
            .sub-navigation {
                layout-mode: Left;
                anchor-height: 36;
                background-color: #1e1812;
            }
            .subpage-btn {
                layout-mode: Left;
                anchor-height: 36;
                background-color: transparent;
                vertical-align: center;
            }
            .subpage-btn.active {
                background-color: rgba(212, 175, 55, 0.2);
            }
            .subpage-icon {
                font-size: 14;
                color: #999;
            }
            .subpage-btn.active .subpage-icon {
                color: #d4af37;
            }
            .subpage-label {
                font-size: 12;
                color: #aaa;
            }
            .subpage-btn.active .subpage-label {
                color: #d4af37;
            }
            .badge {
                font-size: 10;
                color: #fff;
                background-color: #c44;
            }
            .panel-content {
                layout-mode: Top;
                flex-weight: 1;
                background-color: #1a1510;
            }
            .subpage-placeholder {
                layout-mode: Top;
                flex-weight: 1;
                horizontal-align: center;
                vertical-align: center;
            }
            .placeholder-icon {
                font-size: 48;
                color: #444;
            }
            .placeholder-title {
                font-size: 20;
                color: #666;
            }
            .placeholder-message {
                font-size: 14;
                color: #555;
            }
            """;
    }
    
    /**
     * Builds template variables for use with HyUIML templates.
     * 
     * @return map of variable names to values
     */
    public java.util.Map<String, Object> buildTemplateVariables() {
        java.util.Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("activeTab", activeTab != null ? activeTab.id() : "inventory");
        vars.put("activeSubPage", activeSubPage != null ? activeSubPage.id() : "equipment");
        vars.put("subPageContent", subPageContent);
        vars.put("playerId", playerId != null ? playerId.toString() : "");
        return vars;
    }
}
