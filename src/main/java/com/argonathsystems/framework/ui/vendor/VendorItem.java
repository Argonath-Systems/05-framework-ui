package com.argonathsystems.framework.ui.vendor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an item available for purchase or buyback at a vendor.
 * 
 * <p>This is a data model class with no platform dependencies.
 * Rendering is handled by the adapter layer.
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @since 1.1.0
 */
public class VendorItem {
    
    private final String itemId;
    private final String name;
    private final String description;
    private final String iconUrl;
    private final int buyPrice;
    private final int sellPrice;
    private final int stock;
    private final String rarity;
    private final boolean available;
    
    private VendorItem(Builder builder) {
        this.itemId = builder.itemId;
        this.name = builder.name;
        this.description = builder.description;
        this.iconUrl = builder.iconUrl;
        this.buyPrice = builder.buyPrice;
        this.sellPrice = builder.sellPrice;
        this.stock = builder.stock;
        this.rarity = builder.rarity;
        this.available = builder.available;
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
     * Get the item description.
     * 
     * @return Description text
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the icon URL or resource path.
     * 
     * @return Icon URL
     */
    public String getIconUrl() {
        return iconUrl;
    }
    
    /**
     * Get the buy price in gold.
     * 
     * @return Buy price
     */
    public int getBuyPrice() {
        return buyPrice;
    }
    
    /**
     * Get the sell price in gold.
     * 
     * @return Sell price
     */
    public int getSellPrice() {
        return sellPrice;
    }
    
    /**
     * Get the current stock quantity.
     * 
     * <p>-1 indicates unlimited stock.
     * 
     * @return Stock count
     */
    public int getStock() {
        return stock;
    }
    
    /**
     * Get the item rarity.
     * 
     * @return Rarity (common, uncommon, rare, epic, legendary)
     */
    public String getRarity() {
        return rarity;
    }
    
    /**
     * Check if the item is available for purchase.
     * 
     * @return true if purchasable
     */
    public boolean isAvailable() {
        return available;
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
        map.put("description", description);
        map.put("iconUrl", iconUrl);
        map.put("buyPrice", buyPrice);
        map.put("sellPrice", sellPrice);
        map.put("stock", stock);
        map.put("hasUnlimitedStock", stock == -1);
        map.put("rarity", rarity);
        map.put("available", available);
        return map;
    }
    
    /**
     * Create a new builder.
     * 
     * @param itemId the unique item ID
     * @param name the display name
     * @return a new builder instance
     */
    public static Builder builder(String itemId, String name) {
        return new Builder(itemId, name);
    }
    
    /**
     * Builder for VendorItem.
     */
    public static class Builder {
        private final String itemId;
        private final String name;
        private String description = "";
        private String iconUrl = "item/default.png";
        private int buyPrice = 0;
        private int sellPrice = 0;
        private int stock = -1; // -1 = unlimited
        private String rarity = "common";
        private boolean available = true;
        
        private Builder(String itemId, String name) {
            this.itemId = itemId;
            this.name = name;
        }
        
        /**
         * Set the item description.
         * 
         * @param description description text
         * @return this builder
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        /**
         * Set the icon URL.
         * 
         * @param iconUrl icon resource path
         * @return this builder
         */
        public Builder withIcon(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }
        
        /**
         * Set the buy price.
         * 
         * @param price price in gold
         * @return this builder
         */
        public Builder withBuyPrice(int price) {
            this.buyPrice = price;
            return this;
        }
        
        /**
         * Set the sell price.
         * 
         * @param price price in gold
         * @return this builder
         */
        public Builder withSellPrice(int price) {
            this.sellPrice = price;
            return this;
        }
        
        /**
         * Set the stock quantity.
         * 
         * @param stock stock count (-1 for unlimited)
         * @return this builder
         */
        public Builder withStock(int stock) {
            this.stock = stock;
            return this;
        }
        
        /**
         * Set the item rarity.
         * 
         * @param rarity rarity tier
         * @return this builder
         */
        public Builder withRarity(String rarity) {
            this.rarity = rarity;
            return this;
        }
        
        /**
         * Set whether the item is available.
         * 
         * @param available true if purchasable
         * @return this builder
         */
        public Builder withAvailable(boolean available) {
            this.available = available;
            return this;
        }
        
        /**
         * Build the VendorItem.
         * 
         * @return the built VendorItem
         */
        public VendorItem build() {
            return new VendorItem(this);
        }
    }
}
