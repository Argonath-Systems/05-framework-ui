package com.argonathsystems.framework.ui.cinematic;

import com.argonathsystems.framework.accessorapi.CameraAccessor;
import com.argonathsystems.framework.accessorapi.CameraAccessor.CameraState;
import com.argonathsystems.framework.accessorapi.CameraAccessor.Vector2;
import com.argonathsystems.framework.accessorapi.CameraAccessor.Vector3;
import com.argonathsystems.framework.accessorapi.PlayerAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Service for managing cinematic camera sequences.
 * 
 * <p>Provides smooth camera transitions, path following, and focus effects
 * for dialogue cutscenes, quest moments, and world showcases.
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Smooth position/rotation interpolation (linear, ease-in-out, cubic)</li>
 *   <li>Camera path following with keyframes</li>
 *   <li>Focus on entity/position with depth-of-field effects</li>
 *   <li>Letterbox mode for cutscenes</li>
 *   <li>Camera shake effects</li>
 *   <li>Player input blocking during sequences</li>
 * </ul>
 * 
 * <h2>Usage - Simple Pan</h2>
 * <pre>{@code
 * cinematicCamera.panTo(player, targetPosition, Duration.ofSeconds(2))
 *     .withEasing(Easing.EASE_IN_OUT)
 *     .onComplete(() -> startDialogue())
 *     .execute();
 * }</pre>
 * 
 * <h2>Usage - Camera Sequence</h2>
 * <pre>{@code
 * CameraSequence sequence = CameraSequence.builder()
 *     .addKeyframe(CameraKeyframe.at(pos1, rot1, 0.0f))
 *     .addKeyframe(CameraKeyframe.at(pos2, rot2, 2.0f))
 *     .addKeyframe(CameraKeyframe.at(pos3, rot3, 4.0f))
 *     .withLetterbox(true)
 *     .withFocusTarget(npc.getPosition())
 *     .build();
 * 
 * cinematicCamera.playSequence(player, sequence)
 *     .onComplete(() -> restoreCamera())
 *     .execute();
 * }</pre>
 * 
 * <h2>Specification Reference</h2>
 * <ul>
 *   <li>SF-UI-045: Cinematic Camera</li>
 *   <li>IMPL-PLAN-2026-Q1-NPC-QUEST-ANIMATION: Phase 6</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 2026-01-31
 */
public class CinematicCameraService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CinematicCameraService.class);
    private static final int TICK_RATE_MS = 50; // 20 ticks per second
    
    private final CameraAccessor cameraAccessor;
    private final PlayerAccessor playerAccessor;
    private final ScheduledExecutorService scheduler;
    
    // Active sequences per player
    private final Map<UUID, ActiveSequence> activeSequences = new ConcurrentHashMap<>();
    
    // Stored camera states for restoration
    private final Map<UUID, CameraState> savedCameraStates = new ConcurrentHashMap<>();
    
    /**
     * Constructs the service.
     * 
     * @param cameraAccessor Accessor for camera operations
     * @param playerAccessor Accessor for player operations
     */
    public CinematicCameraService(CameraAccessor cameraAccessor, PlayerAccessor playerAccessor) {
        this.cameraAccessor = Objects.requireNonNull(cameraAccessor, "cameraAccessor");
        this.playerAccessor = Objects.requireNonNull(playerAccessor, "playerAccessor");
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "CinematicCamera-Scheduler");
            t.setDaemon(true);
            return t;
        });
        
        LOGGER.info("CinematicCameraService initialized");
    }
    
    // ========================================================================
    // Simple Transitions
    // ========================================================================
    
    /**
     * Creates a smooth pan transition to a target position.
     * 
     * @param playerId The player to move camera for
     * @param targetPosition Target position (x, y, z)
     * @param duration Duration of the pan
     * @return CameraTransitionBuilder for configuration
     */
    public CameraTransitionBuilder panTo(UUID playerId, Vector3 targetPosition, Duration duration) {
        return new CameraTransitionBuilder(this, playerId, TransitionType.PAN)
            .targetPosition(targetPosition)
            .duration(duration);
    }
    
    /**
     * Creates a rotation transition to face a target.
     * 
     * @param playerId The player to rotate camera for
     * @param targetRotation Target rotation (pitch, yaw)
     * @param duration Duration of the rotation
     * @return CameraTransitionBuilder for configuration
     */
    public CameraTransitionBuilder rotateTo(UUID playerId, Vector2 targetRotation, Duration duration) {
        return new CameraTransitionBuilder(this, playerId, TransitionType.ROTATE)
            .targetRotation(targetRotation)
            .duration(duration);
    }
    
    /**
     * Creates a focus transition to an entity or position.
     * 
     * @param playerId The player
     * @param focusPosition Position to focus on
     * @param duration Duration to reach focus
     * @return CameraTransitionBuilder for configuration
     */
    public CameraTransitionBuilder focusOn(UUID playerId, Vector3 focusPosition, Duration duration) {
        return new CameraTransitionBuilder(this, playerId, TransitionType.FOCUS)
            .focusPosition(focusPosition)
            .duration(duration);
    }
    
    // ========================================================================
    // Sequence Playback
    // ========================================================================
    
    /**
     * Plays a camera sequence for a player.
     * 
     * @param playerId The player
     * @param sequence The camera sequence
     * @return SequencePlaybackBuilder for configuration
     */
    public SequencePlaybackBuilder playSequence(UUID playerId, CameraSequence sequence) {
        return new SequencePlaybackBuilder(this, playerId, sequence);
    }
    
    /**
     * Stops any active sequence for a player.
     * 
     * @param playerId The player
     * @param restoreCamera Whether to restore original camera state
     */
    public void stopSequence(UUID playerId, boolean restoreCamera) {
        ActiveSequence active = activeSequences.remove(playerId);
        if (active != null) {
            active.cancel();
            
            if (restoreCamera) {
                restoreCameraState(playerId);
            }
            
            LOGGER.debug("Stopped camera sequence for player {}", playerId);
        }
    }
    
    /**
     * Checks if a player has an active sequence.
     * 
     * @param playerId The player
     * @return true if sequence is playing
     */
    public boolean hasActiveSequence(UUID playerId) {
        return activeSequences.containsKey(playerId);
    }
    
    // ========================================================================
    // Effects
    // ========================================================================
    
    /**
     * Applies camera shake effect.
     * 
     * @param playerId The player
     * @param intensity Shake intensity (0.0 to 1.0)
     * @param duration Duration of the shake
     */
    public void shake(UUID playerId, float intensity, Duration duration) {
        Objects.requireNonNull(playerId, "playerId");
        
        float clampedIntensity = Math.max(0.0f, Math.min(1.0f, intensity));
        
        cameraAccessor.applyShake(playerId, clampedIntensity, duration.toMillis());
        
        LOGGER.debug("Applied camera shake to player {} (intensity: {}, duration: {}ms)", 
            playerId, clampedIntensity, duration.toMillis());
    }
    
    /**
     * Enables letterbox mode (black bars).
     * 
     * @param playerId The player
     * @param enabled Whether to enable letterbox
     */
    public void setLetterbox(UUID playerId, boolean enabled) {
        cameraAccessor.setLetterboxMode(playerId, enabled);
        LOGGER.debug("Set letterbox mode for player {}: {}", playerId, enabled);
    }
    
    /**
     * Sets depth of field focus.
     * 
     * @param playerId The player
     * @param focalDistance Distance to focus point
     * @param aperture Aperture size (smaller = more blur)
     */
    public void setDepthOfField(UUID playerId, float focalDistance, float aperture) {
        cameraAccessor.setDepthOfField(playerId, focalDistance, aperture);
        LOGGER.debug("Set DoF for player {}: focal={}, aperture={}", 
            playerId, focalDistance, aperture);
    }
    
    /**
     * Resets depth of field to default.
     * 
     * @param playerId The player
     */
    public void resetDepthOfField(UUID playerId) {
        cameraAccessor.resetDepthOfField(playerId);
    }
    
    // ========================================================================
    // Camera State Management
    // ========================================================================
    
    /**
     * Saves the current camera state for later restoration.
     * 
     * @param playerId The player
     */
    public void saveCameraState(UUID playerId) {
        CameraState state = cameraAccessor.getCameraState(playerId);
        if (state != null) {
            savedCameraStates.put(playerId, state);
            LOGGER.debug("Saved camera state for player {}", playerId);
        }
    }
    
    /**
     * Restores a previously saved camera state.
     * 
     * @param playerId The player
     * @return true if state was restored
     */
    public boolean restoreCameraState(UUID playerId) {
        CameraState state = savedCameraStates.remove(playerId);
        if (state != null) {
            cameraAccessor.setCameraState(playerId, state);
            LOGGER.debug("Restored camera state for player {}", playerId);
            return true;
        }
        return false;
    }
    
    /**
     * Blocks player input during cinematic.
     * 
     * @param playerId The player
     * @param blocked Whether input is blocked
     */
    public void setInputBlocked(UUID playerId, boolean blocked) {
        playerAccessor.setInputBlocked(playerId, blocked);
        LOGGER.debug("Set input blocked for player {}: {}", playerId, blocked);
    }
    
    // ========================================================================
    // Shutdown
    // ========================================================================
    
    /**
     * Shuts down the service, stopping all sequences.
     */
    public void shutdown() {
        // Stop all active sequences
        for (UUID playerId : new ArrayList<>(activeSequences.keySet())) {
            stopSequence(playerId, true);
        }
        
        scheduler.shutdownNow();
        LOGGER.info("CinematicCameraService shut down");
    }
    
    // ========================================================================
    // Internal Execution
    // ========================================================================
    
    void executeTransition(CameraTransitionBuilder builder) {
        UUID playerId = builder.playerId;
        
        // Save camera state if requested
        if (builder.saveState) {
            saveCameraState(playerId);
        }
        
        // Block input if requested
        if (builder.blockInput) {
            setInputBlocked(playerId, true);
        }
        
        // Set letterbox if requested
        if (builder.letterbox) {
            setLetterbox(playerId, true);
        }
        
        // Create and start active sequence
        ActiveSequence active = new ActiveSequence(playerId, builder);
        activeSequences.put(playerId, active);
        
        active.start();
    }
    
    void executeSequence(SequencePlaybackBuilder builder) {
        UUID playerId = builder.playerId;
        
        // Save camera state
        saveCameraState(playerId);
        
        // Block input during sequence
        if (builder.blockInput) {
            setInputBlocked(playerId, true);
        }
        
        // Enable letterbox if sequence requests it
        if (builder.sequence.isLetterboxEnabled()) {
            setLetterbox(playerId, true);
        }
        
        // Start sequence
        ActiveSequence active = new ActiveSequence(playerId, builder);
        activeSequences.put(playerId, active);
        
        active.start();
    }
    
    // ========================================================================
    // Builder Classes
    // ========================================================================
    
    /**
     * Builder for camera transitions.
     */
    public static class CameraTransitionBuilder {
        private final CinematicCameraService service;
        final UUID playerId;
        final TransitionType type;
        
        Vector3 targetPosition;
        Vector2 targetRotation;
        Vector3 focusPosition;
        Duration duration = Duration.ofSeconds(1);
        Easing easing = Easing.EASE_IN_OUT;
        boolean saveState = true;
        boolean blockInput = true;
        boolean letterbox = false;
        Runnable onComplete;
        Consumer<Float> onProgress;
        
        CameraTransitionBuilder(CinematicCameraService service, UUID playerId, TransitionType type) {
            this.service = service;
            this.playerId = playerId;
            this.type = type;
        }
        
        public CameraTransitionBuilder targetPosition(Vector3 position) {
            this.targetPosition = position;
            return this;
        }
        
        public CameraTransitionBuilder targetRotation(Vector2 rotation) {
            this.targetRotation = rotation;
            return this;
        }
        
        public CameraTransitionBuilder focusPosition(Vector3 position) {
            this.focusPosition = position;
            return this;
        }
        
        public CameraTransitionBuilder duration(Duration duration) {
            this.duration = duration;
            return this;
        }
        
        public CameraTransitionBuilder withEasing(Easing easing) {
            this.easing = easing;
            return this;
        }
        
        public CameraTransitionBuilder withLetterbox(boolean enabled) {
            this.letterbox = enabled;
            return this;
        }
        
        public CameraTransitionBuilder blockInput(boolean block) {
            this.blockInput = block;
            return this;
        }
        
        public CameraTransitionBuilder saveState(boolean save) {
            this.saveState = save;
            return this;
        }
        
        public CameraTransitionBuilder onComplete(Runnable callback) {
            this.onComplete = callback;
            return this;
        }
        
        public CameraTransitionBuilder onProgress(Consumer<Float> callback) {
            this.onProgress = callback;
            return this;
        }
        
        public void execute() {
            service.executeTransition(this);
        }
    }
    
    /**
     * Builder for sequence playback.
     */
    public static class SequencePlaybackBuilder {
        private final CinematicCameraService service;
        final UUID playerId;
        final CameraSequence sequence;
        
        boolean blockInput = true;
        Runnable onComplete;
        Consumer<Integer> onKeyframe;
        
        SequencePlaybackBuilder(CinematicCameraService service, UUID playerId, CameraSequence sequence) {
            this.service = service;
            this.playerId = playerId;
            this.sequence = sequence;
        }
        
        public SequencePlaybackBuilder blockInput(boolean block) {
            this.blockInput = block;
            return this;
        }
        
        public SequencePlaybackBuilder onComplete(Runnable callback) {
            this.onComplete = callback;
            return this;
        }
        
        public SequencePlaybackBuilder onKeyframe(Consumer<Integer> callback) {
            this.onKeyframe = callback;
            return this;
        }
        
        public void execute() {
            service.executeSequence(this);
        }
    }
    
    // ========================================================================
    // Internal Types
    // ========================================================================
    
    private class ActiveSequence {
        private final UUID playerId;
        private final CameraTransitionBuilder transitionBuilder;
        private final SequencePlaybackBuilder sequenceBuilder;
        
        private ScheduledFuture<?> tickTask;
        private long startTime;
        private volatile boolean cancelled = false;
        
        ActiveSequence(UUID playerId, CameraTransitionBuilder builder) {
            this.playerId = playerId;
            this.transitionBuilder = builder;
            this.sequenceBuilder = null;
        }
        
        ActiveSequence(UUID playerId, SequencePlaybackBuilder builder) {
            this.playerId = playerId;
            this.transitionBuilder = null;
            this.sequenceBuilder = builder;
        }
        
        void start() {
            startTime = System.currentTimeMillis();
            
            tickTask = scheduler.scheduleAtFixedRate(
                this::tick,
                0,
                TICK_RATE_MS,
                TimeUnit.MILLISECONDS
            );
        }
        
        void cancel() {
            cancelled = true;
            if (tickTask != null) {
                tickTask.cancel(false);
            }
        }
        
        private void tick() {
            if (cancelled) return;
            
            try {
                if (transitionBuilder != null) {
                    tickTransition();
                } else if (sequenceBuilder != null) {
                    tickSequence();
                }
            } catch (Exception e) {
                LOGGER.error("Error in camera tick for player {}", playerId, e);
                complete();
            }
        }
        
        private void tickTransition() {
            long elapsed = System.currentTimeMillis() - startTime;
            long totalMs = transitionBuilder.duration.toMillis();
            float progress = Math.min(1.0f, (float) elapsed / totalMs);
            
            // Apply easing
            float easedProgress = transitionBuilder.easing.apply(progress);
            
            // Notify progress callback
            if (transitionBuilder.onProgress != null) {
                transitionBuilder.onProgress.accept(easedProgress);
            }
            
            // Get current camera state
            CameraState current = cameraAccessor.getCameraState(playerId);
            if (current == null) {
                complete();
                return;
            }
            
            // Interpolate based on transition type
            switch (transitionBuilder.type) {
                case PAN:
                    if (transitionBuilder.targetPosition != null) {
                        Vector3 newPos = current.position().lerp(transitionBuilder.targetPosition, easedProgress);
                        cameraAccessor.setCameraPosition(playerId, newPos);
                    }
                    break;
                    
                case ROTATE:
                    if (transitionBuilder.targetRotation != null) {
                        Vector2 newRot = current.rotation().lerp(transitionBuilder.targetRotation, easedProgress);
                        cameraAccessor.setCameraRotation(playerId, newRot);
                    }
                    break;
                    
                case FOCUS:
                    if (transitionBuilder.focusPosition != null) {
                        // Calculate rotation to face target
                        Vector2 targetRot = calculateLookAtRotation(current.position(), transitionBuilder.focusPosition);
                        Vector2 newRot = current.rotation().lerp(targetRot, easedProgress);
                        cameraAccessor.setCameraRotation(playerId, newRot);
                    }
                    break;
            }
            
            // Check if complete
            if (progress >= 1.0f) {
                complete();
            }
        }
        
        private void tickSequence() {
            long elapsed = System.currentTimeMillis() - startTime;
            CameraSequence sequence = sequenceBuilder.sequence;
            
            // Find current and next keyframes
            List<CameraKeyframe> keyframes = sequence.getKeyframes();
            CameraKeyframe current = null;
            CameraKeyframe next = null;
            
            for (int i = 0; i < keyframes.size() - 1; i++) {
                CameraKeyframe kf = keyframes.get(i);
                CameraKeyframe nextKf = keyframes.get(i + 1);
                
                if (elapsed >= kf.timeSeconds() * 1000 && elapsed < nextKf.timeSeconds() * 1000) {
                    current = kf;
                    next = nextKf;
                    break;
                }
            }
            
            // Check if sequence complete
            CameraKeyframe lastKf = keyframes.get(keyframes.size() - 1);
            if (elapsed >= lastKf.timeSeconds() * 1000) {
                // Apply final keyframe
                cameraAccessor.setCameraPosition(playerId, lastKf.position());
                cameraAccessor.setCameraRotation(playerId, lastKf.rotation());
                complete();
                return;
            }
            
            // Interpolate between keyframes
            if (current != null && next != null) {
                float segmentStart = current.timeSeconds() * 1000;
                float segmentDuration = (next.timeSeconds() - current.timeSeconds()) * 1000;
                float segmentProgress = (elapsed - segmentStart) / segmentDuration;
                
                Easing easing = sequence.getEasing();
                float easedProgress = easing.apply(segmentProgress);
                
                Vector3 newPos = current.position().lerp(next.position(), easedProgress);
                Vector2 newRot = current.rotation().lerp(next.rotation(), easedProgress);
                
                cameraAccessor.setCameraPosition(playerId, newPos);
                cameraAccessor.setCameraRotation(playerId, newRot);
            }
        }
        
        private void complete() {
            cancel();
            activeSequences.remove(playerId);
            
            // Restore states
            if (transitionBuilder != null) {
                if (transitionBuilder.letterbox) {
                    setLetterbox(playerId, false);
                }
                if (transitionBuilder.blockInput) {
                    setInputBlocked(playerId, false);
                }
                if (transitionBuilder.onComplete != null) {
                    transitionBuilder.onComplete.run();
                }
            } else if (sequenceBuilder != null) {
                if (sequenceBuilder.sequence.isLetterboxEnabled()) {
                    setLetterbox(playerId, false);
                }
                if (sequenceBuilder.blockInput) {
                    setInputBlocked(playerId, false);
                }
                // Restore camera state
                restoreCameraState(playerId);
                
                if (sequenceBuilder.onComplete != null) {
                    sequenceBuilder.onComplete.run();
                }
            }
        }
        
        private Vector2 calculateLookAtRotation(Vector3 from, Vector3 to) {
            double dx = to.x() - from.x();
            double dy = to.y() - from.y();
            double dz = to.z() - from.z();
            
            double yaw = Math.toDegrees(Math.atan2(dx, dz));
            double pitch = Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            
            return new Vector2((float) pitch, (float) yaw);
        }
    }
    
    enum TransitionType {
        PAN, ROTATE, FOCUS
    }
    
    // ========================================================================
    // Data Types
    // ========================================================================
    
    /**
     * Easing functions for smooth transitions.
     */
    public enum Easing {
        LINEAR {
            @Override
            public float apply(float t) {
                return t;
            }
        },
        EASE_IN {
            @Override
            public float apply(float t) {
                return t * t;
            }
        },
        EASE_OUT {
            @Override
            public float apply(float t) {
                return 1 - (1 - t) * (1 - t);
            }
        },
        EASE_IN_OUT {
            @Override
            public float apply(float t) {
                return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
            }
        },
        CUBIC_IN {
            @Override
            public float apply(float t) {
                return t * t * t;
            }
        },
        CUBIC_OUT {
            @Override
            public float apply(float t) {
                return 1 - (float) Math.pow(1 - t, 3);
            }
        },
        CUBIC_IN_OUT {
            @Override
            public float apply(float t) {
                return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
            }
        };
        
        public abstract float apply(float t);
    }
    
    /**
     * Camera keyframe for sequences.
     */
    public record CameraKeyframe(
        Vector3 position,
        Vector2 rotation,
        float timeSeconds
    ) {
        public static CameraKeyframe at(Vector3 position, Vector2 rotation, float timeSeconds) {
            return new CameraKeyframe(position, rotation, timeSeconds);
        }
    }
    
    /**
     * Camera sequence with multiple keyframes.
     */
    public static class CameraSequence {
        private final List<CameraKeyframe> keyframes;
        private final Easing easing;
        private final boolean letterboxEnabled;
        private final Vector3 focusTarget;
        
        private CameraSequence(Builder builder) {
            this.keyframes = new ArrayList<>(builder.keyframes);
            this.easing = builder.easing;
            this.letterboxEnabled = builder.letterboxEnabled;
            this.focusTarget = builder.focusTarget;
        }
        
        public List<CameraKeyframe> getKeyframes() {
            return Collections.unmodifiableList(keyframes);
        }
        
        public Easing getEasing() {
            return easing;
        }
        
        public boolean isLetterboxEnabled() {
            return letterboxEnabled;
        }
        
        public Vector3 getFocusTarget() {
            return focusTarget;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final List<CameraKeyframe> keyframes = new ArrayList<>();
            private Easing easing = Easing.EASE_IN_OUT;
            private boolean letterboxEnabled = true;
            private Vector3 focusTarget;
            
            public Builder addKeyframe(CameraKeyframe keyframe) {
                keyframes.add(keyframe);
                return this;
            }
            
            public Builder withEasing(Easing easing) {
                this.easing = easing;
                return this;
            }
            
            public Builder withLetterbox(boolean enabled) {
                this.letterboxEnabled = enabled;
                return this;
            }
            
            public Builder withFocusTarget(Vector3 target) {
                this.focusTarget = target;
                return this;
            }
            
            public CameraSequence build() {
                if (keyframes.size() < 2) {
                    throw new IllegalStateException("CameraSequence requires at least 2 keyframes");
                }
                // Sort by time
                keyframes.sort(Comparator.comparing(CameraKeyframe::timeSeconds));
                return new CameraSequence(this);
            }
        }
    }
}
