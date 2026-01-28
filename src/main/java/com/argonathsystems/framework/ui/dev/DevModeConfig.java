package com.argonathsystems.framework.ui.dev;

import java.nio.file.Path;
import java.util.Set;

/**
 * Configuration for development mode features in the UI framework.
 * 
 * <p>This configuration controls hot reload, logging, and other 
 * development-time features that should be disabled in production.
 * 
 * <p>Usage:
 * <pre>{@code
 * // For development
 * DevModeConfig config = DevModeConfig.development();
 * UnifiedUIManager.getInstance().initDevMode(config);
 * 
 * // For production (disables all dev features)
 * DevModeConfig config = DevModeConfig.production();
 * 
 * // From configuration file
 * DevModeConfig config = DevModeConfig.builder()
 *     .enabled(true)
 *     .hotReloadEnabled(true)
 *     .uiDirectory(Path.of("config/ui"))
 *     .build();
 * }</pre>
 * 
 * @param enabled Master switch for all development mode features
 * @param hotReloadEnabled Enable UI hot reload specifically
 * @param uiDirectory Directory to watch for UI files
 * @param pollIntervalMs File watcher poll interval in milliseconds
 * @param autoRefreshPlayers Automatically refresh player UIs on file change
 * @param logChanges Log file change events to console
 * @param watchedExtensions File extensions to watch for changes
 * 
 * @author Argonath Systems
 * @since 1.1.0
 */
public record DevModeConfig(
    boolean enabled,
    boolean hotReloadEnabled,
    Path uiDirectory,
    int pollIntervalMs,
    boolean autoRefreshPlayers,
    boolean logChanges,
    Set<String> watchedExtensions
) {
    
    /** Default watched extensions */
    public static final Set<String> DEFAULT_EXTENSIONS = Set.of(".hyuiml", ".html", ".css");
    
    /** Default UI directory */
    public static final Path DEFAULT_UI_DIRECTORY = Path.of("config", "ui");
    
    /** Default poll interval */
    public static final int DEFAULT_POLL_INTERVAL_MS = 500;
    
    /**
     * Create a configuration for development environments.
     * Enables all hot reload features with sensible defaults.
     * 
     * @return Development configuration
     */
    public static DevModeConfig development() {
        return new DevModeConfig(
            true,                     // enabled
            true,                     // hotReloadEnabled
            DEFAULT_UI_DIRECTORY,     // uiDirectory
            DEFAULT_POLL_INTERVAL_MS, // pollIntervalMs
            true,                     // autoRefreshPlayers
            true,                     // logChanges
            DEFAULT_EXTENSIONS        // watchedExtensions
        );
    }
    
    /**
     * Create a configuration for production environments.
     * All development features are disabled.
     * 
     * @return Production configuration (all features disabled)
     */
    public static DevModeConfig production() {
        return new DevModeConfig(
            false,  // enabled
            false,  // hotReloadEnabled
            null,   // uiDirectory (not used)
            0,      // pollIntervalMs (not used)
            false,  // autoRefreshPlayers
            false,  // logChanges
            Set.of()// watchedExtensions (not used)
        );
    }
    
    /**
     * Create a new builder for DevModeConfig.
     * 
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Check if hot reload should be active.
     * Returns false if master switch is off or explicitly disabled.
     * 
     * @return true if hot reload should be enabled
     */
    public boolean isHotReloadActive() {
        return enabled && hotReloadEnabled && uiDirectory != null;
    }
    
    /**
     * Get the UI directory, returning default if not set.
     * 
     * @return The UI directory path
     */
    public Path getUIDirectoryOrDefault() {
        return uiDirectory != null ? uiDirectory : DEFAULT_UI_DIRECTORY;
    }
    
    /**
     * Validate configuration and return any issues.
     * 
     * @return Empty list if valid, otherwise list of issue descriptions
     */
    public java.util.List<String> validate() {
        java.util.List<String> issues = new java.util.ArrayList<>();
        
        if (enabled && hotReloadEnabled) {
            if (uiDirectory == null) {
                issues.add("UI directory must be set when hot reload is enabled");
            }
            if (pollIntervalMs < 100) {
                issues.add("Poll interval should be at least 100ms to avoid excessive CPU usage");
            }
            if (watchedExtensions == null || watchedExtensions.isEmpty()) {
                issues.add("At least one file extension should be configured for watching");
            }
        }
        
        return issues;
    }
    
    /**
     * Builder for DevModeConfig.
     */
    public static final class Builder {
        private boolean enabled = false;
        private boolean hotReloadEnabled = false;
        private Path uiDirectory = DEFAULT_UI_DIRECTORY;
        private int pollIntervalMs = DEFAULT_POLL_INTERVAL_MS;
        private boolean autoRefreshPlayers = true;
        private boolean logChanges = true;
        private Set<String> watchedExtensions = DEFAULT_EXTENSIONS;
        
        private Builder() {}
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder hotReloadEnabled(boolean hotReloadEnabled) {
            this.hotReloadEnabled = hotReloadEnabled;
            return this;
        }
        
        public Builder uiDirectory(Path uiDirectory) {
            this.uiDirectory = uiDirectory;
            return this;
        }
        
        public Builder uiDirectory(String uiDirectory) {
            this.uiDirectory = Path.of(uiDirectory);
            return this;
        }
        
        public Builder pollIntervalMs(int pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
            return this;
        }
        
        public Builder autoRefreshPlayers(boolean autoRefreshPlayers) {
            this.autoRefreshPlayers = autoRefreshPlayers;
            return this;
        }
        
        public Builder logChanges(boolean logChanges) {
            this.logChanges = logChanges;
            return this;
        }
        
        public Builder watchedExtensions(Set<String> watchedExtensions) {
            this.watchedExtensions = watchedExtensions;
            return this;
        }
        
        public Builder watchedExtensions(String... extensions) {
            this.watchedExtensions = Set.of(extensions);
            return this;
        }
        
        public DevModeConfig build() {
            return new DevModeConfig(
                enabled,
                hotReloadEnabled,
                uiDirectory,
                pollIntervalMs,
                autoRefreshPlayers,
                logChanges,
                watchedExtensions
            );
        }
    }
}
