package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IAcceleratedBufferSource extends MultiBufferSource {

    IBufferEnvironment getBufferEnvironment();

    void drawBuffers();
}
