/*
 * Copyright (c) 2024-2025 Argonath Systems. All rights reserved.
 * This file is part of the Argonath Framework UI module.
 */
package com.argonathsystems.framework.ui.menu.model;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a single equipment slot in the player's equipment panel.
 * Used by inventory-equipment.hyuiml template.
 * 
 * <p>Equipment slots are displayed in the 3x4 grid layout:
 * <ul>
 *   <li>Row 1: Head, Neck, Back</li>
 *   <li>Row 2: Chest, Hands, Ring1</li>
 *   <li>Row 3: Legs, Feet, Ring2</li>
 *   <li>Row 4: MainHand, OffHand, Belt</li>
 * </ul>
 * 
 * @param slotId Unique identifier for the slot (e.g., "head", "chest", "main-hand")
 * @param slotLabel Display name shown on hover (e.g., "Head", "Chest", "Main Hand")
 * @param itemName Name of the equipped item, empty if slot is empty
 * @param itemIcon Icon path or emoji for the item
 * @param itemLevel Item level of the equipped item, -1 if empty
 * @param itemQuality Quality tier (common, uncommon, rare, epic, legendary)
 * @param isEmpty Whether the slot is empty
 * @param enchantInfo Optional enchantment information
 * @param durabilityPercent Current durability as percentage (0-100)
 * @param isBroken Whether the item is broken (durability = 0)
 * @param additionalData Extra data for template rendering
 */
public record EquipmentSlotData(
    String slotId,
    String slotLabel,
    String itemName,
    String itemIcon,
    int itemLevel,
    ItemQuality itemQuality,
    boolean isEmpty,
    Optional<String> enchantInfo,
    int durabilityPercent,
    boolean isBroken,
    Map<String, Object> additionalData
) {
    
    /**
     * Item quality tiers with associated CSS classes.
     */
    public enum ItemQuality {
        COMMON("common", "#9d9d9d"),
        UNCOMMON("uncommon", "#1eff00"),
        RARE("rare", "#0070dd"),
        EPIC("epic", "#a335ee"),
        LEGENDARY("legendary", "#ff8000"),
        ARTIFACT("artifact", "#e6cc80");
        
        private final String cssClass;
        private final String color;
        
        ItemQuality(String cssClass, String color) {
            this.cssClass = cssClass;
            this.color = color;
        }
        
        public String getCssClass() {
            return cssClass;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * Creates an empty equipment slot.
     * 
     * @param slotId The slot identifier
     * @param slotLabel The display label
     * @return An empty equipment slot
     */
    public static EquipmentSlotData empty(String slotId, String slotLabel) {
        return new EquipmentSlotData(
            slotId,
            slotLabel,
            "",
            "",
            -1,
            ItemQuality.COMMON,
            true,
            Optional.empty(),
            100,
            false,
            Map.of()
        );
    }
    
    /**
     * Creates a populated equipment slot.
     * 
     * @param slotId The slot identifier
     * @param slotLabel The display label
     * @param itemName Name of the item
     * @param itemIcon Icon for the item
     * @param itemLevel Item level
     * @param quality Quality tier
     * @return A populated equipment slot
     */
    public static EquipmentSlotData withItem(
        String slotId,
        String slotLabel,
        String itemName,
        String itemIcon,
        int itemLevel,
        ItemQuality quality
    ) {
        return new EquipmentSlotData(
            slotId,
            slotLabel,
            itemName,
            itemIcon,
            itemLevel,
            quality,
            false,
            Optional.empty(),
            100,
            false,
            Map.of()
        );
    }
    
    /**
     * Returns the CSS class for this slot's quality.
     */
    public String getQualityCssClass() {
        return isEmpty ? "empty" : itemQuality.getCssClass();
    }
    
    /**
     * Returns whether this item needs repair (durability < 25%).
     */
    public boolean needsRepair() {
        return !isEmpty && durabilityPercent < 25;
    }
    
    /**
     * Converts to a map for template rendering.
     */
    public Map<String, Object> toTemplateMap() {
        return Map.ofEntries(
            Map.entry("slotId", slotId),
            Map.entry("slotLabel", slotLabel),
            Map.entry("itemName", itemName),
            Map.entry("itemIcon", itemIcon),
            Map.entry("itemLevel", itemLevel),
            Map.entry("qualityClass", getQualityCssClass()),
            Map.entry("isEmpty", isEmpty),
            Map.entry("enchant", enchantInfo.orElse("")),
            Map.entry("durability", durabilityPercent),
            Map.entry("isBroken", isBroken),
            Map.entry("needsRepair", needsRepair())
        );
    }
}
