package com.github.argon4w.acceleratedrendering.compat.iris.programs.processing;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexFormatMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;

public class IrisExtraVertexData implements IExtraVertexData {

    private final IMemoryInterface entityOffset;
    private final IMemoryInterface entityIdOffset;

    public IrisExtraVertexData(VertexFormat vertexFormat) {
        var layout = new VertexFormatMemoryLayout(vertexFormat);
        this.entityOffset = layout.getElement(IrisVertexFormats.ENTITY_ELEMENT);
        this.entityIdOffset = layout.getElement(IrisVertexFormats.ENTITY_ID_ELEMENT);
    }

    @Override
    public void addExtraVertex(long address) {
        entityOffset.putShort(address + 0L, (short) -1);
        entityOffset.putShort(address + 2L, (short) -1);
        entityIdOffset.putShort(address + 0L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
        entityIdOffset.putShort(address + 2L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
        entityIdOffset.putShort(address + 4L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
    }

    @Override
    public void addExtraVarying(long address) {

    }
}
