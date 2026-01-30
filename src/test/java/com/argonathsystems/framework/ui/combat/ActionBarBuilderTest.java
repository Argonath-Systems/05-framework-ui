package com.argonathsystems.framework.ui.combat;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ActionBarBuilder}.
 * 
 * @author Argonath Systems
 * @since 1.2.0
 */
@DisplayName("ActionBarBuilder Tests")
class ActionBarBuilderTest {
    
    private ActionBarBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new ActionBarBuilder();
    }
    
    // ========== Slot Management Tests ==========
    
    @Test
    @DisplayName("Should initialize with 12 empty slots")
    void shouldInitializeWith12EmptySlots() {
        for (int i = 1; i <= 12; i++) {
            ActionSlot slot = builder.getSlot(i);
            assertNotNull(slot, "Slot " + i + " should not be null");
            assertTrue(slot.isEmpty(), "Slot " + i + " should be empty");
        }
    }
    
    @Test
    @DisplayName("Should return null for invalid slot indices")
    void shouldReturnNullForInvalidSlotIndices() {
        assertNull(builder.getSlot(0));
        assertNull(builder.getSlot(13));
        assertNull(builder.getSlot(-1));
    }
    
    @Test
    @DisplayName("Should set action slot at valid index")
    void shouldSetSlotAtValidIndex() {
        // Given
        ActionSlot slot = ActionSlot.builder(3)
            .withAction("fireball")
            .withName("Fireball")
            .withIcon("icons/fireball.png")
            .withKeybind("3")
            .build();
        
        // When
        builder.setSlot(3, slot);
        
        // Then
        ActionSlot retrieved = builder.getSlot(3);
        assertNotNull(retrieved);
        assertEquals("fireball", retrieved.getActionId());
        assertEquals("Fireball", retrieved.getName());
    }
    
    @Test
    @DisplayName("Should ignore setSlot for invalid indices")
    void shouldIgnoreSetSlotForInvalidIndices() {
        // Given
        ActionSlot slot = ActionSlot.builder(1).withAction("test").build();
        
        // When - should not throw
        builder.setSlot(0, slot);
        builder.setSlot(13, slot);
        
        // Then - no exception, original slots unchanged
        assertTrue(builder.getSlot(1).isEmpty());
    }
    
    @Test
    @DisplayName("Should clear all slots")
    void shouldClearAllSlots() {
        // Given - set some slots
        builder.setSlot(1, ActionSlot.builder(1).withAction("test1").build());
        builder.setSlot(5, ActionSlot.builder(5).withAction("test5").build());
        
        // When
        builder.clearSlots();
        
        // Then
        for (int i = 1; i <= 12; i++) {
            assertTrue(builder.getSlot(i).isEmpty(), "Slot " + i + " should be empty after clear");
        }
    }
    
    // ========== Resource Bar Tests ==========
    
    @Test
    @DisplayName("Should set health bar")
    void shouldSetHealthBar() {
        // When
        builder.setHealth(80, 100);
        
        // Then
        ResourceBar healthBar = builder.getHealthBar();
        assertNotNull(healthBar);
        assertEquals(80, healthBar.getCurrent());
        assertEquals(100, healthBar.getMaximum());
        assertEquals("health", healthBar.getType());
    }
    
    @Test
    @DisplayName("Should set primary resource bar")
    void shouldSetPrimaryResource() {
        // When
        builder.setPrimaryResource("mana", 50, 200);
        
        // Then
        ResourceBar primary = builder.getPrimaryResource();
        assertNotNull(primary);
        assertEquals("mana", primary.getType());
        assertEquals(50, primary.getCurrent());
        assertEquals(200, primary.getMaximum());
    }
    
    @Test
    @DisplayName("Should set secondary resource bar")
    void shouldSetSecondaryResource() {
        // When
        builder.setSecondaryResource("energy", 75, 100);
        
        // Then
        ResourceBar secondary = builder.getSecondaryResource();
        assertNotNull(secondary);
        assertEquals("energy", secondary.getType());
        assertEquals(75, secondary.getCurrent());
        assertEquals(100, secondary.getMaximum());
    }
    
    // ========== State Flags Tests ==========
    
    @Test
    @DisplayName("Should set combat state")
    void shouldSetCombatState() {
        assertFalse(builder.isInCombat());
        
        builder.setInCombat(true);
        assertTrue(builder.isInCombat());
        
        builder.setInCombat(false);
        assertFalse(builder.isInCombat());
    }
    
    @Test
    @DisplayName("Should set show resource bars")
    void shouldSetShowResourceBars() {
        assertTrue(builder.isShowResourceBars());
        
        builder.setShowResourceBars(false);
        assertFalse(builder.isShowResourceBars());
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
        String expectedTemplate = "<div class=\"action-bar\">test</div>";
        builder.setTemplateSupplier(() -> expectedTemplate);
        
        // When
        String template = builder.getTemplate();
        
        // Then
        assertTrue(builder.hasTemplateSupplier());
        assertEquals(expectedTemplate, template);
    }
    
    // ========== buildTemplateVariables Tests ==========
    
    @Test
    @DisplayName("Should build template variables with slots")
    void shouldBuildTemplateVariablesWithSlots() {
        // Given
        builder.setSlot(1, ActionSlot.builder(1)
            .withAction("attack")
            .withName("Basic Attack")
            .withKeybind("1")
            .build());
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertNotNull(vars.get("slots"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> slots = (List<Map<String, Object>>) vars.get("slots");
        assertEquals(12, slots.size());
        
        Map<String, Object> slot1 = slots.get(0);
        assertEquals("attack", slot1.get("actionId"));
        assertEquals("Basic Attack", slot1.get("name"));
    }
    
    @Test
    @DisplayName("Should build template variables with player stats")
    void shouldBuildTemplateVariablesWithPlayerStats() {
        // Given
        builder.setHealth(100, 100)
               .setPrimaryResource("mana", 50, 100);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> playerStats = (Map<String, Object>) vars.get("playerStats");
        assertNotNull(playerStats);
        assertNotNull(playerStats.get("health"));
        assertNotNull(playerStats.get("primary"));
    }
    
    @Test
    @DisplayName("Should include state flags in template variables")
    void shouldIncludeStateFlagsInTemplateVariables() {
        // Given
        builder.setInCombat(true)
               .setShowResourceBars(false);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(true, vars.get("inCombat"));
        assertEquals(false, vars.get("showResourceBars"));
    }
    
    // ========== Builder Pattern Tests ==========
    
    @Test
    @DisplayName("Should support fluent builder pattern")
    void shouldSupportFluentBuilderPattern() {
        // When - chain all methods
        ActionBarBuilder result = builder
            .setSlot(1, ActionSlot.builder(1).withAction("skill1").build())
            .setHealth(100, 100)
            .setPrimaryResource("mana", 50, 100)
            .setSecondaryResource("rage", 0, 100)
            .setInCombat(true)
            .setShowResourceBars(true)
            .setTemplateSupplier(() -> "<div/>")
            .clearSlots();
        
        // Then - should return same builder instance
        assertSame(builder, result);
    }
}
