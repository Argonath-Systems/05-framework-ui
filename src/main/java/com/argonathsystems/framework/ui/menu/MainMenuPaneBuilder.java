package com.argonathsystems.framework.ui.menu;

/**
 * Builder for generating the Main Menu Pane (ESC menu) HYUIML content.
 * 
 * <p>This is the simple ESC menu overlay per VDD-MISC-021 that provides:
 * <ul>
 *   <li>Resume Game - closes menu</li>
 *   <li>Character - opens Character Panel</li>
 *   <li>Social - opens Character Panel to Social tab</li>
 *   <li>Settings - opens settings page</li>
 *   <li>Quit to Menu - disconnects from server</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 2.0.0
 */
public class MainMenuPaneBuilder {
    
    private String version = "1.0.0";
    private String serverName = "Argonath";
    private boolean showQuit = true;
    private boolean showLogout = true;
    
    /**
     * Sets the version string displayed in footer.
     * @param version the version string
     * @return this builder for chaining
     */
    public MainMenuPaneBuilder withVersion(String version) {
        this.version = version != null ? version : "1.0.0";
        return this;
    }
    
    /**
     * Sets the server name displayed in footer.
     * @param serverName the server name
     * @return this builder for chaining
     */
    public MainMenuPaneBuilder withServerName(String serverName) {
        this.serverName = serverName != null ? serverName : "Argonath";
        return this;
    }
    
    /**
     * Sets whether to show the Quit button.
     * @param show true to show
     * @return this builder for chaining
     */
    public MainMenuPaneBuilder withQuitButton(boolean show) {
        this.showQuit = show;
        return this;
    }
    
    /**
     * Sets whether to show the Logout button.
     * @param show true to show
     * @return this builder for chaining
     */
    public MainMenuPaneBuilder withLogoutButton(boolean show) {
        this.showLogout = show;
        return this;
    }
    
    /**
     * Builds the menu pane HTML.
     * @return the complete menu pane HTML string
     */
    public String build() {
        StringBuilder html = new StringBuilder();
        
        // Styles
        html.append("<style>");
        html.append(getMenuStyles());
        html.append("</style>");
        
        // Page overlay
        html.append("<div class=\"page-overlay\">");
        
        // Menu container (centered, 400x500)
        html.append("<div class=\"main-menu-pane\">");
        
        // Header
        html.append(buildHeader());
        
        // Menu buttons
        html.append(buildButtons());
        
        // Footer
        html.append(buildFooter());
        
        html.append("</div>"); // main-menu-pane
        html.append("</div>"); // page-overlay
        
        return html.toString();
    }
    
    private String buildHeader() {
        return """
            <div class="menu-header">
                <p class="menu-title">ARGONATH SYSTEMS</p>
                <div class="menu-divider"></div>
            </div>
            """;
    }
    
    private String buildButtons() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"menu-buttons\">");
        
        // Resume Game (primary action)
        sb.append("""
            <button id="btn-resume" class="menu-btn primary">
                <p class="btn-label">Resume Game</p>
            </button>
            """);
        
        // Character
        sb.append("""
            <button id="btn-character" class="menu-btn">
                <p class="btn-label">Character</p>
            </button>
            """);
        
        // Social
        sb.append("""
            <button id="btn-social" class="menu-btn">
                <p class="btn-label">Social</p>
            </button>
            """);
        
        // Settings
        sb.append("""
            <button id="btn-settings" class="menu-btn">
                <p class="btn-label">Settings</p>
            </button>
            """);
        
        // Logout (if shown)
        if (showLogout) {
            sb.append("""
                <button id="btn-logout" class="menu-btn warning">
                    <p class="btn-label">Logout</p>
                </button>
                """);
        }
        
        // Quit to Menu (if shown)
        if (showQuit) {
            sb.append("""
                <button id="btn-quit" class="menu-btn danger">
                    <p class="btn-label">Quit to Menu</p>
                </button>
                """);
        }
        
        sb.append("</div>");
        return sb.toString();
    }
    
    private String buildFooter() {
        return String.format("""
            <div class="menu-footer">
                <p class="version-info">v%s | Server: %s</p>
            </div>
            """, version, serverName);
    }
    
    private String getMenuStyles() {
        return """
            .page-overlay {
                layout-mode: MiddleCenter;
                anchor-left: 0; anchor-right: 0;
                anchor-top: 0; anchor-bottom: 0;
                background-color: rgba(0, 0, 0, 0.7);
            }
            .main-menu-pane {
                layout-mode: Top;
                anchor-width: 400; anchor-height: 500;
                background-color: #1a1510;
                background-image: url(textures/ui/panel-bg-ornate.png);
                horizontal-align: center;
            }
            .menu-header {
                layout-mode: Top;
                anchor-height: 80;
                horizontal-align: center;
                vertical-align: center;
            }
            .menu-title {
                color: #d4af37;
                font-size: 24;
                font-weight: bold;
                text-transform: uppercase;
                horizontal-align: center;
            }
            .menu-divider {
                anchor-width: 200; anchor-height: 2;
                background-color: #8d6e63;
                horizontal-align: center;
            }
            .menu-buttons {
                layout-mode: Top;
                flex-weight: 1;
                horizontal-align: center;
            }
            .menu-btn {
                layout-mode: Left;
                anchor-width: 280; anchor-height: 44;
                background-color: #2a2015;
                horizontal-align: center;
                vertical-align: center;
            }
            .menu-btn.primary {
                background-color: #3a5a40;
            }
            .menu-btn.warning {
                background-color: #5a4a30;
            }
            .menu-btn.danger {
                background-color: #5a3030;
            }
            .btn-label {
                color: #ccc;
                font-size: 14;
                horizontal-align: center;
            }
            .menu-btn.primary .btn-label {
                color: #9ddb9d;
            }
            .menu-btn.danger .btn-label {
                color: #db9d9d;
            }
            .menu-footer {
                layout-mode: Top;
                anchor-height: 40;
                horizontal-align: center;
                vertical-align: center;
            }
            .version-info {
                color: #666;
                font-size: 11;
                horizontal-align: center;
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
        vars.put("version", version);
        vars.put("serverName", serverName);
        vars.put("showQuit", showQuit);
        vars.put("showLogout", showLogout);
        return vars;
    }
}
