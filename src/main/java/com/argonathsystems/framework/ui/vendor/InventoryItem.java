package com.argonathsystems.framework.ui.vendor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a player inventory item available for selling to a vendor.
 * 
 * <p>This is a data model class with no platform dependencies.
 * Rendering is handled by the adapter layer.
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
public class InventoryItem {
    
    private final String itemId;
    private final String name;
    private final String iconUrl;
    private final int quantity;
    private final int sellPrice;
    private final String rarity;
    private final boolean sellable;
    
    /**
     * Create a new inventory item.
     * 
     * @param itemId unique item ID
     * @param name display name
     * @param iconUrl icon resource path
     * @param quantity stack size
     * @param sellPrice sell value per unit
     * @param rarity item rarity
     * @param sellable whether the item can be sold
     */
    public InventoryItem(String itemId, String name, String iconUrl, int quantity, 
                         int sellPrice, String rarity, boolean sellable) {
        this.itemId = itemId;
        this.name = name;
        this.iconUrl = iconUrl;
        this.quantity = quantity;
        this.sellPrice = sellPrice;
        this.rarity = rarity;
        this.sellable = sellable;
    }
    
    /**
     * Get the unique item ID.
     * 
     * @return Item ID
     */
    public String getItemId() {
        return itemId;
    }
    
    /**
     * Get the display name.
     * 
     * @return Item name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the icon URL.
     * 
     * @return Icon URL
     */
    public String getIconUrl() {
        return iconUrl;
    }
    
    /**
     * Get the stack quantity.
     * 
     * @return Stack size
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Get the sell price per unit.
     * 
     * @return Sell price
     */
    public int getSellPrice() {
        return sellPrice;
    }
    
    /**
     * Get the total sell value.
     * 
     * @return Total value (quantity * sellPrice)
     */
    public int getTotalValue() {
        return quantity * sellPrice;
    }
    
    /**
     * Get the item rarity.
     * 
     * @return Rarity tier
     */
    public String getRarity() {
        return rarity;
    }
    
    /**
     * Check if the item can be sold.
     * 
     * @return true if sellable
     */
    public boolean isSellable() {
        return sellable;
    }
    
    /**
     * Convert to a map for template processing.
     * 
     * @return Map of template variables
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("name", name);
        map.put("iconUrl", iconUrl);
        map.put("quantity", quantity);
        map.put("sellPrice", sellPrice);
        map.put("totalValue", getTotalValue());
        map.put("rarity", rarity);
        map.put("sellable", sellable);
        return map;
    }
}
