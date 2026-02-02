/*
 * Copyright (c) 2024-2025 Argonath Systems. All rights reserved.
 * This file is part of the Argonath Framework UI module.
 */
package com.argonathsystems.framework.ui.menu.model;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a bag item slot in the inventory grid.
 * Used by inventory-equipment.hyuiml template.
 * 
 * @param slotIndex Slot index within the bag (0-based)
 * @param bagIndex Bag index (0 = backpack, 1-4 = bag slots)
 * @param itemId Item identifier if slot has an item
 * @param itemName Item display name
 * @param itemIcon Item icon path or emoji
 * @param stackCount Current stack count (1 for non-stackables)
 * @param maxStack Maximum stack size
 * @param quality Item quality tier
 * @param isEmpty Whether the slot is empty
 * @param isLocked Whether the slot is locked (bag too small)
 * @param isSoulbound Whether the item is soulbound
 * @param cooldownRemaining Cooldown remaining in seconds (for consumables)
 */
public record InventorySlotData(
    int slotIndex,
    int bagIndex,
    Optional<String> itemId,
    String itemName,
    String itemIcon,
    int stackCount,
    int maxStack,
    EquipmentSlotData.ItemQuality quality,
    boolean isEmpty,
    boolean isLocked,
    boolean isSoulbound,
    Optional<Integer> cooldownRemaining
) {
    
    /**
     * Creates an empty inventory slot.
     */
    public static InventorySlotData empty(int slotIndex, int bagIndex) {
        return new InventorySlotData(
            slotIndex,
            bagIndex,
            Optional.empty(),
            "",
            "",
            0,
            1,
            EquipmentSlotData.ItemQuality.COMMON,
            true,
            false,
            false,
            Optional.empty()
        );
    }
    
    /**
     * Creates a locked inventory slot.
     */
    public static InventorySlotData locked(int slotIndex, int bagIndex) {
        return new InventorySlotData(
            slotIndex,
            bagIndex,
            Optional.empty(),
            "",
            "",
            0,
            1,
            EquipmentSlotData.ItemQuality.COMMON,
            true,
            true,
            false,
            Optional.empty()
        );
    }
    
    /**
     * Creates a filled inventory slot.
     */
    public static InventorySlotData withItem(
        int slotIndex,
        int bagIndex,
        String itemId,
        String itemName,
        String itemIcon,
        int stackCount,
        int maxStack,
        EquipmentSlotData.ItemQuality quality
    ) {
        return new InventorySlotData(
            slotIndex,
            bagIndex,
            Optional.of(itemId),
            itemName,
            itemIcon,
            stackCount,
            maxStack,
            quality,
            false,
            false,
            false,
            Optional.empty()
        );
    }
    
    /**
     * Returns the unique slot identifier.
     */
    public String getSlotId() {
        return "bag" + bagIndex + "-slot" + slotIndex;
    }
    
    /**
     * Returns whether this item is stackable.
     */
    public boolean isStackable() {
        return maxStack > 1;
    }
    
    /**
     * Returns whether the stack is full.
     */
    public boolean isStackFull() {
        return stackCount >= maxStack;
    }
    
    /**
     * Returns whether the item is on cooldown.
     */
    public boolean isOnCooldown() {
        return cooldownRemaining.isPresent() && cooldownRemaining.get() > 0;
    }
    
    /**
     * Formats the cooldown for display.
     */
    public String formatCooldown() {
        if (cooldownRemaining.isEmpty()) {
            return "";
        }
        int cd = cooldownRemaining.get();
        if (cd < 60) {
            return cd + "s";
        }
        return (cd / 60) + "m";
    }
    
    /**
     * Converts to a map for template rendering.
     */
    public Map<String, Object> toTemplateMap() {
        return Map.ofEntries(
            Map.entry("slotId", getSlotId()),
            Map.entry("slotIndex", slotIndex),
            Map.entry("bagIndex", bagIndex),
            Map.entry("itemId", itemId.orElse("")),
            Map.entry("itemName", itemName),
            Map.entry("itemIcon", itemIcon),
            Map.entry("count", stackCount),
            Map.entry("maxStack", maxStack),
            Map.entry("qualityClass", quality.getCssClass()),
            Map.entry("qualityColor", quality.getColor()),
            Map.entry("isEmpty", isEmpty),
            Map.entry("isLocked", isLocked),
            Map.entry("isSoulbound", isSoulbound),
            Map.entry("isStackable", isStackable()),
            Map.entry("showCount", isStackable() && stackCount > 1),
            Map.entry("isOnCooldown", isOnCooldown()),
            Map.entry("cooldown", formatCooldown())
        );
    }
    
    /**
     * Returns this slot with updated stack count.
     */
    public InventorySlotData withStackCount(int newCount) {
        return new InventorySlotData(
            slotIndex, bagIndex, itemId, itemName, itemIcon,
            Math.max(0, Math.min(maxStack, newCount)), maxStack,
            quality, newCount <= 0, isLocked, isSoulbound, cooldownRemaining
        );
    }
    
    /**
     * Returns this slot with cooldown updated.
     */
    public InventorySlotData withCooldown(int seconds) {
        return new InventorySlotData(
            slotIndex, bagIndex, itemId, itemName, itemIcon,
            stackCount, maxStack, quality, isEmpty, isLocked, isSoulbound,
            seconds > 0 ? Optional.of(seconds) : Optional.empty()
        );
    }
    
    /**
     * Returns this slot emptied.
     */
    public InventorySlotData cleared() {
        return empty(slotIndex, bagIndex);
    }
}
