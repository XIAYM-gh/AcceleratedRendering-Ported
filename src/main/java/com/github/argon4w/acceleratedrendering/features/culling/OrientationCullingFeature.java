package com.github.argon4w.acceleratedrendering.features.culling;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;

import java.util.ArrayDeque;
import java.util.Deque;

public class OrientationCullingFeature {

    private static final Deque<FeatureStatus> CULLING_CONTROLLER_STACK = new ArrayDeque<>();

    public static boolean isEnabled() {
        return FeatureConfig.CONFIG.orientationCullingFeatureStatus.get() == FeatureStatus.ENABLED;
    }

    public static boolean shouldIgnoreCullState() {
        return FeatureConfig.CONFIG.orientationCullingIgnoreCullState.get() == FeatureStatus.ENABLED;
    }

    public static boolean shouldCull() {
        return getCullingSetting() == FeatureStatus.ENABLED;
    }

    public static void disableCulling() {
        CULLING_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
    }

    public static void forceEnableCulling() {
        CULLING_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
    }

    public static void forceSetCulling(FeatureStatus culling) {
        CULLING_CONTROLLER_STACK.push(culling);
    }

    public static void resetCullingSetting() {
        CULLING_CONTROLLER_STACK.pop();
    }

    public static FeatureStatus getCullingSetting() {
        return CULLING_CONTROLLER_STACK.isEmpty() ? getDefaultCullingSetting() : CULLING_CONTROLLER_STACK.peek();
    }

    public static FeatureStatus getDefaultCullingSetting() {
        return FeatureConfig.CONFIG.orientationCullingDefaultCulling.get();
    }
}
