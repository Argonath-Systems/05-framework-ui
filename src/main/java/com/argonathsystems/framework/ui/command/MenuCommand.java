package com.argonathsystems.framework.ui.command;

import com.argonathsystems.framework.command.Arguments;
import com.argonathsystems.framework.command.CommandRegistry;
import com.argonathsystems.framework.command.CommandSpec;
import com.argonathsystems.framework.ui.UnifiedUIManager;
import com.argonathsystems.framework.ui.menu.MainMenuManager;
import com.argonathsystems.framework.ui.menu.MenuTab;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command handler for main menu operations.
 * 
 * <p>Provides commands for opening the main menu and specific tabs:
 * <ul>
 *   <li>{@code /menu} - Open the main menu</li>
 *   <li>{@code /menu <tab>} - Open to a specific tab (e.g., /menu guild)</li>
 *   <li>{@code /menu close} - Close the menu</li>
 *   <li>{@code /menu list} - List available tabs</li>
 * </ul>
 * 
 * <p>Shortcut commands are also registered:
 * <ul>
 *   <li>{@code /character} - Open character tab</li>
 *   <li>{@code /inventory} - Open inventory tab</li>
 *   <li>{@code /quests} - Open quests tab</li>
 *   <li>{@code /guild} - Open guild tab</li>
 *   <li>{@code /social} - Open social tab</li>
 *   <li>{@code /map} - Open map tab</li>
 *   <li>{@code /marketplace} - Open marketplace tab</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MenuCommand {
    
    private MenuCommand() {}
    
    /**
     * Registers all menu commands with the command registry.
     * 
     * @param registry The command registry to register with
     */
    public static void register(CommandRegistry registry) {
        // Main /menu command
        registry.register(CommandSpec.builder("menu")
            .description("Open the main menu or a specific tab")
            .alias("m")
            .alias("mainmenu")
            .addArgument("tab", Arguments.word())
            .executor(context -> {
                if (!context.getSender().isPlayer()) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return;
                }
                
                UUID playerId = context.getSender().getUniqueId();
                String tabArg = context.getArgument("tab", String.class).orElse(null);
                
                if (tabArg == null || tabArg.isEmpty()) {
                    // No argument - open/toggle menu
                    MainMenuManager manager = MainMenuManager.getInstance();
                    if (manager.isMenuOpen(playerId)) {
                        manager.closeMenu(playerId);
                    } else {
                        manager.openMenu(playerId, null);
                    }
                    return;
                }
                
                // Handle special subcommands
                switch (tabArg.toLowerCase()) {
                    case "close" -> handleClose(context.getSender(), playerId);
                    case "list", "tabs", "help" -> showTabs(context.getSender());
                    default -> openTab(context.getSender(), playerId, tabArg);
                }
            })
            .build());
        
        // Register shortcut commands for common tabs
        registerShortcut(registry, "character", MenuTab.CHARACTER, "c", "char");
        registerShortcut(registry, "inventory", MenuTab.INVENTORY, "i", "inv");
        registerShortcut(registry, "skills", MenuTab.SKILLS, "k", "skill");
        registerShortcut(registry, "quests", MenuTab.QUESTS, "j", "quest", "q");
        registerShortcut(registry, "guild", MenuTab.GUILD, "g");
        registerShortcut(registry, "social", MenuTab.SOCIAL, "o", "friends");
        registerShortcut(registry, "party", MenuTab.PARTY, "p");
        registerShortcut(registry, "map", MenuTab.MAP);
        registerShortcut(registry, "marketplace", MenuTab.MARKETPLACE, "market", "b", "ah");
        registerShortcut(registry, "factions", MenuTab.FACTIONS, "f", "faction");
        registerShortcut(registry, "crafting", MenuTab.CRAFTING, "n", "craft");
        registerShortcut(registry, "mounts", MenuTab.MOUNTS, "h", "mount");
        registerShortcut(registry, "achievements", MenuTab.ACHIEVEMENTS, "y", "achieve");
        registerShortcut(registry, "settings", MenuTab.SETTINGS);
    }
    
    private static void registerShortcut(CommandRegistry registry, String name, MenuTab tab, String... aliases) {
        CommandSpec.Builder builder = CommandSpec.builder(name)
            .description("Open the " + tab.displayName() + " panel")
            .executor(context -> {
                if (!context.getSender().isPlayer()) {
                    context.getSender().sendMessage("§cThis command can only be used by players.");
                    return;
                }
                
                UUID playerId = context.getSender().getUniqueId();
                MainMenuManager.getInstance().openMenu(playerId, tab);
            });
        
        for (String alias : aliases) {
            builder.alias(alias);
        }
        
        registry.register(builder.build());
    }
    
    private static void handleClose(com.argonathsystems.framework.accessorapi.CommandSender sender, UUID playerId) {
        MainMenuManager manager = MainMenuManager.getInstance();
        if (manager.isMenuOpen(playerId)) {
            manager.closeMenu(playerId);
            sender.sendMessage("§7Menu closed.");
        } else {
            sender.sendMessage("§7No menu is open.");
        }
    }
    
    private static void openTab(com.argonathsystems.framework.accessorapi.CommandSender sender, UUID playerId, String tabName) {
        MenuTab tab = MenuTab.fromId(tabName.toLowerCase()).orElse(null);
        
        if (tab == null) {
            // Try to find by display name
            for (MenuTab t : MenuTab.values()) {
                if (t.displayName().equalsIgnoreCase(tabName)) {
                    tab = t;
                    break;
                }
            }
        }
        
        if (tab == null) {
            sender.sendMessage("§cUnknown tab: " + tabName);
            showTabs(sender);
            return;
        }
        
        MainMenuManager manager = MainMenuManager.getInstance();
        if (!manager.isTabEnabled(tab)) {
            sender.sendMessage("§cThe " + tab.displayName() + " tab is not available.");
            return;
        }
        
        manager.openMenu(playerId, tab);
    }
    
    private static void showTabs(com.argonathsystems.framework.accessorapi.CommandSender sender) {
        sender.sendMessage("§6Available Menu Tabs:");
        
        String tabList = Arrays.stream(MenuTab.values())
            .filter(t -> MainMenuManager.getInstance().isTabEnabled(t))
            .map(t -> {
                String hotkey = t.hotkey() != null ? " §7[" + t.hotkey() + "]" : "";
                return "§e" + t.id() + hotkey;
            })
            .collect(Collectors.joining("§7, "));
        
        sender.sendMessage(tabList);
        sender.sendMessage("§7Usage: §f/menu <tab>§7 or use shortcut commands like §f/guild§7, §f/quests§7, etc.");
    }
}
