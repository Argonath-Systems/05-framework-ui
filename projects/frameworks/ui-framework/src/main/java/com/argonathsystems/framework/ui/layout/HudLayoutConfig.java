package com.argonathsystems.framework.ui.layout;

import java.util.Map;

/**
 * Configuration record for HUD layouts.
 * Used for serialization.
 */
public record HudLayoutConfig(
    Map<String, HudElementPosition> elements
) {
    public static HudLayoutConfig of(Map<String, HudElementPosition> elements) {
        return new HudLayoutConfig(elements);
    }
}
