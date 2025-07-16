package com.github.argon4w.acceleratedrendering.features.entities;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;
import com.github.argon4w.acceleratedrendering.configs.PipelineSetting;
import com.github.argon4w.acceleratedrendering.core.meshes.MeshType;

import java.util.ArrayDeque;
import java.util.Deque;

public class AcceleratedEntityRenderingFeature {

    private static final Deque<PipelineSetting> PIPELINE_CONTROLLER_STACK = new ArrayDeque<>();

    public static boolean isEnabled() {
        return FeatureConfig.CONFIG.acceleratedEntityRenderingFeatureStatus.get() == FeatureStatus.ENABLED;
    }

    public static boolean shouldUseAcceleratedPipeline() {
        return getPipelineSetting() == PipelineSetting.ACCELERATED;
    }

    public static MeshType getMeshType() {
        return FeatureConfig.CONFIG.acceleratedEntityRenderingMeshType.get();
    }

    public static void useVanillaPipeline() {
        PIPELINE_CONTROLLER_STACK.push(PipelineSetting.VANILLA);
    }

    public static void forceUseAcceleratedPipeline() {
        PIPELINE_CONTROLLER_STACK.push(PipelineSetting.ACCELERATED);
    }

    public static void forceSetPipeline(PipelineSetting pipeline) {
        PIPELINE_CONTROLLER_STACK.push(pipeline);
    }

    public static void resetPipelineSetting() {
        PIPELINE_CONTROLLER_STACK.pop();
    }

    public static PipelineSetting getPipelineSetting() {
        return PIPELINE_CONTROLLER_STACK.isEmpty() ? getDefaultPipelineSetting() : PIPELINE_CONTROLLER_STACK.peek();
    }

    public static PipelineSetting getDefaultPipelineSetting() {
        return FeatureConfig.CONFIG.acceleratedEntityRenderingDefaultPipeline.get();
    }
}
