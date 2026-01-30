/**
 * Combat UI data builders for action bars and unit frames.
 * 
 * <p>This package contains platform-agnostic data models for combat UIs:
 * <ul>
 *   <li>{@link ActionBarBuilder} - Builder for action bar HUD with ability slots</li>
 *   <li>{@link ActionSlot} - Individual action slot data</li>
 *   <li>{@link CombatFramesBuilder} - Builder for player/target/party frames</li>
 *   <li>{@link UnitFrame} - Unit frame data (player, target, party member)</li>
 *   <li>{@link ResourceBar} - Health/mana/energy bar data</li>
 * </ul>
 * 
 * <p>Rendering is handled by the adapter layer using HyUI's HudBuilder.
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
package com.argonathsystems.framework.ui.combat;
