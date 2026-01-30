package com.argonathsystems.framework.ui.combat;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CombatFramesBuilder}.
 * 
 * @author Argonath Systems
 * @since 1.2.0
 */
@DisplayName("CombatFramesBuilder Tests")
class CombatFramesBuilderTest {
    
    private CombatFramesBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new CombatFramesBuilder();
    }
    
    // ========== Unit Frame Management Tests ==========
    
    @Test
    @DisplayName("Should start with no frames")
    void shouldStartWithNoFrames() {
        assertNull(builder.getPlayerFrame());
        assertNull(builder.getTargetFrame());
        assertNull(builder.getTargetOfTargetFrame());
        assertTrue(builder.getPartyFrames().isEmpty());
    }
    
    @Test
    @DisplayName("Should set player frame")
    void shouldSetPlayerFrame() {
        // Given
        UnitFrame frame = createTestFrame("Player", 100, 100);
        
        // When
        builder.setPlayerFrame(frame);
        
        // Then
        assertNotNull(builder.getPlayerFrame());
        assertEquals("Player", builder.getPlayerFrame().getName());
    }
    
    @Test
    @DisplayName("Should set target frame")
    void shouldSetTargetFrame() {
        // Given
        UnitFrame frame = createTestFrame("Enemy Boss", 5000, 10000);
        
        // When
        builder.setTargetFrame(frame);
        
        // Then
        assertNotNull(builder.getTargetFrame());
        assertEquals("Enemy Boss", builder.getTargetFrame().getName());
    }
    
    @Test
    @DisplayName("Should set target-of-target frame")
    void shouldSetTargetOfTargetFrame() {
        // Given
        UnitFrame frame = createTestFrame("Tank", 200, 200);
        
        // When
        builder.setTargetOfTargetFrame(frame);
        
        // Then
        assertNotNull(builder.getTargetOfTargetFrame());
        assertEquals("Tank", builder.getTargetOfTargetFrame().getName());
    }
    
    @Test
    @DisplayName("Should clear target frame when set to null")
    void shouldClearTargetFrame() {
        // Given
        builder.setTargetFrame(createTestFrame("Target", 100, 100));
        assertNotNull(builder.getTargetFrame());
        
        // When
        builder.setTargetFrame(null);
        
        // Then
        assertNull(builder.getTargetFrame());
    }
    
    // ========== Party Frame Tests ==========
    
    @Test
    @DisplayName("Should add party members")
    void shouldAddPartyMembers() {
        // When
        builder.addPartyMember(createTestFrame("Party1", 100, 100));
        builder.addPartyMember(createTestFrame("Party2", 80, 100));
        builder.addPartyMember(createTestFrame("Party3", 50, 100));
        
        // Then
        List<UnitFrame> party = builder.getPartyFrames();
        assertEquals(3, party.size());
        assertEquals("Party1", party.get(0).getName());
        assertEquals("Party2", party.get(1).getName());
        assertEquals("Party3", party.get(2).getName());
    }
    
    @Test
    @DisplayName("Should clear party members")
    void shouldClearPartyMembers() {
        // Given
        builder.addPartyMember(createTestFrame("Party1", 100, 100));
        builder.addPartyMember(createTestFrame("Party2", 100, 100));
        assertEquals(2, builder.getPartyFrames().size());
        
        // When
        builder.clearPartyMembers();
        
        // Then
        assertTrue(builder.getPartyFrames().isEmpty());
    }
    
    @Test
    @DisplayName("Should return defensive copy of party frames")
    void shouldReturnDefensiveCopyOfPartyFrames() {
        // Given
        builder.addPartyMember(createTestFrame("Party1", 100, 100));
        
        // When
        List<UnitFrame> copy = builder.getPartyFrames();
        
        // Then - modifying copy should not affect builder
        assertThrows(UnsupportedOperationException.class, () -> 
            copy.add(createTestFrame("External", 100, 100)));
    }
    
    // ========== Display Flag Tests ==========
    
    @Test
    @DisplayName("Should show party frames by default")
    void shouldShowPartyFramesByDefault() {
        assertTrue(builder.isShowPartyFrames());
    }
    
    @Test
    @DisplayName("Should show target-of-target by default")
    void shouldShowTargetOfTargetByDefault() {
        assertTrue(builder.isShowTargetOfTarget());
    }
    
    @Test
    @DisplayName("Should toggle party frames visibility")
    void shouldTogglePartyFramesVisibility() {
        builder.setShowPartyFrames(false);
        assertFalse(builder.isShowPartyFrames());
        
        builder.setShowPartyFrames(true);
        assertTrue(builder.isShowPartyFrames());
    }
    
    @Test
    @DisplayName("Should toggle target-of-target visibility")
    void shouldToggleTargetOfTargetVisibility() {
        builder.setShowTargetOfTarget(false);
        assertFalse(builder.isShowTargetOfTarget());
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
        String expectedTemplate = "<div class=\"combat-frames\">test</div>";
        builder.setTemplateSupplier(() -> expectedTemplate);
        
        // When / Then
        assertTrue(builder.hasTemplateSupplier());
        assertEquals(expectedTemplate, builder.getTemplate());
    }
    
    // ========== buildTemplateVariables Tests ==========
    
    @Test
    @DisplayName("Should mark hasPlayer false when no player frame")
    void shouldMarkHasPlayerFalseWhenNoPlayerFrame() {
        Map<String, Object> vars = builder.buildTemplateVariables();
        assertEquals(false, vars.get("hasPlayer"));
    }
    
    @Test
    @DisplayName("Should include player frame in variables")
    void shouldIncludePlayerFrameInVariables() {
        // Given
        builder.setPlayerFrame(createTestFrame("Hero", 100, 100));
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(true, vars.get("hasPlayer"));
        assertNotNull(vars.get("player"));
    }
    
    @Test
    @DisplayName("Should include target frame when set")
    void shouldIncludeTargetFrameWhenSet() {
        // Given
        builder.setTargetFrame(createTestFrame("Boss", 10000, 10000));
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(true, vars.get("hasTarget"));
        assertNotNull(vars.get("target"));
    }
    
    @Test
    @DisplayName("Should include party frames in variables")
    void shouldIncludePartyFramesInVariables() {
        // Given
        builder.addPartyMember(createTestFrame("Member1", 100, 100));
        builder.addPartyMember(createTestFrame("Member2", 100, 100));
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(true, vars.get("hasParty"));
        assertEquals(2, vars.get("partySize"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> partyList = (List<Map<String, Object>>) vars.get("partyMembers");
        assertEquals(2, partyList.size());
    }
    
    @Test
    @DisplayName("Should respect showPartyFrames flag")
    void shouldRespectShowPartyFramesFlag() {
        // Given
        builder.addPartyMember(createTestFrame("Member1", 100, 100));
        builder.setShowPartyFrames(false);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(false, vars.get("hasParty"));
        assertEquals(0, vars.get("partySize"));
    }
    
    @Test
    @DisplayName("Should respect showTargetOfTarget flag")
    void shouldRespectShowTargetOfTargetFlag() {
        // Given
        builder.setTargetOfTargetFrame(createTestFrame("ToT", 100, 100));
        builder.setShowTargetOfTarget(false);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(false, vars.get("hasTargetOfTarget"));
    }
    
    // ========== Builder Pattern Tests ==========
    
    @Test
    @DisplayName("Should support fluent builder pattern")
    void shouldSupportFluentBuilderPattern() {
        // When
        CombatFramesBuilder result = builder
            .setPlayerFrame(createTestFrame("Player", 100, 100))
            .setTargetFrame(createTestFrame("Target", 100, 100))
            .setTargetOfTargetFrame(createTestFrame("ToT", 100, 100))
            .addPartyMember(createTestFrame("Party1", 100, 100))
            .setShowPartyFrames(true)
            .setShowTargetOfTarget(true)
            .setTemplateSupplier(() -> "<div/>")
            .clearPartyMembers();
        
        // Then
        assertSame(builder, result);
    }
    
    // ========== Helper Methods ==========
    
    private UnitFrame createTestFrame(String name, int currentHp, int maxHp) {
        return UnitFrame.builder("unit-" + name.toLowerCase(), name)
            .withLevel(60)
            .withHealth(currentHp, maxHp)
            .build();
    }
}
