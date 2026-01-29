package com.argonathsystems.framework.ui.dev;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UIHotReloadService}.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>File content loading and caching</li>
 *   <li>File change detection</li>
 *   <li>Reload functionality</li>
 *   <li>Security (path traversal prevention)</li>
 *   <li>Supplier pattern</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @since 1.1.0
 */
@DisplayName("UIHotReloadService Tests")
class UIHotReloadServiceTest {
    
    @TempDir
    Path tempDir;
    
    private UIHotReloadService service;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create standard directory structure
        Files.createDirectories(tempDir.resolve("pages"));
        Files.createDirectories(tempDir.resolve("huds"));
        Files.createDirectories(tempDir.resolve("components"));
    }
    
    @AfterEach
    void tearDown() {
        if (service != null) {
            service.close();
            service = null;
        }
    }
    
    // ========== Content Loading Tests ==========
    
    @Test
    @DisplayName("Should load HTML content from file")
    void shouldLoadHtmlFromFile() throws IOException {
        // Given
        String expectedContent = "<div>Hello World</div>";
        Files.writeString(tempDir.resolve("pages/test-page.hyuiml"), expectedContent);
        service = new UIHotReloadService(tempDir);
        
        // When
        String actualContent = service.getHtml("test-page");
        
        // Then
        assertEquals(expectedContent, actualContent);
    }
    
    @Test
    @DisplayName("Should return empty string for non-existent file")
    void shouldReturnEmptyForMissingFile() throws IOException {
        // Given
        service = new UIHotReloadService(tempDir);
        
        // When
        String content = service.getHtml("non-existent");
        
        // Then
        assertEquals("", content);
    }
    
    @Test
    @DisplayName("Should cache loaded content")
    void shouldCacheContent() throws IOException {
        // Given
        String originalContent = "<div>Original</div>";
        Path filePath = tempDir.resolve("pages/cached-page.hyuiml");
        Files.writeString(filePath, originalContent);
        service = new UIHotReloadService(tempDir);
        
        // Load once to cache
        String firstLoad = service.getHtml("cached-page");
        
        // Modify file directly (without triggering watcher)
        Files.writeString(filePath, "<div>Modified</div>");
        
        // When - get from cache
        String secondLoad = service.getHtml("cached-page");
        
        // Then - should still be cached value
        assertEquals(originalContent, firstLoad);
        assertEquals(originalContent, secondLoad);
    }
    
    @Test
    @DisplayName("Should find files in subdirectories")
    void shouldFindFilesInSubdirectories() throws IOException {
        // Given
        String pagesContent = "<div>Pages Content</div>";
        String hudsContent = "<div>Huds Content</div>";
        Files.writeString(tempDir.resolve("pages/my-page.hyuiml"), pagesContent);
        Files.writeString(tempDir.resolve("huds/my-hud.hyuiml"), hudsContent);
        service = new UIHotReloadService(tempDir);
        
        // When
        String page = service.getHtml("my-page");
        String hud = service.getHtml("my-hud");
        
        // Then
        assertEquals(pagesContent, page);
        assertEquals(hudsContent, hud);
    }
    
    @Test
    @DisplayName("Should support multiple file extensions")
    void shouldSupportMultipleExtensions() throws IOException {
        // Given
        Files.writeString(tempDir.resolve("test1.hyuiml"), "hyuiml");
        Files.writeString(tempDir.resolve("test2.html"), "html");
        service = new UIHotReloadService(tempDir);
        
        // When
        String hyuiml = service.getHtml("test1");
        String html = service.getHtml("test2");
        
        // Then
        assertEquals("hyuiml", hyuiml);
        assertEquals("html", html);
    }
    
    // ========== Reload Tests ==========
    
    @Test
    @DisplayName("Should reload single file on demand")
    void shouldReloadSingleFile() throws IOException {
        // Given
        Path filePath = tempDir.resolve("pages/reload-test.hyuiml");
        Files.writeString(filePath, "<div>Original</div>");
        service = new UIHotReloadService(tempDir);
        
        // Load initial
        assertEquals("<div>Original</div>", service.getHtml("reload-test"));
        
        // Modify file
        Files.writeString(filePath, "<div>Updated</div>");
        
        // When
        boolean reloaded = service.reload("reload-test");
        
        // Then
        assertTrue(reloaded);
        assertEquals("<div>Updated</div>", service.getHtml("reload-test"));
    }
    
    @Test
    @DisplayName("Should reload all files on demand")
    void shouldReloadAllFiles() throws IOException {
        // Given
        Files.writeString(tempDir.resolve("pages/page1.hyuiml"), "page1-v1");
        Files.writeString(tempDir.resolve("pages/page2.hyuiml"), "page2-v1");
        service = new UIHotReloadService(tempDir);
        
        // Load initial
        service.getHtml("page1");
        service.getHtml("page2");
        
        // Modify files
        Files.writeString(tempDir.resolve("pages/page1.hyuiml"), "page1-v2");
        Files.writeString(tempDir.resolve("pages/page2.hyuiml"), "page2-v2");
        
        // When
        int count = service.reloadAll();
        
        // Then
        assertTrue(count >= 2);
        assertEquals("page1-v2", service.getHtml("page1"));
        assertEquals("page2-v2", service.getHtml("page2"));
    }
    
    @Test
    @DisplayName("Should return false when reloading non-existent file")
    void shouldReturnFalseForNonExistentReload() throws IOException {
        // Given
        service = new UIHotReloadService(tempDir);
        
        // When
        boolean reloaded = service.reload("non-existent");
        
        // Then
        assertFalse(reloaded);
    }
    
    // ========== File Watcher Tests ==========
    
    @Test
    @DisplayName("Should detect file modification")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldDetectFileModification() throws Exception {
        // Given
        Path filePath = tempDir.resolve("pages/watched.hyuiml");
        Files.writeString(filePath, "original");
        service = new UIHotReloadService(tempDir, 100, true);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> changedPage = new AtomicReference<>();
        
        service.onReload(pageId -> {
            changedPage.set(pageId);
            latch.countDown();
        });
        
        // When - modify file
        Thread.sleep(200); // Wait for initial setup
        Files.writeString(filePath, "modified");
        
        // Then
        boolean detected = latch.await(3, TimeUnit.SECONDS);
        assertTrue(detected, "File change should be detected within timeout");
        assertEquals("watched", changedPage.get());
    }
    
    @Test
    @DisplayName("Should detect new file creation")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldDetectNewFile() throws Exception {
        // Given
        service = new UIHotReloadService(tempDir, 100, true);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> newPage = new AtomicReference<>();
        
        service.onReload(pageId -> {
            newPage.set(pageId);
            latch.countDown();
        });
        
        // When - create new file
        Thread.sleep(200);
        Files.writeString(tempDir.resolve("new-file.hyuiml"), "new content");
        
        // Then
        boolean detected = latch.await(3, TimeUnit.SECONDS);
        assertTrue(detected, "New file should be detected");
        assertEquals("new-file", newPage.get());
    }
    
    @Test
    @DisplayName("Should notify multiple listeners")
    void shouldNotifyMultipleListeners() throws Exception {
        // Given
        Path filePath = tempDir.resolve("multi.hyuiml");
        Files.writeString(filePath, "content");
        service = new UIHotReloadService(tempDir, 100, true);
        
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);
        
        service.onReload(pageId -> {
            callCount.incrementAndGet();
            latch.countDown();
        });
        service.onReload(pageId -> {
            callCount.incrementAndGet();
            latch.countDown();
        });
        
        // When
        Thread.sleep(200);
        Files.writeString(filePath, "modified");
        
        // Then
        latch.await(3, TimeUnit.SECONDS);
        assertEquals(2, callCount.get());
    }
    
    // ========== Supplier Pattern Tests ==========
    
    @Test
    @DisplayName("Should create working supplier")
    void shouldCreateSupplier() throws IOException {
        // Given
        Files.writeString(tempDir.resolve("supplier-test.hyuiml"), "content");
        service = new UIHotReloadService(tempDir);
        
        // When
        var supplier = service.createSupplier("supplier-test");
        
        // Then
        assertNotNull(supplier);
        assertEquals("content", supplier.get());
    }
    
    @Test
    @DisplayName("Supplier should return updated content after reload")
    void supplierShouldReturnUpdatedContent() throws IOException {
        // Given
        Path filePath = tempDir.resolve("supplier-reload.hyuiml");
        Files.writeString(filePath, "v1");
        service = new UIHotReloadService(tempDir);
        var supplier = service.createSupplier("supplier-reload");
        
        assertEquals("v1", supplier.get());
        
        // When - modify and reload
        Files.writeString(filePath, "v2");
        service.reload("supplier-reload");
        
        // Then
        assertEquals("v2", supplier.get());
    }
    
    // ========== Security Tests ==========
    
    @Test
    @DisplayName("Should reject path traversal in page ID")
    void shouldRejectPathTraversal() throws IOException {
        // Given
        service = new UIHotReloadService(tempDir);
        
        // Then
        assertThrows(SecurityException.class, () -> service.getHtml("../etc/passwd"));
        assertThrows(SecurityException.class, () -> service.getHtml("..\\windows\\system32"));
        assertThrows(SecurityException.class, () -> service.getHtml("foo/bar"));
        assertThrows(SecurityException.class, () -> service.getHtml("foo\\bar"));
    }
    
    @Test
    @DisplayName("Should reject null page ID")
    void shouldRejectNullPageId() throws IOException {
        // Given
        service = new UIHotReloadService(tempDir);
        
        // Then
        assertThrows(IllegalArgumentException.class, () -> service.getHtml(null));
        assertThrows(IllegalArgumentException.class, () -> service.getHtml(""));
    }
    
    // ========== Lifecycle Tests ==========
    
    @Test
    @DisplayName("Should report running state correctly")
    void shouldReportRunningState() throws IOException {
        // Given
        service = new UIHotReloadService(tempDir);
        
        // Then
        assertTrue(service.isRunning());
        
        // When
        service.close();
        
        // Then
        assertFalse(service.isRunning());
    }
    
    @Test
    @DisplayName("Should create directory if not exists")
    void shouldCreateDirectoryIfNotExists() throws IOException {
        // Given
        Path nonExistent = tempDir.resolve("new-ui-dir");
        assertFalse(Files.exists(nonExistent));
        
        // When
        service = new UIHotReloadService(nonExistent);
        
        // Then
        assertTrue(Files.exists(nonExistent));
    }
    
    @Test
    @DisplayName("Should get registered page IDs")
    void shouldGetRegisteredPageIds() throws IOException {
        // Given
        Files.writeString(tempDir.resolve("page1.hyuiml"), "1");
        Files.writeString(tempDir.resolve("page2.hyuiml"), "2");
        Files.writeString(tempDir.resolve("pages/page3.hyuiml"), "3");
        service = new UIHotReloadService(tempDir);
        
        // When
        var pageIds = service.getRegisteredPageIds();
        
        // Then
        assertTrue(pageIds.size() >= 3);
        assertTrue(pageIds.contains("page1"));
        assertTrue(pageIds.contains("page2"));
        assertTrue(pageIds.contains("page3"));
    }
    
    @Test
    @DisplayName("Should remove listener")
    void shouldRemoveListener() throws IOException {
        // Given
        service = new UIHotReloadService(tempDir);
        AtomicInteger callCount = new AtomicInteger(0);
        var listener = (java.util.function.Consumer<String>) pageId -> callCount.incrementAndGet();
        
        service.onReload(listener);
        
        // When
        boolean removed = service.removeListener(listener);
        
        // Then
        assertTrue(removed);
    }
}
