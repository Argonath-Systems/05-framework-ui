package com.argonathsystems.framework.ui.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Template data container for NPC Dialogue HUD using HyUIML.
 * 
 * <p><b>⚠️ WARNING: INCOMPLETE IMPLEMENTATION - NOT A FUNCTIONAL HyUI BUILDER!</b>
 * <p>This class only generates template data structures. It does NOT:
 * <ul>
 *   <li>Process template variables (no TemplateProcessor/Handlebars integration)
 *   <li>Render UIs (no HyUI PageBuilder.fromHtml() calls)
 *   <li>Handle events (no addEventListener binding)
 *   <li>Interact with Hytale (no PlayerRef/Store usage)
 * </ul>
 * 
 * <p><b>Current Status:</b> ~10% complete - requires major refactoring.
 * <p><b>See:</b> {@code IMPLEMENTATION_TRACKING.md} for critical blockers and fix requirements.
 * 
 * <p><b>Intended Features</b> (NOT YET IMPLEMENTED):
 * <ul>
 *   <li>Branching dialogue trees with numbered choices ❌
 *   <li>Quest offer mode with objectives and rewards (template only) ⚠️
 *   <li>Keyboard shortcuts (1-9, ESC) ❌
 *   <li>Typewriter text animation ❌
 *   <li>LOTR theme styling (partial - many CSS properties unsupported) ⚠️
 * </ul>
 * 
 * <p><b>Platform Agnostic</b>: No hytale.* imports. Actual rendering requires adapter layer (not yet created).
 * 
 * <p><b>Current Usage</b> (generates unparsed template):
 * <pre>{@code
 * DialoguePageBuilder builder = new DialoguePageBuilder();
 * builder.setTemplateSupplier(() -> loadTemplate("npc-dialogue.hyuiml"))
 *        .setNpcName("Gandalf")
 *        .setDialogueText("...")
 *        .addChoice(1, "Tell me about the Ring", "quest", true);
 * 
 * String html = builder.generateHtml();
 * // ❌ Returns raw template with {{$variables}} NOT processed!
 * }</pre>
 * 
 * @author Argonath Systems
 * @version 1.0.0-SNAPSHOT (INCOMPLETE)
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
     * @param npcAvatarId Hyvatar username
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
     * @param choiceId Unique choice identifier
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
     * @param index Choice number
     * @param text Choice text
     * @param choiceId Choice identifier
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
     * @param questOffer Quest offer data
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
     * Generate the HyUIML HTML for this dialogue.
     * 
     * <p>Uses template processor to inject variables into the template.
     * 
     * @return HyUIML HTML string
     */
    public String generateHtml() {
        if (templateSupplier == null) {
            throw new IllegalStateException("Template supplier not set. Call setTemplateSupplier() first.");
        }
        
        String template = templateSupplier.get();
        
        // Build template variables
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
        
        // Process template (simple variable replacement for now)
        // In production, this would use HyUI's TemplateProcessor
        return processTemplate(template, variables);
    }
    
    /**
     * Simple template variable replacement.
     * 
     * <p>In production, this should be replaced with HyUI's TemplateProcessor
     * which supports {{#if}}, {{#each}}, and other advanced features.
     * 
     * @param template Template string
     * @param variables Variable map
     * @return Processed template
     */
    private String processTemplate(String template, Map<String, Object> variables) {
        String result = template;
        
        // Replace simple variables like {{$npc.name}}
        result = result.replace("{{$npc.name}}", (String) ((Map<?, ?>) variables.get("npc")).get("name"));
        result = result.replace("{{$npc.avatarId}}", (String) ((Map<?, ?>) variables.get("npc")).get("avatarId"));
        result = result.replace("{{$currentText}}", (String) variables.get("currentText"));
        
        // Handle {{#if showQuestOffer}} blocks
        boolean showQuest = (Boolean) variables.get("showQuestOffer");
        if (showQuest) {
            // Keep quest offer section, remove dialogue choices section
            result = result.replaceAll("(?s)\\{\\{#if showQuestOffer\\}\\}(.+?)\\{\\{else\\}\\}.+?\\{\\{/if\\}\\}", "$1");
        } else {
            // Keep dialogue choices section, remove quest offer section
            result = result.replaceAll("(?s)\\{\\{#if showQuestOffer\\}\\}.+?\\{\\{else\\}\\}(.+?)\\{\\{/if\\}\\}", "$1");
        }
        
        // Handle {{#each choices}} blocks
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choicesList = (List<Map<String, Object>>) variables.get("choices");
        StringBuilder choicesHtml = new StringBuilder();
        for (Map<String, Object> choice : choicesList) {
            String choiceTemplate = extractEachBlock(result, "choices");
            choicesHtml.append(processChoiceTemplate(choiceTemplate, choice));
        }
        result = result.replaceAll("(?s)\\{\\{#each choices\\}\\}.+?\\{\\{/each\\}\\}", choicesHtml.toString());
        
        return result;
    }
    
    private String extractEachBlock(String template, String blockName) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "\\{\\{#each " + blockName + "\\}\\}(.+?)\\{\\{/each\\}\\}", 
            java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    private String processChoiceTemplate(String template, Map<String, Object> choice) {
        String result = template;
        result = result.replace("{{$id}}", (String) choice.get("id"));
        result = result.replace("{{$index}}", String.valueOf(choice.get("index")));
        result = result.replace("{{$text}}", (String) choice.get("text"));
        
        // Handle {{#if !available}}
        boolean available = (Boolean) choice.get("available");
        if (!available) {
            result = result.replace("{{#if !available}}", "");
            result = result.replace("{{/if}}", "");
        } else {
            result = result.replaceAll("\\{\\{#if !available\\}\\}.+?\\{\\{/if\\}\\}", "");
        }
        
        // Handle {{#if type}}
        String type = (String) choice.get("type");
        if (type != null && !type.isEmpty()) {
            result = result.replace("{{#if type}}", "");
            result = result.replace("{{$type}}", type);
            result = result.replace("{{/if}}", "");
        } else {
            result = result.replaceAll("\\{\\{#if type\\}\\}.+?\\{\\{/if\\}\\}", "");
        }
        
        return result;
    }
    
    /**
     * Get the current NPC name.
     * 
     * @return NPC name
     */
    public String getNpcName() {
        return npcName;
    }
    
    /**
     * Get the current dialogue text.
     * 
     * @return Dialogue text
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
}
