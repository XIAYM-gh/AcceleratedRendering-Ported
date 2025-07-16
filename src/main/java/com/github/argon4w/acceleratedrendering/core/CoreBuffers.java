package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.core.buffers.AcceleratedBufferSources;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.mojang.blaze3d.vertex.VertexFormat;

public class CoreBuffers {

    public static final AcceleratedBufferSource BLOCK = new AcceleratedBufferSource(IBufferEnvironment.Presets.BLOCK);
    public static final AcceleratedBufferSource ENTITY = new AcceleratedBufferSource(IBufferEnvironment.Presets.ENTITY);
    public static final AcceleratedBufferSource POS = new AcceleratedBufferSource(IBufferEnvironment.Presets.POS);
    public static final AcceleratedBufferSource POS_TEX = new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_TEX);
    public static final AcceleratedBufferSource POS_TEX_COLOR = new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_TEX_COLOR);
    public static final AcceleratedBufferSource POS_COLOR_TEX_LIGHT = new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_COLOR_TEX_LIGHT);
    public static final AcceleratedBufferSource POS_TEX_COLOR_OUTLINE = new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_TEX_COLOR);

    public static final AcceleratedBufferSources CORE = AcceleratedBufferSources
            .builder()
            .source(BLOCK)
            .source(ENTITY)
            .source(POS)
            .source(POS_TEX)
            .source(POS_TEX_COLOR)
            .source(POS_COLOR_TEX_LIGHT)
            .mode(VertexFormat.Mode.QUADS)
            .mode(VertexFormat.Mode.TRIANGLES)
            .invalid("breeze_wind")
            .invalid("energy_swirl")
            .build();

    public static final AcceleratedBufferSources OUTLINE = AcceleratedBufferSources
            .builder()
            .source(POS_TEX_COLOR_OUTLINE)
            .mode(VertexFormat.Mode.QUADS)
            .mode(VertexFormat.Mode.TRIANGLES)
            .invalid("breeze_wind")
            .invalid("energy_swirl")
            .build();

    public static AcceleratedBufferSources getCoreBufferSourceSet() {
        return CORE;
    }
}
