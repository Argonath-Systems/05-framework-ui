/**
 * Main Menu system for Argonath Systems.
 * 
 * <p>This package provides:
 * <ul>
 *   <li>{@link MainMenuManager} - Central manager for menu operations</li>
 *   <li>{@link MenuTab} - Enum of all available menu tabs</li>
 *   <li>{@link MenuTabHandler} - Interface for generating tab content</li>
 *   <li>{@link MenuTabListener} - Listener for tab events</li>
 * </ul>
 * 
 * <p>Mods can register handlers for their tabs:
 * <pre>
 * MainMenuManager.getInstance().registerTabHandler(MenuTab.GUILD, new GuildTabHandler());
 * </pre>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 */
package com.argonathsystems.framework.ui.menu;
