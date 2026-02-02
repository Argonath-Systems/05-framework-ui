package com.argonathsystems.framework.ui.menu;

import com.argonathsystems.framework.accessorapi.UIAccessor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Central manager for the Main Character Panel - the unified 8-tab navigation system.
 * 
 * <p>This manager handles:
 * <ul>
 *   <li>Sub-page handler registration (mods register content providers)</li>
 *   <li>Panel open/close state per player</li>
 *   <li>Tab and sub-page navigation</li>
 *   <li>Keybind handling (I, J, O, U, M, K, Shift+I, G)</li>
 *   <li>Template rendering coordination</li>
 * </ul>
 * 
 * <p>This class implements the unified navigation per VDD-LAYOUT-003.
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Initialize (during server startup)
 * MainCharacterPanelManager.getInstance().init(uiAccessor);
 * 
 * // Register a sub-page handler (from a mod)
 * MainCharacterPanelManager.getInstance()
 *     .registerSubPageHandler(new GuildSubPageHandler(guildService));
 * 
 * // Open panel to specific sub-page
 * MainCharacterPanelManager.getInstance()
 *     .openToSubPage(playerId, SubPage.GUILD);
 * }</pre>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 2.0.0
 * @see MainPanelTab
 * @see SubPage
 * @see SubPageHandler
 */
public final class MainCharacterPanelManager {
    
    private static final Logger LOG = Logger.getLogger(MainCharacterPanelManager.class.getName());
    
    private static final MainCharacterPanelManager INSTANCE = new MainCharacterPanelManager();
    
    /** Registered sub-page handlers */
    private final Map<SubPage, SubPageHandler> subPageHandlers = new EnumMap<>(SubPage.class);
    
    /** Current open tab per player */
    private final Map<UUID, MainPanelTab> playerCurrentTab = new ConcurrentHashMap<>();
    
    /** Current open sub-page per player */
    private final Map<UUID, SubPage> playerCurrentSubPage = new ConcurrentHashMap<>();
    
    /** Whether panel is open per player */
    private final Map<UUID, Boolean> panelOpenState = new ConcurrentHashMap<>();
    
    /** Last visited sub-page per tab per player (for "remember last" behavior) */
    private final Map<UUID, Map<MainPanelTab, SubPage>> playerLastSubPage = new ConcurrentHashMap<>();
    
    /** UI Accessor for rendering */
    private UIAccessor accessor;
    
    /** Builder for generating panel HTML */
    private MainCharacterPanelBuilder panelBuilder;
    
    private MainCharacterPanelManager() {}
    
    /**
     * Gets the singleton instance.
     * @return the manager instance
     */
    public static MainCharacterPanelManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initializes the manager with a UI accessor.
     * Must be called during server startup before any other operations.
     * 
     * @param accessor the UI accessor for rendering
     */
    public void init(UIAccessor accessor) {
        this.accessor = accessor;
        this.panelBuilder = new MainCharacterPanelBuilder();
        LOG.info("MainCharacterPanelManager initialized");
    }
    
    // ========================================================================
    // Sub-Page Handler Registration
    // ========================================================================
    
    /**
     * Registers a handler for a sub-page.
     * 
     * @param handler the handler to register
     * @throws IllegalStateException if a handler is already registered for that sub-page
     */
    public void registerSubPageHandler(SubPageHandler handler) {
        SubPage subPage = handler.getHandledSubPage();
        if (subPageHandlers.containsKey(subPage)) {
            throw new IllegalStateException(
                "Handler already registered for sub-page: " + subPage.id());
        }
        subPageHandlers.put(subPage, handler);
        LOG.info("Registered sub-page handler for: " + subPage.id());
    }
    
    /**
     * Unregisters a handler for a sub-page.
     * 
     * @param subPage the sub-page to unregister
     */
    public void unregisterSubPageHandler(SubPage subPage) {
        subPageHandlers.remove(subPage);
    }
    
    /**
     * Gets the handler for a sub-page, if registered.
     * 
     * @param subPage the sub-page
     * @return Optional containing the handler, or empty if not registered
     */
    public Optional<SubPageHandler> getHandler(SubPage subPage) {
        return Optional.ofNullable(subPageHandlers.get(subPage));
    }
    
    /**
     * Checks if a handler is registered for a sub-page.
     * 
     * @param subPage the sub-page to check
     * @return true if a handler is registered
     */
    public boolean hasHandler(SubPage subPage) {
        return subPageHandlers.containsKey(subPage);
    }
    
    // ========================================================================
    // Keybind Handling
    // ========================================================================
    
    /**
     * Handles a key press event and routes to appropriate action.
     * 
     * @param playerId the player who pressed the key
     * @param key the key pressed (e.g., "I", "J", "SHIFT_I")
     * @return true if the key was handled, false otherwise
     */
    public boolean handleKeyPress(UUID playerId, String key) {
        if (key == null || accessor == null) {
            return false;
        }
        
        String normalizedKey = key.toUpperCase().replace("+", "_").replace(" ", "_");
        
        // Check for panel toggle (C key)
        if ("C".equals(normalizedKey)) {
            if (isPanelOpen(playerId)) {
                closePanel(playerId);
            } else {
                openPanel(playerId);
            }
            return true;
        }
        
        // Check for direct tab hotkeys
        Optional<MainPanelTab> tabOpt = MainPanelTab.fromHotkey(normalizedKey);
        if (tabOpt.isPresent()) {
            MainPanelTab tab = tabOpt.get();
            SubPage defaultSubPage = getLastOrDefaultSubPage(playerId, tab);
            
            if (isPanelOpen(playerId) && playerCurrentTab.get(playerId) == tab) {
                // Same tab pressed again - close panel
                closePanel(playerId);
            } else {
                // Open to this tab's sub-page
                openToSubPage(playerId, defaultSubPage);
            }
            return true;
        }
        
        // Check for G key (special case - goes to Social > Guild)
        if ("G".equals(normalizedKey)) {
            if (isPanelOpen(playerId) && playerCurrentSubPage.get(playerId) == SubPage.GUILD) {
                closePanel(playerId);
            } else {
                openToSubPage(playerId, SubPage.GUILD);
            }
            return true;
        }
        
        return false;
    }
    
    // ========================================================================
    // Panel Navigation
    // ========================================================================
    
    /**
     * Opens the panel for a player to the last-visited or default location.
     * 
     * @param playerId the player to show the panel to
     */
    public void openPanel(UUID playerId) {
        MainPanelTab lastTab = playerCurrentTab.getOrDefault(playerId, MainPanelTab.INVENTORY);
        SubPage lastSubPage = getLastOrDefaultSubPage(playerId, lastTab);
        openToSubPage(playerId, lastSubPage);
    }
    
    /**
     * Opens the panel directly to a specific tab's default sub-page.
     * 
     * @param playerId the player to show the panel to
     * @param tab the tab to open to
     */
    public void openToTab(UUID playerId, MainPanelTab tab) {
        SubPage subPage = getLastOrDefaultSubPage(playerId, tab);
        openToSubPage(playerId, subPage);
    }
    
    /**
     * Opens the panel directly to a specific sub-page.
     * 
     * @param playerId the player to show the panel to
     * @param subPage the sub-page to open to
     */
    public void openToSubPage(UUID playerId, SubPage subPage) {
        if (accessor == null) {
            throw new IllegalStateException("MainCharacterPanelManager not initialized");
        }
        
        MainPanelTab tab = subPage.parentTab();
        SubPage previousSubPage = playerCurrentSubPage.get(playerId);
        
        // Notify previous handler of close
        if (previousSubPage != null && previousSubPage != subPage) {
            getHandler(previousSubPage).ifPresent(h -> h.onClose(playerId));
        }
        
        // Update state
        panelOpenState.put(playerId, true);
        playerCurrentTab.put(playerId, tab);
        playerCurrentSubPage.put(playerId, subPage);
        
        // Remember last sub-page for this tab
        playerLastSubPage
            .computeIfAbsent(playerId, k -> new EnumMap<>(MainPanelTab.class))
            .put(tab, subPage);
        
        // Notify new handler of open
        getHandler(subPage).ifPresent(h -> h.onOpen(playerId));
        
        // Generate and render panel
        String panelHtml = generatePanelHtml(playerId, tab, subPage);
        accessor.openUI(playerId, "main_character_panel", 
            new MainCharacterPanelContext(panelHtml, tab, subPage));
        
        LOG.fine("Opened panel for " + playerId + " to " + subPage.id());
    }
    
    /**
     * Switches to a different sub-page within the open panel.
     * 
     * @param playerId the player
     * @param newSubPage the new sub-page to show
     */
    public void switchSubPage(UUID playerId, SubPage newSubPage) {
        if (!isPanelOpen(playerId)) {
            return;
        }
        
        SubPage previousSubPage = playerCurrentSubPage.get(playerId);
        if (previousSubPage == newSubPage) {
            return;
        }
        
        openToSubPage(playerId, newSubPage);
    }
    
    /**
     * Closes the panel for a player.
     * 
     * @param playerId the player
     */
    public void closePanel(UUID playerId) {
        if (accessor == null) {
            return;
        }
        
        SubPage currentSubPage = playerCurrentSubPage.get(playerId);
        
        // Notify handler of close
        if (currentSubPage != null) {
            getHandler(currentSubPage).ifPresent(h -> h.onClose(playerId));
        }
        
        panelOpenState.put(playerId, false);
        accessor.closeUI(playerId);
        
        LOG.fine("Closed panel for " + playerId);
    }
    
    /**
     * Checks if the panel is open for a player.
     * 
     * @param playerId the player to check
     * @return true if the panel is open
     */
    public boolean isPanelOpen(UUID playerId) {
        return Boolean.TRUE.equals(panelOpenState.get(playerId));
    }
    
    /**
     * Gets the current tab for a player.
     * 
     * @param playerId the player
     * @return the current tab, or null if panel not open
     */
    public MainPanelTab getCurrentTab(UUID playerId) {
        return playerCurrentTab.get(playerId);
    }
    
    /**
     * Gets the current sub-page for a player.
     * 
     * @param playerId the player
     * @return the current sub-page, or null if panel not open
     */
    public SubPage getCurrentSubPage(UUID playerId) {
        return playerCurrentSubPage.get(playerId);
    }
    
    /**
     * Cleans up player state on disconnect.
     * 
     * @param playerId the player who disconnected
     */
    public void onPlayerQuit(UUID playerId) {
        SubPage currentSubPage = playerCurrentSubPage.get(playerId);
        if (currentSubPage != null) {
            getHandler(currentSubPage).ifPresent(h -> h.onClose(playerId));
        }
        
        panelOpenState.remove(playerId);
        playerCurrentTab.remove(playerId);
        playerCurrentSubPage.remove(playerId);
        playerLastSubPage.remove(playerId);
    }
    
    // ========================================================================
    // Event Handling
    // ========================================================================
    
    /**
     * Handles an event/action from within the panel.
     * Routes to the appropriate sub-page handler.
     * 
     * @param playerId the player who triggered the event
     * @param eventId the element ID that was interacted with
     * @param eventData additional event data
     */
    public void handleEvent(UUID playerId, String eventId, String eventData) {
        // Check for tab switch events
        if (eventId != null && eventId.startsWith("tab-")) {
            String tabId = eventId.substring(4);
            MainPanelTab.fromId(tabId).ifPresent(tab -> openToTab(playerId, tab));
            return;
        }
        
        // Check for sub-page switch events
        if (eventId != null && eventId.startsWith("subpage-")) {
            String subPageId = eventId.substring(8);
            SubPage.fromId(subPageId).ifPresent(sp -> switchSubPage(playerId, sp));
            return;
        }
        
        // Route to current sub-page handler
        SubPage currentSubPage = playerCurrentSubPage.get(playerId);
        if (currentSubPage != null) {
            getHandler(currentSubPage).ifPresent(h -> h.handleEvent(playerId, eventId, eventData));
        }
    }
    
    // ========================================================================
    // Private Methods
    // ========================================================================
    
    private SubPage getLastOrDefaultSubPage(UUID playerId, MainPanelTab tab) {
        Map<MainPanelTab, SubPage> lastPages = playerLastSubPage.get(playerId);
        if (lastPages != null && lastPages.containsKey(tab)) {
            return lastPages.get(tab);
        }
        return tab.getDefaultSubPage();
    }
    
    private String generatePanelHtml(UUID playerId, MainPanelTab activeTab, SubPage activeSubPage) {
        // Generate sub-page content using handler
        String subPageContent = generateSubPageContent(playerId, activeSubPage);
        
        // Build full panel HTML
        return panelBuilder
            .withActiveTab(activeTab)
            .withActiveSubPage(activeSubPage)
            .withSubPageContent(subPageContent)
            .withVisibleSubPages(playerId, this::isSubPageVisible)
            .withBadgeCounts(playerId, this::getSubPageBadgeCount)
            .build();
    }
    
    private String generateSubPageContent(UUID playerId, SubPage subPage) {
        Optional<SubPageHandler> handlerOpt = getHandler(subPage);
        if (handlerOpt.isPresent()) {
            return handlerOpt.get().generateContent(playerId);
        }
        
        // Default placeholder for unregistered sub-pages
        return generateDefaultSubPageContent(subPage);
    }
    
    private String generateDefaultSubPageContent(SubPage subPage) {
        return String.format("""
            <div class="subpage-placeholder">
                <p class="placeholder-icon">%s</p>
                <p class="placeholder-title">%s</p>
                <p class="placeholder-message">Coming Soon</p>
            </div>
            """, subPage.icon(), subPage.displayName());
    }
    
    private boolean isSubPageVisible(UUID playerId, SubPage subPage) {
        // Check admin-only
        if (subPage.isAdminOnly()) {
            // TODO: Check player admin permission via accessor
            return false;
        }
        
        // Check handler visibility
        Optional<SubPageHandler> handlerOpt = getHandler(subPage);
        return handlerOpt.map(h -> h.isVisibleTo(playerId)).orElse(true);
    }
    
    private int getSubPageBadgeCount(UUID playerId, SubPage subPage) {
        return getHandler(subPage)
            .map(h -> h.getBadgeCount(playerId))
            .orElse(0);
    }
    
    /**
     * Gets the list of all sub-pages visible to a player for a tab.
     * 
     * @param playerId the player
     * @param tab the tab
     * @return list of visible sub-pages
     */
    public List<SubPage> getVisibleSubPages(UUID playerId, MainPanelTab tab) {
        return tab.getSubPages().stream()
            .filter(sp -> isSubPageVisible(playerId, sp))
            .toList();
    }
}
