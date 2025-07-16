package com.github.argon4w.acceleratedrendering.core.programs.extras;

public class CompositeExtraVertex implements IExtraVertexData {

    private final IExtraVertexData left;
    private final IExtraVertexData right;

    public CompositeExtraVertex(IExtraVertexData left, IExtraVertexData right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void addExtraVertex(long address) {
        left.addExtraVertex(address);
        right.addExtraVertex(address);
    }

    @Override
    public void addExtraVarying(long address) {
        left.addExtraVarying(address);
        right.addExtraVarying(address);
    }
}
