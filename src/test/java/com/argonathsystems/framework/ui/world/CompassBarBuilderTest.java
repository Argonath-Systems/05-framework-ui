package com.argonathsystems.framework.ui.world;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CompassBarBuilder}.
 * 
 * @author Argonath Systems
 * @since 1.2.0
 */
@DisplayName("CompassBarBuilder Tests")
class CompassBarBuilderTest {
    
    private CompassBarBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new CompassBarBuilder();
    }
    
    // ========== Player State Tests ==========
    
    @Test
    @DisplayName("Should set player heading")
    void shouldSetPlayerHeading() {
        builder.setPlayerHeading(90.0f);
        assertEquals(90.0f, builder.getPlayerHeading(), 0.001f);
    }
    
    @Test
    @DisplayName("Should normalize heading over 360")
    void shouldNormalizeHeadingOver360() {
        builder.setPlayerHeading(450.0f);
        assertEquals(90.0f, builder.getPlayerHeading(), 0.001f);
    }
    
    @Test
    @DisplayName("Should normalize negative heading")
    void shouldNormalizeNegativeHeading() {
        builder.setPlayerHeading(-90.0f);
        assertEquals(270.0f, builder.getPlayerHeading(), 0.001f);
    }
    
    @Test
    @DisplayName("Should set player position")
    void shouldSetPlayerPosition() {
        builder.setPlayerPosition(100.5f, -200.75f);
        
        assertEquals(100.5f, builder.getPlayerX(), 0.001f);
        assertEquals(-200.75f, builder.getPlayerZ(), 0.001f);
    }
    
    // ========== Marker Management Tests ==========
    
    @Test
    @DisplayName("Should start with no markers")
    void shouldStartWithNoMarkers() {
        assertTrue(builder.getMarkers().isEmpty());
    }
    
    @Test
    @DisplayName("Should add markers")
    void shouldAddMarkers() {
        // Given
        CompassMarker marker1 = CompassMarker.builder("quest1", "quest")
            .withName("Find the Sword")
            .atPosition(100, 0)
            .build();
        CompassMarker marker2 = CompassMarker.builder("poi1", "poi")
            .withName("Town")
            .atPosition(200, 100)
            .build();
        
        // When
        builder.addMarker(marker1).addMarker(marker2);
        
        // Then
        List<CompassMarker> markers = builder.getMarkers();
        assertEquals(2, markers.size());
    }
    
    @Test
    @DisplayName("Should clear markers")
    void shouldClearMarkers() {
        // Given
        builder.addMarker(CompassMarker.builder("q1", "quest").withName("Objective").atPosition(0, 0).build());
        builder.addMarker(CompassMarker.builder("q2", "quest").withName("Objective").atPosition(0, 0).build());
        assertEquals(2, builder.getMarkers().size());
        
        // When
        builder.clearMarkers();
        
        // Then
        assertTrue(builder.getMarkers().isEmpty());
    }
    
    @Test
    @DisplayName("Should return defensive copy of markers")
    void shouldReturnDefensiveCopyOfMarkers() {
        // Given
        builder.addMarker(CompassMarker.builder("q1", "quest").withName("Objective").atPosition(0, 0).build());
        
        // When
        List<CompassMarker> copy = builder.getMarkers();
        
        // Then
        assertThrows(UnsupportedOperationException.class, () -> 
            copy.add(CompassMarker.builder("external", "quest").withName("External").atPosition(0, 0).build()));
    }
    
    // ========== Settings Tests ==========
    
    @Test
    @DisplayName("Should show distance by default")
    void shouldShowDistanceByDefault() {
        assertTrue(builder.isShowDistance());
    }
    
    @Test
    @DisplayName("Should toggle distance display")
    void shouldToggleDistanceDisplay() {
        builder.setShowDistance(false);
        assertFalse(builder.isShowDistance());
        
        builder.setShowDistance(true);
        assertTrue(builder.isShowDistance());
    }
    
    @Test
    @DisplayName("Should have default visible range of 90 degrees")
    void shouldHaveDefaultVisibleRange() {
        assertEquals(90.0f, builder.getVisibleRange(), 0.001f);
    }
    
    @Test
    @DisplayName("Should set visible range")
    void shouldSetVisibleRange() {
        builder.setVisibleRange(120.0f);
        assertEquals(120.0f, builder.getVisibleRange(), 0.001f);
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
        String expectedTemplate = "<div class=\"compass-bar\">test</div>";
        builder.setTemplateSupplier(() -> expectedTemplate);
        
        // When / Then
        assertTrue(builder.hasTemplateSupplier());
        assertEquals(expectedTemplate, builder.getTemplate());
    }
    
    // ========== buildTemplateVariables Tests ==========
    
    @Test
    @DisplayName("Should include player heading in variables")
    void shouldIncludePlayerHeadingInVariables() {
        // Given
        builder.setPlayerHeading(45.0f);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(45.0f, (Float) vars.get("playerHeading"), 0.001f);
    }
    
    @Test
    @DisplayName("Should include player position in variables")
    void shouldIncludePlayerPositionInVariables() {
        // Given
        builder.setPlayerPosition(100.0f, 200.0f);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(100.0f, (Float) vars.get("playerX"), 0.001f);
        assertEquals(200.0f, (Float) vars.get("playerZ"), 0.001f);
    }
    
    @Test
    @DisplayName("Should include direction markers when facing North")
    void shouldIncludeDirectionMarkersWhenFacingNorth() {
        // Given - facing North (0 degrees)
        builder.setPlayerHeading(0.0f);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> directions = (List<Map<String, Object>>) vars.get("directions");
        assertNotNull(directions);
        assertFalse(directions.isEmpty());
        
        // Should include N, NE, NW within 90 degree range
        assertTrue(directions.stream().anyMatch(d -> "N".equals(d.get("label"))));
    }
    
    @Test
    @DisplayName("Should filter markers by visible range")
    void shouldFilterMarkersByVisibleRange() {
        // Given - player at origin facing North, marker directly behind (South)
        builder.setPlayerHeading(0.0f)
               .setPlayerPosition(0.0f, 0.0f)
               .setVisibleRange(90.0f);
        
        // Marker directly South (behind player)
        CompassMarker behindMarker = CompassMarker.builder("behind", "quest")
            .withName("Behind")
            .atPosition(0, 100)
            .build();
        builder.addMarker(behindMarker);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then - marker should be filtered out (180 degrees away)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> markers = (List<Map<String, Object>>) vars.get("markers");
        assertTrue(markers.isEmpty());
    }
    
    @Test
    @DisplayName("Should include markers within visible range")
    void shouldIncludeMarkersWithinVisibleRange() {
        // Given - player at origin facing North
        builder.setPlayerHeading(0.0f)
               .setPlayerPosition(0.0f, 0.0f)
               .setVisibleRange(90.0f);
        
        // Marker directly North (in front)
        CompassMarker frontMarker = CompassMarker.builder("front", "quest")
            .withName("Front")
            .atPosition(0, -100)
            .build();
        builder.addMarker(frontMarker);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then - marker should be included
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> markers = (List<Map<String, Object>>) vars.get("markers");
        assertEquals(1, markers.size());
    }
    
    @Test
    @DisplayName("Should include settings in variables")
    void shouldIncludeSettingsInVariables() {
        // Given
        builder.setShowDistance(false)
               .setVisibleRange(120.0f);
        
        // When
        Map<String, Object> vars = builder.buildTemplateVariables();
        
        // Then
        assertEquals(false, vars.get("showDistance"));
        assertEquals(120.0f, (Float) vars.get("visibleRange"), 0.001f);
    }
    
    // ========== Builder Pattern Tests ==========
    
    @Test
    @DisplayName("Should support fluent builder pattern")
    void shouldSupportFluentBuilderPattern() {
        // When
        CompassBarBuilder result = builder
            .setPlayerHeading(45.0f)
            .setPlayerPosition(100.0f, 200.0f)
            .addMarker(CompassMarker.builder("q1", "quest").withName("Objective").atPosition(0, 0).build())
            .setShowDistance(true)
            .setVisibleRange(90.0f)
            .setTemplateSupplier(() -> "<div/>")
            .clearMarkers();
        
        // Then
        assertSame(builder, result);
    }
}
