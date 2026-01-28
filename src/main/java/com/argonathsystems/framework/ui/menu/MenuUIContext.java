package com.argonathsystems.framework.ui.menu;

import com.argonathsystems.framework.accessorapi.ui.UIContext;

/**
 * UI Context for the main menu system.
 * Contains the HYUIML content to render.
 * 
 * @param hyuimlContent The HYUIML markup to render
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 */
public record MenuUIContext(String hyuimlContent) implements UIContext {
}
