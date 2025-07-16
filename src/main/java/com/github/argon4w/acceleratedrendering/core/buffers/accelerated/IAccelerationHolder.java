package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;

public interface IAccelerationHolder {

    VertexConsumer initAcceleration(RenderType renderType);
}
