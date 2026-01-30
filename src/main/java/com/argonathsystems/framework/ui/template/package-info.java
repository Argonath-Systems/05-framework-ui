/**
 * Template processing utilities for HyUI integration.
 * 
 * <p>This package provides the bridge between Argonath's type-safe DataValue system
 * and HyUI's template processing capabilities.
 * 
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link com.argonathsystems.framework.ui.template.TemplateProcessorWrapper} - 
 *       Wraps HyUI's TemplateProcessor with DataValue support</li>
 *   <li>{@link com.argonathsystems.framework.ui.template.TemplateLoader} - 
 *       Loads and caches HYUIML template files</li>
 * </ul>
 * 
 * <h2>Usage Pattern</h2>
 * <pre>{@code
 * // Load template
 * TemplateLoader loader = new TemplateLoader();
 * String template = loader.loadHudTemplate("npc-dialogue").orElseThrow();
 * 
 * // Process with variables
 * TemplateProcessorWrapper processor = new TemplateProcessorWrapper()
 *     .setVariable("npc", npcData)
 *     .setVariable("choices", choicesList);
 * 
 * String processedHtml = processor.process(template);
 * }</pre>
 * 
 * <h2>Adapter Layer Integration</h2>
 * <p>The processed HTML should be passed to HyUI's PageBuilder or HudBuilder
 * through the adapter layer in 02-adapter-hytale. This package contains only
 * the template processing logic, not the rendering logic.
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.1.0
 */
package com.argonathsystems.framework.ui.template;
