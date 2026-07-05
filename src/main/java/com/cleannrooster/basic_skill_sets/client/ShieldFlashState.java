package com.cleannrooster.basic_skill_sets.client;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShieldFlashState {
    /** Entity ID -> remaining flash ticks. */
    private static final Map<Integer, Integer> flashMap = new ConcurrentHashMap<>();

    /** Set true during HeldItemRenderer.renderItem for a flashing shield. */
    public static boolean renderingFlashingShield = false;
    /** Flash factor (1.0 = peak, 0.0 = done) for the entity currently being rendered. */
    public static float currentFlashFactor = 0f;

    public static void startFlash(int entityId) { flashMap.put(entityId, 10); }
    public static boolean isFlashing(int entityId) { return flashMap.getOrDefault(entityId, 0) > 0; }
    public static float getFlashFactor(int entityId) { return flashMap.getOrDefault(entityId, 0) / 10.0f; }

    public static void tick() {
        Iterator<Map.Entry<Integer, Integer>> it = flashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) it.remove();
            else entry.setValue(remaining);
        }
    }
    private static final ThreadLocal<Boolean> RENDERING_SHIELD =
            ThreadLocal.withInitial(() -> false);


    public static void push(boolean isShield) {
        RENDERING_SHIELD.set(isShield);
    }

    public static void pop() {
        RENDERING_SHIELD.set(false);
    }

    public static boolean isShieldContext() {
        return RENDERING_SHIELD.get();
    }
}
