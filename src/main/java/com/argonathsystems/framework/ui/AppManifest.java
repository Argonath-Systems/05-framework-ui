package com.argonathsystems.framework.ui;

import java.util.Objects;

/**
 * Metadata for an application registered in the Unified UI.
 */
public record AppManifest(
    String id,
    String displayName,
    String iconPath, // Path relative to framework assets or adapter resource
    String entryScreenId,
    String permission
) {
    public AppManifest {
        Objects.requireNonNull(id, "App ID cannot be null");
        Objects.requireNonNull(displayName, "Display Name cannot be null");
        Objects.requireNonNull(entryScreenId, "Entry Screen ID cannot be null");
    }
}
