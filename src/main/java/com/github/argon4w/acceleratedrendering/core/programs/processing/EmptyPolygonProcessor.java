package com.github.argon4w.acceleratedrendering.core.programs.processing;

import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.EmptyProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.extras.EmptyExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.mojang.blaze3d.vertex.VertexFormat;

public class EmptyPolygonProcessor implements IPolygonProcessor {

    public static final EmptyPolygonProcessor INSTANCE = new EmptyPolygonProcessor();

    @Override
    public IPolygonProgramDispatcher select(VertexFormat.Mode mode) {
        return EmptyProgramDispatcher.INSTANCE;
    }

    @Override
    public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
        return EmptyExtraVertexData.INSTANCE;
    }
}
