package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IBufferDecorator {

    VertexConsumer decorate(VertexConsumer buffer);
}
