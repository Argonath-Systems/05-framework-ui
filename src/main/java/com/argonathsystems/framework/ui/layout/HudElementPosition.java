package com.argonathsystems.framework.ui.layout;

/**
 * Represents the position and state of a HUD element.
 */
public record HudElementPosition(
    String elementId,
    float x,
    float y,
    String anchor, // "TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT", "CENTER"
    float scale,
    boolean visible
) {
    public static HudElementPosition defaults(String id) {
        return new HudElementPosition(id, 0, 0, "TOP_LEFT", 1.0f, true);
    }
}
