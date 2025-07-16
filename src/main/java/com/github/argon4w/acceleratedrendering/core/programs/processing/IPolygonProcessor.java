package com.github.argon4w.acceleratedrendering.core.programs.processing;

import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface IPolygonProcessor {

    IPolygonProgramDispatcher select(VertexFormat.Mode mode);

    IExtraVertexData getExtraVertex(VertexFormat.Mode mode);
}
