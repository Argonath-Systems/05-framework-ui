package com.argonathsystems.framework.ui.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builder for Quest Book Page using HyUIML.
 * 
 * <p>Full-screen quest journal with:
 * <ul>
 *   <li>Active/Complete/Failed tabs
 *   <li>Category-based quest organization
 *   <li>Sidebar with quest list
 *   <li>Detail panel with objectives and rewards
 *   <li>Track/Untrack/Abandon actions
 * </ul>
 * 
 * <p><b>Platform Agnostic</b>: No hytale.* imports. Rendering via adapter layer.
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public class QuestBookPageBuilder {
    
    private String activeTab = "active"; // "active", "complete", "failed"
    private final List<QuestCategory> categories;
    private QuestDetail selectedQuest;
    private Supplier<String> templateSupplier;
    
    public QuestBookPageBuilder() {
        this.categories = new ArrayList<>();
    }
    
    /**
     * Set the template supplier for hot reload support.
     * 
     * @param templateSupplier Template supplier
     * @return this builder
     */
    public QuestBookPageBuilder setTemplateSupplier(Supplier<String> templateSupplier) {
        this.templateSupplier = templateSupplier;
        return this;
    }
    
    /**
     * Set the active tab.
     * 
     * @param tab Tab name: "active", "complete", or "failed"
     * @return this builder
     */
    public QuestBookPageBuilder setActiveTab(String tab) {
        this.activeTab = tab;
        return this;
    }
    
    /**
     * Add a quest category.
     * 
     * @param category Quest category
     * @return this builder
     */
    public QuestBookPageBuilder addCategory(QuestCategory category) {
        this.categories.add(category);
        return this;
    }
    
    /**
     * Set the selected quest for detail panel.
     * 
     * @param quest Quest details
     * @return this builder
     */
    public QuestBookPageBuilder setSelectedQuest(QuestDetail quest) {
        this.selectedQuest = quest;
        return this;
    }
    
    /**
     * Clear all categories.
     * 
     * @return this builder
     */
    public QuestBookPageBuilder clearCategories() {
        this.categories.clear();
        return this;
    }
    
    /**
     * Generate HyUIML HTML.
     * 
     * @return HTML string
     */
    public String generateHtml() {
        if (templateSupplier == null) {
            throw new IllegalStateException("Template supplier not set");
        }
        
        String template = templateSupplier.get();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("activeTab", activeTab);
        
        List<Map<String, Object>> categoriesList = new ArrayList<>();
        for (QuestCategory category : categories) {
            categoriesList.add(category.toMap());
        }
        variables.put("categories", categoriesList);
        
        if (selectedQuest != null) {
            variables.put("selectedQuest", selectedQuest.toMap());
        }
        
        return processTemplate(template, variables);
    }
    
    private String processTemplate(String template, Map<String, Object> variables) {
        // Simplified template processing (production uses HyUI TemplateProcessor)
        String result = template;
        
        result = result.replace("{{$activeTab}}", (String) variables.get("activeTab"));
        
        // Process categories
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoriesList = (List<Map<String, Object>>) variables.get("categories");
        // ... template processing logic ...
        
        return result;
    }
    
    /**
     * Get current active tab.
     * 
     * @return Tab name
     */
    public String getActiveTab() {
        return activeTab;
    }
    
    /**
     * Get quest categories.
     * 
     * @return Categories list
     */
    public List<QuestCategory> getCategories() {
        return List.copyOf(categories);
    }
}
