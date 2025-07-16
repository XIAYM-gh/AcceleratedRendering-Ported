package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import com.mojang.blaze3d.vertex.VertexFormat;

import static org.lwjgl.opengl.GL46.*;

public class DrawContextPool extends SimpleResetPool<DrawContextPool.IndirectDrawContext, MappedBuffer> {

    public DrawContextPool(int size) {
        super(size, new MappedBuffer(20L * size));
    }

    @Override
    protected IndirectDrawContext create(MappedBuffer buffer, int i) {
        return new IndirectDrawContext(i);
    }

    @Override
    protected void reset(IndirectDrawContext drawContext) {

    }

    @Override
    protected void delete(IndirectDrawContext drawContext) {

    }

    @Override
    public void delete() {
        super.getContext().delete();
    }

    @Override
    public IndirectDrawContext fail() {
        expand();
        return get();
    }

    public class IndirectDrawContext {

        public static final int ELEMENT_BUFFER_INDEX = 6;
        public static final int ELEMENT_COUNT_INDEX = 0;

        public static final IMemoryInterface INDIRECT_COUNT = new SimpleMemoryInterface(0L * 4L, 4);
        public static final IMemoryInterface INDIRECT_INSTANCE_COUNT = new SimpleMemoryInterface(1L * 4L, 4);
        public static final IMemoryInterface INDIRECT_FIRST_INDEX = new SimpleMemoryInterface(2L * 4L, 4);
        public static final IMemoryInterface INDIRECT_BASE_INDEX = new SimpleMemoryInterface(3L * 4L, 4);
        public static final IMemoryInterface INDIRECT_BASE_INSTANCE = new SimpleMemoryInterface(4L * 4L, 4);

        private final long commandOffset;

        public IndirectDrawContext(int index) {
            this.commandOffset = index * 20L;
            var address = context.reserve(20L);

            INDIRECT_COUNT.putInt(address, 0);
            INDIRECT_INSTANCE_COUNT.putInt(address, 1);
            INDIRECT_FIRST_INDEX.putInt(address, 0);
            INDIRECT_BASE_INDEX.putInt(address, 0);
            INDIRECT_BASE_INSTANCE.putInt(address, 0);
        }

        public void bindComputeBuffers(IServerBuffer elementBuffer, ElementBufferPool.ElementSegment elementSegmentIn) {
            elementSegmentIn.allocateOffset();

            var elementOffset = elementSegmentIn.getOffset();
            var elementSize = elementSegmentIn.getSize();
            var commandAddress = context.addressAt(commandOffset);

            INDIRECT_COUNT.putInt(commandAddress, 0);
            INDIRECT_FIRST_INDEX.putInt(commandAddress, (int) elementOffset / 4);

            elementBuffer.bindRange(
                    GL_SHADER_STORAGE_BUFFER,
                    ELEMENT_BUFFER_INDEX,
                    elementOffset,
                    elementSize
            );

            context.bindRange(
                    GL_ATOMIC_COUNTER_BUFFER,
                    ELEMENT_COUNT_INDEX,
                    commandOffset,
                    4
            );
        }

        public void drawElements(VertexFormat.Mode mode) {
            glDrawElementsIndirect(
                    mode.asGLMode,
                    GL_UNSIGNED_INT,
                    commandOffset
            );
        }
    }
}
