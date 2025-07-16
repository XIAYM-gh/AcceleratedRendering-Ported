package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAccelerationHolder;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class VertexConsumerExtension {

    public static IAcceleratedVertexConsumer getAccelerated(VertexConsumer in) {
        return (IAcceleratedVertexConsumer) in;
    }

    public static IAccelerationHolder getHolder(VertexConsumer in) {
        return (IAccelerationHolder) in;
    }
}
