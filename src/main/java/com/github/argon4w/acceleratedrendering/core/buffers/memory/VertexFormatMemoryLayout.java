package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VertexFormatMemoryLayout implements IMemoryLayout<VertexFormatElement> {

    private final VertexFormat vertexFormat;
    @EqualsAndHashCode.Include
    private final int hashCode;
    private final long size;
    private final IMemoryInterface[] byId;

    public VertexFormatMemoryLayout(VertexFormat vertexFormat) {
        var offsets = vertexFormat.getOffsetsByElement();
        var count = offsets.length;

        this.vertexFormat = vertexFormat;
        this.hashCode = vertexFormat.hashCode();
        this.size = vertexFormat.getVertexSize();
        this.byId = new IMemoryInterface[count];

        for (var i = 0; i < count; i++) {
            var offset = offsets[i];
            byId[i] = offset == -1 ? NullMemoryInterface.INSTANCE : new SimpleMemoryInterface(offset, size);
        }
    }

    @Override
    public IMemoryInterface getElement(VertexFormatElement element) {
        return byId[element.id()];
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return vertexFormat.toString();
    }
}
