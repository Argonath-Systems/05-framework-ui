package com.argonathsystems.framework.ui.template;

/**
 * Factory for creating TemplateProcessorWrapper instances.
 * 
 * <p>This factory is implemented by the adapter layer to provide platform-specific
 * template processor implementations. The framework layer uses this factory
 * to create processors without knowing the underlying implementation.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // In framework layer (dependency injected)
 * public class DialoguePageBuilder {
 *     private final TemplateProcessorFactory processorFactory;
 *     
 *     public DialoguePageBuilder(TemplateProcessorFactory factory) {
 *         this.processorFactory = factory;
 *     }
 *     
 *     public String buildDialoguePage(NpcDialogueData data) {
 *         TemplateProcessorWrapper processor = processorFactory.create();
 *         processor.setVariable("npc", data.getNpcData());
 *         return processor.process(template);
 *     }
 * }
 * }</pre>
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.1.0
 * @see TemplateProcessorWrapper
 */
public interface TemplateProcessorFactory {
    
    /**
     * Creates a new TemplateProcessorWrapper instance.
     * 
     * <p>Each call returns a fresh processor instance. Processors are not
     * thread-safe and should not be shared between threads.
     * 
     * @return a new TemplateProcessorWrapper
     */
    TemplateProcessorWrapper create();
    
    /**
     * Creates a new TemplateProcessorWrapper with pre-registered components.
     * 
     * <p>The returned processor will have common UI components pre-registered
     * based on the specified component set.
     * 
     * @param componentSet the name of the component set to load
     * @return a new TemplateProcessorWrapper with components registered
     */
    default TemplateProcessorWrapper createWithComponents(String componentSet) {
        return create();
    }
}
