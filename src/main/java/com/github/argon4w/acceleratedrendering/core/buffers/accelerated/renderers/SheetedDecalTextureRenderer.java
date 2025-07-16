package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedSheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SheetedDecalTextureRenderer<T> implements IAcceleratedRenderer<T> {

    private final IAcceleratedRenderer<T> renderer;
    private final Matrix4f cameraInverse;
    private final Matrix3f normalInverse;
    private final float textureScale;

    public SheetedDecalTextureRenderer(
            IAcceleratedRenderer<T> renderer,
            Matrix4f cameraInverse,
            Matrix3f normalInverse,
            float textureScale
    ) {
        this.renderer = renderer;
        this.cameraInverse = cameraInverse;
        this.normalInverse = normalInverse;
        this.textureScale = textureScale;
    }

    @Override
    public void render(
            VertexConsumer vertexConsumer,
            T context,
            Matrix4f transform,
            Matrix3f normal,
            int light,
            int overlay,
            int color
    ) {
        renderer.render(
                new AcceleratedSheetedDecalTextureGenerator(
                        vertexConsumer,
                        new Matrix4f(cameraInverse).mul(transform),
                        new Matrix3f(normalInverse).mul(normal),
                        textureScale
                ),
                context,
                transform,
                normal,
                light,
                overlay,
                color
        );
    }
}
