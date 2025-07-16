package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public interface IAcceleratedRenderer<T> {

    void render(VertexConsumer vertexConsumer, T context, Matrix4f transform, Matrix3f normal, int light, int overlay, int color);
}
