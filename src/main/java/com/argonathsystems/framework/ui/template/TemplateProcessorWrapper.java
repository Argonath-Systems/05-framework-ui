package com.argonathsystems.framework.ui.template;

import com.argonathsystems.framework.accessorapi.data.DataValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for template processing that bridges DataValue types with UI rendering.
 * 
 * <p>This interface defines the contract for template processing. Implementations
 * (such as HyUITemplateProcessor in the adapter layer) handle the actual rendering
 * using platform-specific libraries like HyUI.
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * TemplateProcessorWrapper processor = templateProcessorFactory.create();
 * processor.setVariable("playerName", DataValue.of("Gandalf"))
 *          .setVariable("playerLevel", DataValue.of(60));
 * 
 * String processedHtml = processor.process(templateString);
 * }</pre>
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.1.0
 * @see DataValue
 */
public interface TemplateProcessorWrapper {
    
    /**
     * Sets a variable with a DataValue.
     * 
     * @param name the variable name (used as {{$name}} in templates)
     * @param value the DataValue to set
     * @return this wrapper for chaining
     */
    TemplateProcessorWrapper setVariable(String name, DataValue value);
    
    /**
     * Sets a variable with a raw Object value.
     * 
     * <p>This method allows setting non-DataValue objects directly, useful
     * for POJOs with getters that template engines can introspect.
     * 
     * @param name the variable name
     * @param value the value (POJO, primitive, or collection)
     * @return this wrapper for chaining
     */
    TemplateProcessorWrapper setVariable(String name, Object value);
    
    /**
     * Sets multiple variables from a DataValue map.
     * 
     * @param variables map of variable names to DataValues
     * @return this wrapper for chaining
     */
    TemplateProcessorWrapper setVariables(Map<String, DataValue> variables);
    
    /**
     * Registers a reusable component template.
     * 
     * @param name the component name
     * @param template the component template
     * @return this wrapper for chaining
     */
    TemplateProcessorWrapper registerComponent(String name, String template);
    
    /**
     * Processes a template string, replacing variables and expanding components.
     * 
     * @param template the template string with variable placeholders
     * @return the processed string
     */
    String process(String template);
    
    /**
     * Builder for creating template data structures.
     * 
     * <p>Provides a fluent API for building nested DataValue structures
     * that can be passed to templates.
     */
    class DataBuilder {
        private final Map<String, DataValue> data = new HashMap<>();
        
        /**
         * Adds a string value.
         */
        public DataBuilder put(String key, String value) {
            data.put(key, DataValue.of(value));
            return this;
        }
        
        /**
         * Adds an integer value.
         */
        public DataBuilder put(String key, int value) {
            data.put(key, DataValue.of(value));
            return this;
        }
        
        /**
         * Adds a long value.
         */
        public DataBuilder put(String key, long value) {
            data.put(key, DataValue.of(value));
            return this;
        }
        
        /**
         * Adds a double value.
         */
        public DataBuilder put(String key, double value) {
            data.put(key, DataValue.of(value));
            return this;
        }
        
        /**
         * Adds a boolean value.
         */
        public DataBuilder put(String key, boolean value) {
            data.put(key, DataValue.of(value));
            return this;
        }
        
        /**
         * Adds a nested DataValue.
         */
        public DataBuilder put(String key, DataValue value) {
            data.put(key, value);
            return this;
        }
        
        /**
         * Adds a list of DataValues.
         */
        public DataBuilder putList(String key, List<DataValue> list) {
            data.put(key, DataValue.of(list));
            return this;
        }
        
        /**
         * Builds the DataValue map.
         */
        public DataValue build() {
            return DataValue.of(data);
        }
        
        /**
         * Returns the raw map for use with setVariables().
         */
        public Map<String, DataValue> toMap() {
            return new HashMap<>(data);
        }
    }
    
    /**
     * Creates a new DataBuilder for constructing template data.
     * 
     * @return a new DataBuilder instance
     */
    static DataBuilder dataBuilder() {
        return new DataBuilder();
    }
}
