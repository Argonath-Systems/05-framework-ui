package com.argonathsystems.framework.ui.vendor;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VendorPageBuilder}.
 * 
 * @author Argonath Systems
 * @since 1.2.0
 */
@DisplayName("VendorPageBuilder Tests")
class VendorPageBuilderTest {
    
    private VendorPageBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new VendorPageBuilder();
    }
    
    // ========== Vendor Info Tests ==========
    
    @Test
    @DisplayName("Should have default vendor name")
    void shouldHaveDefaultVendorName() {
        assertEquals("Merchant", builder.getVendorName());
    }
    
    @Test
    @DisplayName("Should set vendor name")
    void shouldSetVendorName() {
        builder.setVendorName("Blacksmith");
        assertEquals("Blacksmith", builder.getVendorName());
    }
    
    @Test
    @DisplayName("Should set vendor ID")
    void shouldSetVendorId() {
        builder.setVendorId("vendor_123");
        assertEquals("vendor_123", builder.getVendorId());
    }
    
    // ========== Tab Tests ==========
    
    @Test
    @DisplayName("Should have buy tab as default")
    void shouldHaveBuyTabAsDefault() {
        assertEquals("buy", builder.getActiveTab());
    }
    
    @Test
    @DisplayName("Should switch to sell tab")
    void shouldSwitchToSellTab() {
        builder.setActiveTab("sell");
        assertEquals("sell", builder.getActiveTab());
    }
    
    @Test
    @DisplayName("Should switch to buyback tab")
    void shouldSwitchToBuybackTab() {
        builder.setActiveTab("buyback");
        assertEquals("buyback", builder.getActiveTab());
    }
    
    // ========== Currency Tests ==========
    
    @Test
    @DisplayName("Should start with zero gold")
    void shouldStartWithZeroGold() {
        assertEquals(0, builder.getPlayerGold());
    }
    
    @Test
    @DisplayName("Should set player gold")
    void shouldSetPlayerGold() {
        builder.setPlayerGold(1500);
        assertEquals(1500, builder.getPlayerGold());
    }
    
    // ========== Item Management Tests ==========
    
    @Test
    @DisplayName("Should start with empty item lists")
    void shouldStartWithEmptyItemLists() {
        assertTrue(builder.getBuyItems().isEmpty());
        assertTrue(builder.getPlayerInventory().isEmpty());
        assertTrue(builder.getBuybackItems().isEmpty());
    }
    
    @Test
    @DisplayName("Should add buy items")
    void shouldAddBuyItems() {
        // Given
        VendorItem item1 = createVendorItem("sword", "Iron Sword", 100);
        VendorItem item2 = createVendorItem("shield", "Wooden Shield", 50);
        
        // When
        builder.addBuyItem(item1).addBuyItem(item2);
        
        // Then
        List<VendorItem> items = builder.getBuyItems();
        assertEquals(2, items.size());
        assertEquals("sword", items.get(0).getItemId());
        assertEquals("shield", items.get(1).getItemId());
    }
    
    @Test
    @DisplayName("Should add inventory items")
    void shouldAddInventoryItems() {
        // Given
        InventoryItem item = createInventoryItem("potion", "Health Potion", 25);
        
        // When
        builder.addInventoryItem(item);
        
        // Then
        assertEquals(1, builder.getPlayerInventory().size());
    }
    
    @Test
    @DisplayName("Should add buyback items")
    void shouldAddBuybackItems() {
        // Given
        VendorItem item = createVendorItem("armor", "Leather Armor", 75);
        
        // When
        builder.addBuybackItem(item);
        
        // Then
        assertEquals(1, builder.getBuybackItems().size());
    }
    
    @Test
    @DisplayName("Should clear all items")
    void shouldClearAllItems() {
        // Given
        builder.addBuyItem(createVendorItem("sword", "Sword", 100));
        builder.addInventoryItem(createInventoryItem("potion", "Potion", 25));
        builder.addBuybackItem(createVendorItem("armor", "Armor", 75));
        
        // When
        builder.clearItems();
        
        // Then
        assertTrue(builder.getBuyItems().isEmpty());
        assertTrue(builder.getPlayerInventory().isEmpty());
        assertTrue(builder.getBuybackItems().isEmpty());
    }
    
    @Test
    @DisplayName("Should return defensive copies of item lists")
    void shouldReturnDefensiveCopiesOfItemLists() {
        // Given
        builder.addBuyItem(createVendorItem("item", "Item", 10));
        
        // When
        List<VendorItem> copy = builder.getBuyItems();
        
        // Then
        assertThrows(UnsupportedOperationException.class, () -> 
            copy.add(createVendorItem("external", "External", 0)));
    }
    
    // ========== Selection Tests ==========
    
    @Test
    @DisplayName("Should start with no selection")
    void shouldStartWithNoSelection() {
        assertNull(builder.getSelectedItemId());
        assertEquals(1, builder.getSelectedQuantity());
    }
    
    @Test
    @DisplayName("Should set selected item")
    void shouldSetSelectedItem() {
        builder.setSelectedItem("sword_123");
        assertEquals("sword_123", builder.getSelectedItemId());
    }
    
    @Test
    @DisplayName("Should set selected quantity")
    void shouldSetSelectedQuantity() {
        builder.setSelectedQuantity(5);
        assertEquals(5, builder.getSelectedQuantity());
    }
    
    @Test
    @DisplayName("Should enforce minimum quantity of 1")
    void shouldEnforceMinimumQuantity() {
        builder.setSelectedQuantity(0);
        assertEquals(1, builder.getSelectedQuantity());
        
        builder.setSelectedQuantity(-5);
        assertEquals(1, builder.getSelectedQuantity());
    }
    
    // ========== Template Supplier Tests ==========
    
    @Test
    @DisplayName("Should report no template supplier by default")
    void shouldReportNoTemplateSupplierByDefault() {
        assertFalse(builder.hasTemplateSupplier());
    }
    
    @Test
    @DisplayName("Should throw when getting template without supplier")
    void shouldThrowWhenNoTemplateSupplier() {
        assertThrows(IllegalStateException.class, () -> builder.getTemplate());
    }
    
    @Test
    @DisplayName("Should get template from supplier")
    void shouldGetTemplateFromSupplier() {
        // Given
        String expectedTemplate = "<div class=\"vendor-page\">test</div>";
        builder.setTemplateSupplier(() -> expectedTemplate);
        
        // When / Then
        assertTrue(builder.hasTemplateSupplier());
        assertEquals(expectedTemplate, builder.getTemplate());
    }
    
    // ========== buildTemplateVariables Tests ==========
    
    @Test
    @DisplayName("Should include vendor info in variables")
    void shouldIncludeVendorInfoInVariables() {
        // Given
        builder.setVendorName("Armorer")
               .setVendorId("vendor_456");
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals("Armorer", vars.get("vendorName"));
        assertEquals("vendor_456", vars.get("vendorId"));
    }
    
    @Test
    @DisplayName("Should include tab state in variables")
    void shouldIncludeTabStateInVariables() {
        // Given
        builder.setActiveTab("sell");
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals("sell", vars.get("activeTab"));
        assertEquals(false, vars.get("isBuyTab"));
        assertEquals(true, vars.get("isSellTab"));
        assertEquals(false, vars.get("isBuybackTab"));
    }
    
    @Test
    @DisplayName("Should include player gold in variables")
    void shouldIncludePlayerGoldInVariables() {
        // Given
        builder.setPlayerGold(2500);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(2500, vars.get("playerGold"));
    }
    
    @Test
    @DisplayName("Should include items in variables")
    void shouldIncludeItemsInVariables() {
        // Given
        builder.addBuyItem(createVendorItem("sword", "Sword", 100));
        builder.addBuyItem(createVendorItem("shield", "Shield", 50));
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) vars.get("items");
        assertEquals(2, items.size());
    }
    
    @Test
    @DisplayName("Should calculate transaction total for buy tab")
    void shouldCalculateTransactionTotalForBuyTab() {
        // Given
        builder.addBuyItem(createVendorItem("sword", "Sword", 100))
               .setActiveTab("buy")
               .setSelectedItem("sword")
               .setSelectedQuantity(3);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(300, vars.get("transactionTotal"));
    }
    
    @Test
    @DisplayName("Should calculate canAfford correctly")
    void shouldCalculateCanAffordCorrectly() {
        // Given - player has 200 gold, item costs 100 each
        builder.addBuyItem(createVendorItem("sword", "Sword", 100))
               .setPlayerGold(200)
               .setActiveTab("buy")
               .setSelectedItem("sword")
               .setSelectedQuantity(3); // Total 300, can't afford
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(false, vars.get("canAfford"));
    }
    
    @Test
    @DisplayName("Should include selection state in variables")
    void shouldIncludeSelectionStateInVariables() {
        // Given
        builder.setSelectedItem("item_123")
               .setSelectedQuantity(5);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(true, vars.get("hasSelectedItem"));
        assertEquals("item_123", vars.get("selectedItemId"));
        assertEquals(5, vars.get("selectedQuantity"));
    }
    
    // ========== Builder Pattern Tests ==========
    
    @Test
    @DisplayName("Should support fluent builder pattern")
    void shouldSupportFluentBuilderPattern() {
        // When
        VendorPageBuilder result = builder
            .setVendorName("Trader")
            .setVendorId("trader_1")
            .setActiveTab("buy")
            .setPlayerGold(1000)
            .addBuyItem(createVendorItem("item", "Item", 100))
            .addInventoryItem(createInventoryItem("inv", "Inv", 50))
            .addBuybackItem(createVendorItem("back", "Back", 75))
            .setSelectedItem("item")
            .setSelectedQuantity(2)
            .setTemplateSupplier(() -> "<div/>")
            .clearItems();
        
        // Then
        assertSame(builder, result);
    }
    
    // ========== Helper Methods ==========
    
    private VendorItem createVendorItem(String itemId, String name, int buyPrice) {
        return VendorItem.builder(itemId, name)
            .withBuyPrice(buyPrice)
            .withSellPrice(buyPrice / 2)
            .build();
    }
    
    private InventoryItem createInventoryItem(String itemId, String name, int sellPrice) {
        return new InventoryItem(itemId, name, null, 1, sellPrice, "common", true);
    }
}
