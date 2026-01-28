/**
 * Development mode features for the UI Framework.
 * 
 * <p>This package contains utilities for UI development including:
 * <ul>
 *   <li>{@link UIHotReloadService} - File watching and hot reload for HYUIML files</li>
 *   <li>{@link DevModeConfig} - Configuration for development features</li>
 * </ul>
 * 
 * <h2>Production Safety</h2>
 * <p>All features in this package are designed to be disabled in production:
 * <ul>
 *   <li>Default configuration has hot reload disabled</li>
 *   <li>Setting {@code ARGONATH_ENV=production} force-disables all dev features</li>
 *   <li>The {@code /uireload} command requires admin permissions</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Initialize in development
 * UnifiedUIManager.getInstance().init(accessor);
 * UnifiedUIManager.getInstance().initDevMode(DevModeConfig.development());
 * 
 * // Create hot-reloadable supplier
 * Supplier<String> supplier = UnifiedUIManager.getInstance()
 *     .createUISupplier("my-page", "ui/pages/my-page.hyuiml");
 * 
 * // Use with HyUI PageBuilder
 * PageBuilder.fromHtml(supplier.get()).open(store);
 * }</pre>
 * 
 * @since 1.1.0
 * @see com.argonathsystems.framework.ui.UnifiedUIManager#initDevMode
 */
package com.argonathsystems.framework.ui.dev;
