package com.argonathsystems.framework.ui.dev;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Service for hot-reloading HYUIML files during development.
 * 
 * <p>Architecture:
 * <ul>
 *   <li>File watcher thread monitors UI directory</li>
 *   <li>Content cache stores loaded HTML</li>
 *   <li>Suppliers provide fresh content on each request</li>
 *   <li>Listeners notified on file changes</li>
 * </ul>
 * 
 * <p>Thread Safety: All operations are thread-safe.
 * 
 * <p>Usage:
 * <pre>{@code
 * UIHotReloadService service = new UIHotReloadService(Path.of("config/ui"));
 * 
 * // Get content directly
 * String html = service.getHtml("quest-tracker");
 * 
 * // Or use supplier pattern with PageBuilder
 * Supplier<String> supplier = service.createSupplier("quest-tracker");
 * PageBuilder.pageForPlayer(playerRef)
 *     .fromHtml(supplier.get())
 *     .open(store);
 * 
 * // Listen for changes
 * service.onReload(pageId -> System.out.println("Changed: " + pageId));
 * }</pre>
 * 
 * @author Argonath Systems
 * @since 1.1.0
 */
public final class UIHotReloadService implements AutoCloseable {
    
    private static final Logger LOG = Logger.getLogger(UIHotReloadService.class.getName());
    
    /** Supported file extensions for UI files */
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".hyuiml", ".html", ".htm");
    
    private final Path uiDirectory;
    private final WatchService watchService;
    private final Map<String, String> contentCache;
    private final Map<WatchKey, Path> watchKeyToPath;
    private final List<Consumer<String>> changeListeners;
    private final Thread watchThread;
    private final int pollIntervalMs;
    private final boolean logChanges;
    private volatile boolean running;
    
    /**
     * Create a new hot reload service with default settings.
     * 
     * @param uiDirectory The directory to watch for HYUIML files
     * @throws IOException If watch service cannot be created
     */
    public UIHotReloadService(Path uiDirectory) throws IOException {
        this(uiDirectory, 500, true);
    }
    
    /**
     * Create a new hot reload service with custom settings.
     * 
     * @param uiDirectory The directory to watch for HYUIML files
     * @param pollIntervalMs Interval between poll cycles (not used directly, but for future)
     * @param logChanges Whether to log file change events
     * @throws IOException If watch service cannot be created
     */
    public UIHotReloadService(Path uiDirectory, int pollIntervalMs, boolean logChanges) throws IOException {
        this.uiDirectory = uiDirectory.toAbsolutePath();
        this.pollIntervalMs = pollIntervalMs;
        this.logChanges = logChanges;
        this.contentCache = new ConcurrentHashMap<>();
        this.watchKeyToPath = new ConcurrentHashMap<>();
        this.changeListeners = new CopyOnWriteArrayList<>();
        this.running = true;
        
        // Create directory if it doesn't exist
        if (!Files.exists(this.uiDirectory)) {
            Files.createDirectories(this.uiDirectory);
            LOG.info("Created UI directory: " + this.uiDirectory);
        }
        
        // Create watch service
        this.watchService = FileSystems.getDefault().newWatchService();
        
        // Register directory and subdirectories
        registerWatchRecursively(this.uiDirectory);
        
        // Pre-load all existing files
        preloadAllFiles();
        
        // Start watch thread
        this.watchThread = new Thread(this::watchLoop, "UI-HotReload-Watcher");
        this.watchThread.setDaemon(true);
        this.watchThread.start();
        
        LOG.info("UI Hot Reload Service started. Watching: " + this.uiDirectory);
    }
    
    /**
     * Get HTML content for a page, returning cached or freshly loaded content.
     * 
     * @param pageId The page identifier (maps to filename without extension)
     * @return The HYUIML content, or empty string if not found
     */
    public String getHtml(String pageId) {
        validatePageId(pageId);
        
        // Check cache first
        String cached = contentCache.get(pageId);
        if (cached != null) {
            return cached;
        }
        
        // Try to load from disk
        return loadFromDisk(pageId);
    }
    
    /**
     * Create a supplier that always returns fresh content.
     * Use with PageBuilder.registerPage(id, supplier).
     * 
     * <p>The supplier will read from cache if available, 
     * falling back to disk if needed.
     * 
     * @param pageId The page identifier
     * @return Supplier that provides current content
     */
    public Supplier<String> createSupplier(String pageId) {
        validatePageId(pageId);
        return () -> getHtml(pageId);
    }
    
    /**
     * Force reload a specific file from disk.
     * 
     * @param pageId The page to reload
     * @return true if file was found and reloaded
     */
    public boolean reload(String pageId) {
        validatePageId(pageId);
        
        // Clear cache entry
        contentCache.remove(pageId);
        
        // Reload from disk
        String content = loadFromDisk(pageId);
        if (content != null && !content.isEmpty()) {
            if (logChanges) {
                LOG.info("Reloaded UI file: " + pageId);
            }
            notifyListeners(pageId);
            return true;
        }
        return false;
    }
    
    /**
     * Force reload all watched files from disk.
     * 
     * @return Number of files reloaded
     */
    public int reloadAll() {
        Set<String> pageIds = new HashSet<>(contentCache.keySet());
        
        // Also scan directory for any new files
        try {
            scanDirectory(uiDirectory, pageIds);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error scanning UI directory", e);
        }
        
        int count = 0;
        for (String pageId : pageIds) {
            if (reload(pageId)) {
                count++;
            }
        }
        
        if (logChanges) {
            LOG.info("Reloaded " + count + " UI files");
        }
        return count;
    }
    
    /**
     * Get all registered page IDs currently in cache.
     * 
     * @return Set of page IDs
     */
    public Set<String> getRegisteredPageIds() {
        return Collections.unmodifiableSet(contentCache.keySet());
    }
    
    /**
     * Register a listener for file change events.
     * 
     * @param listener Consumer receiving the changed pageId
     */
    public void onReload(Consumer<String> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        changeListeners.add(listener);
    }
    
    /**
     * Remove a previously registered listener.
     * 
     * @param listener The listener to remove
     * @return true if listener was found and removed
     */
    public boolean removeListener(Consumer<String> listener) {
        return changeListeners.remove(listener);
    }
    
    /**
     * Check if the service is currently running.
     * 
     * @return true if watching for changes
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Stop watching and release resources.
     */
    @Override
    public void close() {
        running = false;
        
        if (watchThread != null) {
            watchThread.interrupt();
        }
        
        try {
            watchService.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error closing watch service", e);
        }
        
        contentCache.clear();
        changeListeners.clear();
        
        LOG.info("UI Hot Reload Service stopped");
    }
    
    // ========== Private Implementation ==========
    
    private void validatePageId(String pageId) {
        if (pageId == null || pageId.isEmpty()) {
            throw new IllegalArgumentException("Page ID cannot be null or empty");
        }
        // Security: prevent path traversal
        if (pageId.contains("..") || pageId.contains("/") || pageId.contains("\\")) {
            throw new SecurityException("Invalid page ID (path traversal attempt): " + pageId);
        }
    }
    
    private void registerWatchRecursively(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE);
        watchKeyToPath.put(key, dir);
        
        // Register subdirectories
        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(Files::isDirectory)
                 .forEach(subdir -> {
                     try {
                         registerWatchRecursively(subdir);
                     } catch (IOException e) {
                         LOG.log(Level.WARNING, "Failed to register watch on: " + subdir, e);
                     }
                 });
        }
    }
    
    private void preloadAllFiles() {
        try {
            Set<String> pageIds = new HashSet<>();
            scanDirectory(uiDirectory, pageIds);
            
            for (String pageId : pageIds) {
                loadFromDisk(pageId);
            }
            
            LOG.info("Pre-loaded " + pageIds.size() + " UI files");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error pre-loading UI files", e);
        }
    }
    
    private void scanDirectory(Path dir, Set<String> pageIds) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isUIFile)
                 .forEach(path -> {
                     String pageId = pathToPageId(path);
                     if (pageId != null) {
                         pageIds.add(pageId);
                     }
                 });
        }
    }
    
    private boolean isUIFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
    
    private String pathToPageId(Path path) {
        // Get relative path from UI directory
        Path relative = uiDirectory.relativize(path);
        String name = relative.toString();
        
        // Remove extension
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }
        
        // Replace path separators with dashes for nested files
        // e.g., pages/quest-tracker.hyuiml -> pages-quest-tracker
        // But we prefer just the filename for simplicity
        // e.g., pages/quest-tracker.hyuiml -> quest-tracker
        String filename = path.getFileName().toString();
        dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }
    
    private Path pageIdToPath(String pageId) {
        // Try each supported extension in common directories
        String[] directories = {"", "pages/", "huds/", "components/"};
        
        for (String dir : directories) {
            for (String ext : SUPPORTED_EXTENSIONS) {
                Path candidate = uiDirectory.resolve(dir + pageId + ext);
                if (Files.exists(candidate)) {
                    return candidate;
                }
            }
        }
        
        // Try recursive search as fallback
        try (Stream<Path> paths = Files.walk(uiDirectory)) {
            return paths.filter(Files::isRegularFile)
                        .filter(this::isUIFile)
                        .filter(p -> pageId.equals(pathToPageId(p)))
                        .findFirst()
                        .orElse(null);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error searching for page: " + pageId, e);
            return null;
        }
    }
    
    private String loadFromDisk(String pageId) {
        Path filePath = pageIdToPath(pageId);
        
        if (filePath == null || !Files.exists(filePath)) {
            LOG.warning("UI file not found: " + pageId);
            return "";
        }
        
        try {
            String content = Files.readString(filePath);
            contentCache.put(pageId, content);
            return content;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error reading UI file: " + filePath, e);
            return "";
        }
    }
    
    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.take();
                Path dir = watchKeyToPath.get(key);
                
                if (dir == null) {
                    key.reset();
                    continue;
                }
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path changedPath = dir.resolve(pathEvent.context());
                    
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(changedPath)) {
                        // Register new subdirectory
                        try {
                            registerWatchRecursively(changedPath);
                        } catch (IOException e) {
                            LOG.log(Level.WARNING, "Failed to register watch on new directory: " + changedPath, e);
                        }
                    } else if (isUIFile(changedPath)) {
                        handleFileChange(changedPath, kind);
                    }
                }
                
                boolean valid = key.reset();
                if (!valid) {
                    watchKeyToPath.remove(key);
                    if (watchKeyToPath.isEmpty()) {
                        LOG.warning("All watch keys invalidated, stopping watcher");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                // Service closed, exit gracefully
                break;
            }
        }
    }
    
    private void handleFileChange(Path changedPath, WatchEvent.Kind<?> kind) {
        String pageId = pathToPageId(changedPath);
        
        if (pageId == null) {
            return;
        }
        
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            contentCache.remove(pageId);
            if (logChanges) {
                LOG.info("UI file deleted: " + pageId);
            }
        } else {
            // ENTRY_CREATE or ENTRY_MODIFY
            // Small delay to ensure file write is complete
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            
            // Clear cache and reload
            contentCache.remove(pageId);
            loadFromDisk(pageId);
            
            if (logChanges) {
                LOG.info("UI file changed: " + pageId + " (" + kind.name() + ")");
            }
        }
        
        notifyListeners(pageId);
    }
    
    private void notifyListeners(String pageId) {
        for (Consumer<String> listener : changeListeners) {
            try {
                listener.accept(pageId);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error in reload listener", e);
            }
        }
    }
}
