package com.argonathsystems.framework.ui.menu;

import java.util.UUID;

/**
 * Interface for providing content for a {@link SubPage} within the Main Character Panel.
 * 
 * <p>Mods implement this interface to provide dynamic content for their sub-pages.
 * Handlers are registered with {@link MainCharacterPanelManager#registerSubPageHandler}.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class GuildSubPageHandler implements SubPageHandler {
 *     private final GuildService guildService;
 *     
 *     @Override
 *     public SubPage getHandledSubPage() {
 *         return SubPage.GUILD;
 *     }
 *     
 *     @Override
 *     public String generateContent(UUID playerId) {
 *         Guild guild = guildService.getPlayerGuild(playerId);
 *         return new GuildPanelBuilder()
 *             .withGuild(guild)
 *             .withRoster(guildService.getRoster(guild.id()))
 *             .build();
 *     }
 * }
 * }</pre>
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 2.0.0
 * @see SubPage
 * @see MainCharacterPanelManager
 */
public interface SubPageHandler {
    
    /**
     * Gets the sub-page that this handler provides content for.
     * 
     * @return the handled sub-page
     */
    SubPage getHandledSubPage();
    
    /**
     * Generates the HTML/HYUIML content for this sub-page.
     * 
     * <p>The returned content will be injected into the panel's content area.
     * It should NOT include page-overlay or container wrappers - only the
     * inner content for the tab area.
     * 
     * @param playerId the player viewing the sub-page
     * @return HTML/HYUIML content string
     */
    String generateContent(UUID playerId);
    
    /**
     * Handles an event/action triggered from within this sub-page.
     * 
     * <p>Called when the player interacts with an element in the sub-page
     * (e.g., clicking a button, selecting an item).
     * 
     * @param playerId the player who triggered the event
     * @param eventId the element ID that was interacted with
     * @param eventData additional event data (may be null)
     */
    default void handleEvent(UUID playerId, String eventId, String eventData) {
        // Default no-op - subclasses override if they need event handling
    }
    
    /**
     * Called when the sub-page is opened/switched to.
     * 
     * <p>Use this to load data, start animations, etc.
     * 
     * @param playerId the player opening the sub-page
     */
    default void onOpen(UUID playerId) {
        // Default no-op
    }
    
    /**
     * Called when the sub-page is closed or switched away from.
     * 
     * <p>Use this to cleanup resources, cancel pending operations, etc.
     * 
     * @param playerId the player closing the sub-page
     */
    default void onClose(UUID playerId) {
        // Default no-op
    }
    
    /**
     * Checks if this sub-page should be visible to the player.
     * 
     * <p>Used to hide admin-only pages from non-admin players, or
     * to conditionally show pages based on game state.
     * 
     * @param playerId the player to check
     * @return true if the sub-page should be visible
     */
    default boolean isVisibleTo(UUID playerId) {
        return true;
    }
    
    /**
     * Gets the notification badge count for this sub-page.
     * 
     * <p>Used to show notification badges on dropdown items
     * (e.g., "3 new guild invites").
     * 
     * @param playerId the player to check
     * @return badge count (0 for no badge)
     */
    default int getBadgeCount(UUID playerId) {
        return 0;
    }
}
