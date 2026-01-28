package com.argonathsystems.framework.ui.command;

import com.argonathsystems.framework.accessorapi.CommandSender;
import com.argonathsystems.framework.command.Arguments;
import com.argonathsystems.framework.command.CommandRegistry;
import com.argonathsystems.framework.command.CommandSpec;
import com.argonathsystems.framework.ui.UnifiedUIManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Command handler for UI hot reload during development.
 * 
 * <p>Provides commands for reloading UI files without server restart:
 * <ul>
 *   <li>{@code /uireload [pageId]} - Reload specific page or all pages</li>
 *   <li>{@code /uireload all -r} - Reload all pages and refresh player UIs</li>
 * </ul>
 * 
 * <p>Requires permission: {@code argonath.admin.uireload}
 * 
 * <p>Only functional when UI hot reload is enabled via {@link UnifiedUIManager#initDevMode}.
 * 
 * @author Argonath Systems Team
 * @version 1.1.0
 * @since 1.1.0
 */
public final class UIReloadCommand {
    
    /** Permission required to use this command */
    public static final String PERMISSION = "argonath.admin.uireload";
    
    private UIReloadCommand() {}
    
    /**
     * Registers the UI reload command with the command registry.
     * 
     * @param registry The command registry to register with
     */
    public static void register(CommandRegistry registry) {
        registry.register(CommandSpec.builder("uireload")
            .description("Hot reload UI files from disk (development only)")
            .alias("uir")
            .addArgument("page", Arguments.word())  // Use word() since optionalWord() may not exist
            .executor(context -> {
                CommandSender sender = context.getSender();
                
                // Permission check
                if (!sender.hasPermission(PERMISSION)) {
                    sender.sendMessage("§c✗ You don't have permission to use this command.");
                    return;
                }
                
                UnifiedUIManager uiManager = UnifiedUIManager.getInstance();
                
                // Check if hot reload is enabled
                if (!uiManager.isHotReloadEnabled()) {
                    sender.sendMessage("§c⚠ UI hot reload is not enabled.");
                    sender.sendMessage("§7Enable it in argonath-framework.yml under development.ui.hot_reload");
                    return;
                }
                
                // Get page argument (default to "all" if not provided)
                String pageArg = context.getArgument("page", String.class).orElse("all");
                
                // Check for special commands
                if ("list".equalsIgnoreCase(pageArg)) {
                    handleList(sender, uiManager);
                    return;
                }
                
                // Parse refresh flag from page arg (e.g., "all -r" or just check if it ends with -r)
                boolean refreshPlayers = pageArg.endsWith("-r") || pageArg.equals("refresh");
                String pageId = pageArg.replace("-r", "").trim();
                if (pageId.isEmpty()) {
                    pageId = "all";
                }
                
                try {
                    int count = uiManager.reloadUI(pageId, refreshPlayers);
                    
                    if (count > 0) {
                        String suffix = refreshPlayers ? " and refreshed player UIs" : "";
                        sender.sendMessage("§a✓ Reloaded " + count + " UI file(s)" + suffix);
                    } else {
                        sender.sendMessage("§e⚠ No UI files found to reload for: " + pageId);
                    }
                } catch (Exception e) {
                    sender.sendMessage("§c✗ Error reloading UI: " + e.getMessage());
                }
            })
            .build());
    }
    
    private static void handleList(CommandSender sender, UnifiedUIManager uiManager) {
        Set<String> pageIds = uiManager.getRegisteredPageIds();
        
        sender.sendMessage("§6=== Registered UI Pages (" + pageIds.size() + ") ===");
        
        if (pageIds.isEmpty()) {
            sender.sendMessage("§7No UI files loaded. Check watch directory.");
        } else {
            for (String pageId : pageIds.stream().sorted().toList()) {
                sender.sendMessage("§7 • §f" + pageId);
            }
        }
        
        sender.sendMessage("§6================================");
        sender.sendMessage("§7Usage: /uireload <pageId|all> [-r to refresh players]");
    }
}
