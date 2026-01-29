package com.argonathsystems.framework.ui;

/**
 * LOTR Theme Constants for Java code.
 * 
 * <p>Mirrors the CSS constants defined in lotr-theme.css for use in Java builders.
 * Use these constants when building UI components programmatically.
 * 
 * @author Argonath Systems
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LotrTheme {
    
    private LotrTheme() {
        // Utility class
    }
    
    // === Color Palette ===
    
    public static final String COLOR_PARCHMENT = "#f4e8d0";
    public static final String COLOR_GOLD = "#d4a017";
    public static final String COLOR_DARK_BROWN = "#2d241e";
    public static final String COLOR_BROWN = "#6b5d4f";
    public static final String COLOR_LIGHT_BROWN = "#a89885";
    public static final String COLOR_INK = "#2d241e";
    
    // Functional Colors
    public static final String COLOR_HEALTH = "#dc143c";
    public static final String COLOR_MANA = "#4169e1";
    public static final String COLOR_ENERGY = "#ffa500";
    public static final String COLOR_XP = "#90ee90";
    public static final String COLOR_WARNING = "#ff6347";
    public static final String COLOR_SUCCESS = "#32cd32";
    
    // Quest Type Colors
    public static final String COLOR_QUEST_MAIN = "#ffd700";
    public static final String COLOR_QUEST_SIDE = "#ffffff";
    public static final String COLOR_QUEST_DAILY = "#87ceeb";
    public static final String COLOR_QUEST_WEEKLY = "#9370db";
    public static final String COLOR_QUEST_GUILD = "#ff8c00";
    public static final String COLOR_QUEST_TIMED = "#ff4500";
    public static final String COLOR_QUEST_EVENT = "#da70d6";
    
    // === Typography ===
    
    public static final String FONT_PRIMARY = "Aniron";
    public static final String FONT_SECONDARY = "Ringbearer";
    public static final String FONT_TENGWAR = "Tengwar Annatar";
    
    public static final int FONT_SIZE_HUGE = 24;
    public static final int FONT_SIZE_LARGE = 20;
    public static final int FONT_SIZE_MEDIUM = 16;
    public static final int FONT_SIZE_NORMAL = 14;
    public static final int FONT_SIZE_SMALL = 12;
    public static final int FONT_SIZE_TINY = 10;
    
    // === Spacing ===
    
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 20;
    public static final int SPACING_XXL = 24;
    
    // === Component Sizes ===
    
    public static final int ACTION_SLOT_SIZE = 46;
    public static final int ACTION_BAR_GAP = 6;
    
    public static final int ITEM_SLOT_SMALL = 32;
    public static final int ITEM_SLOT_MEDIUM = 40;
    public static final int ITEM_SLOT_LARGE = 48;
    
    public static final int PORTRAIT_SMALL = 36;
    public static final int PORTRAIT_MEDIUM = 60;
    public static final int PORTRAIT_LARGE = 80;
    public static final int PORTRAIT_HUGE = 128;
    
    public static final int BUFF_ICON_SIZE = 20;
    
    public static final int RESOURCE_BAR_HEIGHT = 20;
    public static final int FRAME_BAR_HEIGHT = 16;
    public static final int PARTY_BAR_HEIGHT = 12;
    
    // === Quest Types ===
    
    public enum QuestType {
        MAIN("Main Quest", COLOR_QUEST_MAIN),
        SIDE("Side Quest", COLOR_QUEST_SIDE),
        DAILY("Daily Quest", COLOR_QUEST_DAILY),
        WEEKLY("Weekly Quest", COLOR_QUEST_WEEKLY),
        GUILD("Guild Quest", COLOR_QUEST_GUILD),
        TIMED("Timed Event", COLOR_QUEST_TIMED),
        EVENT("Special Event", COLOR_QUEST_EVENT);
        
        private final String displayName;
        private final String color;
        
        QuestType(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
    }
}
