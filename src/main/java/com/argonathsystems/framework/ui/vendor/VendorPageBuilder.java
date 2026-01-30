package com.argonathsystems.framework.ui.vendor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Platform-agnostic data builder for NPC Vendor Page using HyUIML.
 * 
 * <p>This class builds the data model for vendor shop UIs. It does NOT process
 * templates or render UIs directly - that is handled by the adapter layer using
 * HyUI's {@code TemplateProcessor} and {@code PageBuilder}.
 * 
 * <h2>Architecture</h2>
 * <pre>{@code
 *   VendorPageBuilder (this class)
 *          ↓ builds data
 *   Map<String, Object> templateVariables
 *          ↓ passed to
 *   02-adapter-hytale/VendorPageAdapter
 *          ↓ uses HyUI
 *   TemplateProcessor → PageBuilder → Player UI
 * }</pre>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Buy/Sell/Buyback tabs</li>
 *   <li>Item grid with icons, names, and prices</li>
 *   <li>Transaction panel for quantity selection</li>
 *   <li>Player gold display</li>
 *   <li>Hot reload support via template supplier</li>
 * </ul>
 * 
 * @author Argonath Systems
 * @version 1.1.0
 * @see VendorItem
 * @see InventoryItem
 * @since 1.1.0
 */
public class VendorPageBuilder {
    
    private String vendorName = "Merchant";
    private String vendorId;
    private String activeTab = "buy"; // "buy", "sell", "buyback"
    private int playerGold = 0;
    private final List<VendorItem> buyItems;
    private final List<InventoryItem> playerInventory;
    private final List<VendorItem> buybackItems;
    private String selectedItemId;
    private int selectedQuantity = 1;
    private Supplier<String> templateSupplier;
    
    /**
     * Create a new vendor page builder.
     */
    public VendorPageBuilder() {
        this.buyItems = new ArrayList<>();
        this.playerInventory = new ArrayList<>();
        this.buybackItems = new ArrayList<>();
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
    public VendorPageBuilder setTemplateSupplier(Supplier<String> templateSupplier) {
        this.templateSupplier = templateSupplier;
        return this;
    }
    
    /**
     * Set the vendor's display name.
     * 
     * @param vendorName NPC vendor name
     * @return this builder
     */
    public VendorPageBuilder setVendorName(String vendorName) {
        this.vendorName = vendorName;
        return this;
    }
    
    /**
     * Set the vendor's unique ID.
     * 
     * @param vendorId Vendor entity ID
     * @return this builder
     */
    public VendorPageBuilder setVendorId(String vendorId) {
        this.vendorId = vendorId;
        return this;
    }
    
    /**
     * Set the active tab.
     * 
     * @param tab Tab name: "buy", "sell", or "buyback"
     * @return this builder
     */
    public VendorPageBuilder setActiveTab(String tab) {
        this.activeTab = tab;
        return this;
    }
    
    /**
     * Set the player's current gold amount.
     * 
     * @param gold Gold amount
     * @return this builder
     */
    public VendorPageBuilder setPlayerGold(int gold) {
        this.playerGold = gold;
        return this;
    }
    
    /**
     * Add an item to the buy tab.
     * 
     * @param item Vendor item for sale
     * @return this builder
     */
    public VendorPageBuilder addBuyItem(VendorItem item) {
        this.buyItems.add(item);
        return this;
    }
    
    /**
     * Add a player inventory item for the sell tab.
     * 
     * @param item Player inventory item
     * @return this builder
     */
    public VendorPageBuilder addInventoryItem(InventoryItem item) {
        this.playerInventory.add(item);
        return this;
    }
    
    /**
     * Add an item to the buyback tab.
     * 
     * @param item Previously sold item
     * @return this builder
     */
    public VendorPageBuilder addBuybackItem(VendorItem item) {
        this.buybackItems.add(item);
        return this;
    }
    
    /**
     * Set the currently selected item.
     * 
     * @param itemId Selected item ID
     * @return this builder
     */
    public VendorPageBuilder setSelectedItem(String itemId) {
        this.selectedItemId = itemId;
        return this;
    }
    
    /**
     * Set the transaction quantity.
     * 
     * @param quantity Quantity to buy/sell
     * @return this builder
     */
    public VendorPageBuilder setSelectedQuantity(int quantity) {
        this.selectedQuantity = Math.max(1, quantity);
        return this;
    }
    
    /**
     * Clear all items from all tabs.
     * 
     * @return this builder
     */
    public VendorPageBuilder clearItems() {
        this.buyItems.clear();
        this.playerInventory.clear();
        this.buybackItems.clear();
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
     *   "vendorName": "...",
     *   "vendorId": "...",
     *   "activeTab": "buy",
     *   "isBuyTab": true,
     *   "isSellTab": false,
     *   "isBuybackTab": false,
     *   "playerGold": 1500,
     *   "items": [ {...} ],
     *   "playerInventory": [ {...} ],
     *   "buybackItems": [ {...} ],
     *   "selectedItemId": "...",
     *   "hasSelectedItem": true/false,
     *   "selectedQuantity": 1,
     *   "transactionTotal": 100
     * }
     * }</pre>
     * 
     * @return Map of template variables for TemplateProcessor
     */
    public Map<String, Object> buildTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();
        
        // Vendor info
        variables.put("vendorName", vendorName);
        variables.put("vendorId", vendorId != null ? vendorId : "unknown");
        
        // Tab state
        variables.put("activeTab", activeTab);
        variables.put("isBuyTab", "buy".equals(activeTab));
        variables.put("isSellTab", "sell".equals(activeTab));
        variables.put("isBuybackTab", "buyback".equals(activeTab));
        
        // Player currency
        variables.put("playerGold", playerGold);
        
        // Buy items
        List<Map<String, Object>> buyItemsList = new ArrayList<>();
        for (VendorItem item : buyItems) {
            buyItemsList.add(item.toMap());
        }
        variables.put("items", buyItemsList);
        
        // Player inventory (for sell tab)
        List<Map<String, Object>> inventoryList = new ArrayList<>();
        for (InventoryItem item : playerInventory) {
            inventoryList.add(item.toMap());
        }
        variables.put("playerInventory", inventoryList);
        
        // Buyback items
        List<Map<String, Object>> buybackList = new ArrayList<>();
        for (VendorItem item : buybackItems) {
            buybackList.add(item.toMap());
        }
        variables.put("buybackItems", buybackList);
        
        // Selection state
        variables.put("hasSelectedItem", selectedItemId != null);
        variables.put("selectedItemId", selectedItemId != null ? selectedItemId : "");
        variables.put("selectedQuantity", selectedQuantity);
        
        // Calculate transaction total based on active tab
        int transactionTotal = calculateTransactionTotal();
        variables.put("transactionTotal", transactionTotal);
        variables.put("canAfford", transactionTotal <= playerGold || !"buy".equals(activeTab));
        
        return variables;
    }
    
    /**
     * Calculate the total cost/value of the current transaction.
     */
    private int calculateTransactionTotal() {
        if (selectedItemId == null) {
            return 0;
        }
        
        if ("buy".equals(activeTab)) {
            return buyItems.stream()
                .filter(item -> item.getItemId().equals(selectedItemId))
                .findFirst()
                .map(item -> item.getBuyPrice() * selectedQuantity)
                .orElse(0);
        } else if ("sell".equals(activeTab)) {
            return playerInventory.stream()
                .filter(item -> item.getItemId().equals(selectedItemId))
                .findFirst()
                .map(item -> item.getSellPrice() * selectedQuantity)
                .orElse(0);
        } else if ("buyback".equals(activeTab)) {
            return buybackItems.stream()
                .filter(item -> item.getItemId().equals(selectedItemId))
                .findFirst()
                .map(item -> item.getBuyPrice() * selectedQuantity)
                .orElse(0);
        }
        
        return 0;
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
    
    // Getters
    
    public String getVendorName() {
        return vendorName;
    }
    
    public String getVendorId() {
        return vendorId;
    }
    
    public String getActiveTab() {
        return activeTab;
    }
    
    public int getPlayerGold() {
        return playerGold;
    }
    
    public List<VendorItem> getBuyItems() {
        return List.copyOf(buyItems);
    }
    
    public List<InventoryItem> getPlayerInventory() {
        return List.copyOf(playerInventory);
    }
    
    public List<VendorItem> getBuybackItems() {
        return List.copyOf(buybackItems);
    }
    
    public String getSelectedItemId() {
        return selectedItemId;
    }
    
    public int getSelectedQuantity() {
        return selectedQuantity;
    }
}
