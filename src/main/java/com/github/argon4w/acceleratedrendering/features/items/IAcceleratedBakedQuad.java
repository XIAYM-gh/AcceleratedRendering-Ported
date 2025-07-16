package com.github.argon4w.acceleratedrendering.features.items;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public interface IAcceleratedBakedQuad {

    void renderFast(Matrix4f transform, Matrix3f normal, IAcceleratedVertexConsumer extension, int combinedLight, int combinedOverlay, int color);

    int getCustomColor(int color);
}
