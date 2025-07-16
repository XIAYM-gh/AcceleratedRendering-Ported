package com.github.argon4w.acceleratedrendering.core.programs.extras;

public class EmptyExtraVertexData implements IExtraVertexData {

    public static final EmptyExtraVertexData INSTANCE = new EmptyExtraVertexData();

    @Override
    public void addExtraVertex(long address) {

    }

    @Override
    public void addExtraVarying(long address) {

    }
}
