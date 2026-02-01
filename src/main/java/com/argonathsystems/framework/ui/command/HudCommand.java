package com.argonathsystems.framework.ui.command;

import com.argonathsystems.framework.accessorapi.command.CommandSender;
import com.argonathsystems.framework.command.Arguments;
import com.argonathsystems.framework.command.CommandRegistry;
import com.argonathsystems.framework.command.CommandSpec;
import com.argonathsystems.framework.ui.UnifiedUIManager;

import java.util.UUID;

/**
 * Command handler for HUD management.
 * 
 * <p>Provides commands for entering/exiting HUD edit mode and managing layouts:
 * <ul>
 *   <li>{@code /hud edit} - Toggle HUD edit mode</li>
 *   <li>{@code /hud reset} - Reset HUD layout to defaults</li>
 *   <li>{@code /hud save} - Force save current layout</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class HudCommand {
    
    private HudCommand() {}
    
    /**
     * Registers all HUD commands with the command registry.
     * 
     * @param registry The command registry to register with
     */
    public static void register(CommandRegistry registry) {
        // /hud edit - Toggle edit mode
        registry.register(CommandSpec.builder("hud")
            .description("Manage HUD layout and customization")
            .alias("hudeditor")
            .addArgument("subcommand", Arguments.word())
            .executor(context -> {
                if (!context.getSender().isPlayer()) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return;
                }
                
                UUID playerId = context.getSender().getPlayerId().orElse(null);
                if (playerId == null) {
                    context.getSender().sendMessage("§cCould not determine your player ID.");
                    return;
                }
                String subcommand = context.getArgument("subcommand", String.class)
                    .orElse("edit"); // Default to edit if no subcommand
                
                switch (subcommand.toLowerCase()) {
                    case "edit" -> handleEdit(context.getSender(), playerId);
                    case "reset" -> handleReset(context.getSender(), playerId);
                    case "save" -> handleSave(context.getSender(), playerId);
                    case "help" -> showHelp(context.getSender());
                    default -> {
                        context.getSender().sendMessage("§cUnknown subcommand: " + subcommand);
                        showHelp(context.getSender());
                    }
                }
            })
            .build());
    }
    
    private static void handleEdit(CommandSender sender, UUID playerId) {
        UnifiedUIManager manager = UnifiedUIManager.getInstance();
        
        if (manager.isInEditMode(playerId)) {
            manager.exitEditMode(playerId);
            sender.sendMessage("§aExited HUD edit mode.");
        } else {
            manager.enterEditMode(playerId);
            sender.sendMessage("§aEntered HUD edit mode. Drag elements to reposition them.");
            sender.sendMessage("§7Use §e/hud edit§7 or press §eF7§7 again to exit.");
        }
    }
    
    private static void handleReset(CommandSender sender, UUID playerId) {
        UnifiedUIManager manager = UnifiedUIManager.getInstance();
        manager.resetLayout(playerId);
        sender.sendMessage("§aHUD layout reset to defaults.");
    }
    
    private static void handleSave(CommandSender sender, UUID playerId) {
        UnifiedUIManager manager = UnifiedUIManager.getInstance();
        manager.forceSaveLayout(playerId);
        sender.sendMessage("§aHUD layout saved.");
    }
    
    private static void showHelp(CommandSender sender) {
        sender.sendMessage("§6=== HUD Commands ===");
        sender.sendMessage("§e/hud edit §7- Toggle HUD edit mode (or press §eF7§7)");
        sender.sendMessage("§e/hud reset §7- Reset HUD layout to defaults");
        sender.sendMessage("§e/hud save §7- Force save current layout");
        sender.sendMessage("§e/hud help §7- Show this help message");
    }
}
