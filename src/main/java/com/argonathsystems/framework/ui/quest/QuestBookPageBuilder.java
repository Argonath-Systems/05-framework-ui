package com.argonathsystems.framework.ui.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Platform-agnostic data builder for Quest Book Page using HyUIML.
 * 
 * <p>This class builds the data model for Quest Book UIs. It does NOT process
 * templates or render UIs directly - that is handled by the adapter layer using
 * HyUI's {@code TemplateProcessor} and {@code PageBuilder}.
 * 
 * <h2>Architecture</h2>
 * <pre>{@code
 *   QuestBookPageBuilder (this class)
 *          ↓ builds data
 *   Map<String, Object> templateVariables
 *          ↓ passed to
 *   02-adapter-hytale/QuestBookPageAdapter
 *          ↓ uses HyUI
 *   TemplateProcessor → PageBuilder → Player UI
 * }</pre>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Active/Complete/Failed tabs</li>
 *   <li>Category-based quest organization</li>
 *   <li>Sidebar with quest list</li>
 *   <li>Detail panel with objectives and rewards</li>
 *   <li>Track/Untrack/Abandon actions</li>
 *   <li>Hot reload support via template supplier</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @see QuestCategory
 * @see QuestDetail
 * @since 1.0.0
 */
public class QuestBookPageBuilder {
    
    private String activeTab = "active"; // "active", "complete", "failed"
    private String selectedQuestId;
    private final List<QuestCategory> categories;
    private QuestDetail selectedQuest;
    private Supplier<String> templateSupplier;
    
    /**
     * Create a new quest book page builder.
     */
    public QuestBookPageBuilder() {
        this.categories = new ArrayList<>();
    }
    
    /**
     * Set the template supplier for hot reload support.
     * 
     * <p>The supplier should return the raw HyUIML template content.
     * In development mode, this can be wired to {@code UIHotReloadService.createSupplier()}.
     * 
     * @param templateSupplier Supplier providing the base HyUIML template
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
        if (quest != null) {
            this.selectedQuestId = quest.getId();
        }
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
     * Build the template variables map for HyUI TemplateProcessor.
     * 
     * <p>This method builds a map of variables that can be passed to
     * HyUI's {@code TemplateProcessor.setVariable()} method. The adapter
     * layer should use this to process the template.
     * 
     * <h3>Variable Structure</h3>
     * <pre>{@code
     * {
     *   "activeTab": "active",
     *   "isActiveTab": true,
     *   "isCompleteTab": false,
     *   "isFailedTab": false,
     *   "categories": [ { "name": "...", "count": 5, "quests": [...] }, ... ],
     *   "selectedQuest": { "id": "...", "name": "...", ... },
     *   "hasSelectedQuest": true/false
     * }
     * }</pre>
     * 
     * @return Map of template variables for TemplateProcessor
     */
    public Map<String, Object> buildTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        // Tab state
        variables.put("activeTab", activeTab);
        variables.put("isActiveTab", "active".equals(activeTab));
        variables.put("isCompleteTab", "complete".equals(activeTab));
        variables.put("isFailedTab", "failed".equals(activeTab));
        
        // Categories with quest lists
        List<Map<String, Object>> categoriesList = new ArrayList<>();
        for (QuestCategory category : categories) {
            categoriesList.add(category.toMap());
        }
        variables.put("categories", categoriesList);
        
        // Selected quest detail
        variables.put("hasSelectedQuest", selectedQuest != null);
        if (selectedQuest != null) {
            variables.put("selectedQuest", selectedQuest.toMap());
            variables.put("selectedQuestId", selectedQuestId);
        }
        
        return variables;
    }
    
    /**
     * Get the raw template content from the supplier.
     * 
     * <p>The adapter layer should call this to get the template, then use
     * {@link #buildTemplateVariables()} to process it with HyUI TemplateProcessor.
     * 
     * @return Raw HyUIML template string
     * @throws IllegalStateException if template supplier not set
     */
    public String getTemplate() {
        if (templateSupplier == null) {
            throw new IllegalStateException("Template supplier not set. Call setTemplateSupplier() first.");
        }
        return templateSupplier.get();
    }
    
    /**
     * Check if a template supplier has been set.
     * 
     * @return true if template supplier is configured
     */
    public boolean hasTemplateSupplier() {
        return templateSupplier != null;
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
     * @return Unmodifiable list of categories
     */
    public List<QuestCategory> getCategories() {
        return List.copyOf(categories);
    }
    
    /**
     * Get the selected quest for detail panel.
     * 
     * @return Selected quest, or null if none selected
     */
    public QuestDetail getSelectedQuest() {
        return selectedQuest;
    }
    
    /**
     * Get the selected quest ID.
     * 
     * @return Quest ID, or null if none selected
     */
    public String getSelectedQuestId() {
        return selectedQuestId;
    }
}
