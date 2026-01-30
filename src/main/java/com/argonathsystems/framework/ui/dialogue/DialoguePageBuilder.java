package com.argonathsystems.framework.ui.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Platform-agnostic data builder for NPC Dialogue HUD using HyUIML.
 * 
 * <p>This class builds the data model for NPC dialogue UIs. It does NOT process
 * templates or render UIs directly - that is handled by the adapter layer using
 * HyUI's {@code TemplateProcessor} and {@code PageBuilder}.
 * 
 * <h2>Architecture</h2>
 * <pre>{@code
 *   DialoguePageBuilder (this class)
 *          ↓ builds data
 *   Map<String, Object> templateVariables
 *          ↓ passed to
 *   02-adapter-hytale/DialoguePageAdapter
 *          ↓ uses HyUI
 *   TemplateProcessor → PageBuilder → Player UI
 * }</pre>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // In framework layer (this module):
 * DialoguePageBuilder builder = new DialoguePageBuilder()
 *     .setNpcName("Gandalf the Grey")
 *     .setNpcAvatarId("Gandalf")
 *     .setDialogueText("A wizard is never late...")
 *     .addChoice(1, "Tell me about the Ring", "quest-ring", true, "Quest")
 *     .addChoice(2, "Goodbye", "farewell", true);
 * 
 * Map<String, Object> variables = builder.buildTemplateVariables();
 * 
 * // In adapter layer (02-adapter-hytale):
 * TemplateProcessor processor = new TemplateProcessor();
 * variables.forEach((k, v) -> processor.setVariable(k, v));
 * String html = processor.process(templateContent);
 * PageBuilder.pageForPlayer(playerRef).fromHtml(html).open(store);
 * }</pre>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>NPC name and avatar configuration</li>
 *   <li>Dialogue text with typewriter animation support</li>
 *   <li>Numbered choices with keyboard shortcuts (1-9)</li>
 *   <li>Quest offer mode with objectives and rewards</li>
 *   <li>Hot reload support via template supplier</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @see com.argonathsystems.framework.ui.dialogue.DialogueChoice
 * @see com.argonathsystems.framework.ui.dialogue.QuestOfferData
 * @since 1.0.0
 */
public class DialoguePageBuilder {
    
    private String npcName;
    private String npcAvatarId;
    private String dialogueText;
    private final List<DialogueChoice> choices;
    private boolean showQuestOffer;
    private QuestOfferData questOffer;
    
    // Template supplier (for hot reload support)
    private Supplier<String> templateSupplier;
    
    /**
     * Create a new dialogue page builder.
     */
    public DialoguePageBuilder() {
        this.choices = new ArrayList<>();
        this.showQuestOffer = false;
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
    public DialoguePageBuilder setTemplateSupplier(Supplier<String> templateSupplier) {
        this.templateSupplier = templateSupplier;
        return this;
    }
    
    /**
     * Set the NPC name displayed in the dialogue header.
     * 
     * @param npcName NPC display name
     * @return this builder
     */
    public DialoguePageBuilder setNpcName(String npcName) {
        this.npcName = npcName;
        return this;
    }
    
    /**
     * Set the NPC avatar ID for Hyvatar portrait rendering.
     * 
     * @param npcAvatarId Hyvatar username or NPC identifier
     * @return this builder
     */
    public DialoguePageBuilder setNpcAvatarId(String npcAvatarId) {
        this.npcAvatarId = npcAvatarId;
        return this;
    }
    
    /**
     * Set the current dialogue text.
     * 
     * @param dialogueText NPC speech text
     * @return this builder
     */
    public DialoguePageBuilder setDialogueText(String dialogueText) {
        this.dialogueText = dialogueText;
        return this;
    }
    
    /**
     * Add a dialogue choice option.
     * 
     * @param index Choice number (1-9 for keyboard shortcuts)
     * @param text Choice text displayed to player
     * @param choiceId Unique choice identifier for event handling
     * @param available Whether choice is selectable
     * @return this builder
     */
    public DialoguePageBuilder addChoice(int index, String text, String choiceId, boolean available) {
        this.choices.add(new DialogueChoice(index, text, choiceId, available, null));
        return this;
    }
    
    /**
     * Add a dialogue choice with a type label.
     * 
     * @param index Choice number (1-9 for keyboard shortcuts)
     * @param text Choice text displayed to player
     * @param choiceId Unique choice identifier for event handling
     * @param available Whether choice is selectable
     * @param type Choice type label (e.g., "Quest", "Lore", "Shop")
     * @return this builder
     */
    public DialoguePageBuilder addChoice(int index, String text, String choiceId, boolean available, String type) {
        this.choices.add(new DialogueChoice(index, text, choiceId, available, type));
        return this;
    }
    
    /**
     * Enable quest offer mode with quest details.
     * 
     * @param questOffer Quest offer data containing objectives and rewards
     * @return this builder
     */
    public DialoguePageBuilder setQuestOffer(QuestOfferData questOffer) {
        this.questOffer = questOffer;
        this.showQuestOffer = true;
        return this;
    }
    
    /**
     * Disable quest offer mode (return to normal dialogue).
     * 
     * @return this builder
     */
    public DialoguePageBuilder clearQuestOffer() {
        this.showQuestOffer = false;
        this.questOffer = null;
        return this;
    }
    
    /**
     * Clear all dialogue choices.
     * 
     * @return this builder
     */
    public DialoguePageBuilder clearChoices() {
        this.choices.clear();
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
     *   "npc": { "name": "...", "avatarId": "..." },
     *   "currentText": "...",
     *   "showQuestOffer": true/false,
     *   "quest": { ... },  // if showQuestOffer
     *   "choices": [ { "id": "...", "index": 1, "text": "...", "available": true, "type": "..." }, ... ]
     * }
     * }</pre>
     * 
     * @return Map of template variables for TemplateProcessor
     */
    public Map<String, Object> buildTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        // NPC data
        Map<String, String> npcData = new HashMap<>();
        npcData.put("name", npcName != null ? npcName : "Unknown NPC");
        npcData.put("avatarId", npcAvatarId != null ? npcAvatarId : "default");
        variables.put("npc", npcData);
        
        // Dialogue text
        variables.put("currentText", dialogueText != null ? dialogueText : "...");
        
        // Quest offer mode
        variables.put("showQuestOffer", showQuestOffer);
        if (showQuestOffer && questOffer != null) {
            variables.put("quest", questOffer.toMap());
        }
        
        // Dialogue choices
        List<Map<String, Object>> choicesList = new ArrayList<>();
        for (DialogueChoice choice : choices) {
            choicesList.add(choice.toMap());
        }
        variables.put("choices", choicesList);
        
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
     * Get the current NPC name.
     * 
     * @return NPC name, or null if not set
     */
    public String getNpcName() {
        return npcName;
    }
    
    /**
     * Get the NPC avatar ID.
     * 
     * @return Avatar ID, or null if not set
     */
    public String getNpcAvatarId() {
        return npcAvatarId;
    }
    
    /**
     * Get the current dialogue text.
     * 
     * @return Dialogue text, or null if not set
     */
    public String getDialogueText() {
        return dialogueText;
    }
    
    /**
     * Get the list of dialogue choices.
     * 
     * @return Unmodifiable list of choices
     */
    public List<DialogueChoice> getChoices() {
        return List.copyOf(choices);
    }
    
    /**
     * Check if quest offer mode is active.
     * 
     * @return true if showing quest offer
     */
    public boolean isShowingQuestOffer() {
        return showQuestOffer;
    }
    
    /**
     * Get the quest offer data if in quest offer mode.
     * 
     * @return Quest offer data, or null if not in quest offer mode
     */
    public QuestOfferData getQuestOffer() {
        return questOffer;
    }
}
