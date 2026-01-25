package com.argonathsystems.framework.ui.layout;

import java.util.Map;
import java.util.UUID;

/**
 * Configuration record for HUD layouts.
 * Used for serialization.
 */
public record HudLayoutConfig(
    UUID playerId,
    Map<String, HudElementPosition> elements
) {
    public static HudLayoutConfig of(UUID playerId, Map<String, HudElementPosition> elements) {
        return new HudLayoutConfig(playerId, elements);
    }
}
