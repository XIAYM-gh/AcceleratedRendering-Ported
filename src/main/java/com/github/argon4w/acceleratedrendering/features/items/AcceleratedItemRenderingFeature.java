package com.github.argon4w.acceleratedrendering.features.items;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;
import com.github.argon4w.acceleratedrendering.configs.PipelineSetting;
import com.github.argon4w.acceleratedrendering.core.meshes.MeshType;

import java.util.ArrayDeque;
import java.util.Deque;

public class AcceleratedItemRenderingFeature {

    private static final Deque<PipelineSetting> PIPELINE_CONTROLLER_STACK = new ArrayDeque<>();
    private static final Deque<FeatureStatus> BAKE_QUAD_MESH_CONTROLLER_STACK = new ArrayDeque<>();

    public static boolean isEnabled() {
        return FeatureConfig.CONFIG.acceleratedItemRenderingFeatureStatus.get() == FeatureStatus.ENABLED;
    }

    public static boolean shouldUseAcceleratedPipeline() {
        return getPipelineSetting() == PipelineSetting.ACCELERATED;
    }

    public static boolean shouldBakeMeshForQuad() {
        return getBakeQuadMeshSetting() == FeatureStatus.ENABLED;
    }

    public static MeshType getMeshType() {
        return FeatureConfig.CONFIG.acceleratedItemRenderingMeshType.get();
    }

    public static void useVanillaPipeline() {
        PIPELINE_CONTROLLER_STACK.push(PipelineSetting.VANILLA);
    }

    public static void dontBakeMeshForQuad() {
        BAKE_QUAD_MESH_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
    }

    public static void forceUseAcceleratedPipeline() {
        PIPELINE_CONTROLLER_STACK.push(PipelineSetting.ACCELERATED);
    }

    public static void forceBakeMeshForQuad() {
        BAKE_QUAD_MESH_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
    }

    public static void forceSetPipeline(PipelineSetting pipeline) {
        PIPELINE_CONTROLLER_STACK.push(pipeline);
    }

    public static void forceSetBakeQuadMeshSetting(FeatureStatus status) {
        BAKE_QUAD_MESH_CONTROLLER_STACK.push(status);
    }

    public static void resetPipelineSetting() {
        PIPELINE_CONTROLLER_STACK.pop();
    }

    public static void resetBakeQuadMeshSetting() {
        BAKE_QUAD_MESH_CONTROLLER_STACK.pop();
    }

    public static PipelineSetting getPipelineSetting() {
        return PIPELINE_CONTROLLER_STACK.isEmpty() ? getDefaultPipelineSetting() : PIPELINE_CONTROLLER_STACK.peek();
    }

    public static FeatureStatus getBakeQuadMeshSetting() {
        return BAKE_QUAD_MESH_CONTROLLER_STACK.isEmpty() ? getDefaultBakeQuadMeshSetting() : BAKE_QUAD_MESH_CONTROLLER_STACK.peek();
    }

    public static PipelineSetting getDefaultPipelineSetting() {
        return FeatureConfig.CONFIG.acceleratedItemRenderingDefaultPipeline.get();
    }

    public static FeatureStatus getDefaultBakeQuadMeshSetting() {
        return FeatureConfig.CONFIG.acceleratedItemRenderingBakeMeshForQuads.get();
    }
}
