package com.github.argon4w.acceleratedrendering.core.programs.culling;

import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public interface ICullingProgramSelector {

    IPolygonProgramDispatcher select(RenderType renderType);

    IExtraVertexData getExtraVertex(VertexFormat.Mode mode);
}
