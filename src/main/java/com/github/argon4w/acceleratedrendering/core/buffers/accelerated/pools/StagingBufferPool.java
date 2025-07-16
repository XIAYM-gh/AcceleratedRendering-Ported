package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableLong;

import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

public class StagingBufferPool extends SimpleResetPool<StagingBufferPool.StagingBuffer, Void> {

    private final AcceleratedBufferSetPool.BufferSet bufferSet;
    @Getter
    private final MutableBuffer bufferOut;
    private final MutableLong bufferSegments;
    private final MutableLong bufferOutSize;
    private final MutableLong bufferOutUsedSize;

    public StagingBufferPool(int size, AcceleratedBufferSetPool.BufferSet bufferSet) {
        super(size, null);

        this.bufferSet = bufferSet;
        this.bufferOut = new MutableBuffer(64L * size, GL_DYNAMIC_STORAGE_BIT);
        this.bufferSegments = new MutableLong(0L);
        this.bufferOutSize = new MutableLong(64L * size);
        this.bufferOutUsedSize = new MutableLong(0L);
    }

    public void prepare() {
        bufferOut.resizeTo(bufferOutSize.getValue());
        bufferSegments.setValue(0L);
    }

    @Override
    public void delete() {
        bufferOut.delete();
        super.delete();
    }

    @Override
    public void reset() {
        bufferOutUsedSize.setValue(0L);
        super.reset();
    }

    @Override
    protected StagingBuffer create(Void context, int i) {
        return new StagingBuffer();
    }

    @Override
    protected void reset(StagingBuffer stagingBuffer) {
        stagingBuffer.poolReset();
    }

    @Override
    protected void delete(StagingBuffer stagingBuffer) {
        stagingBuffer.poolDelete();
    }

    @Override
    public boolean test(StagingBuffer stagingBuffer) {
        return bufferOutUsedSize.addAndGet(stagingBuffer.getSize()) <= (2L * 1024L * 1024L * 1024L);
    }

    @Getter
    public class StagingBuffer extends MappedBuffer {

        private long offset;

        public StagingBuffer() {
            super(64L);
            this.offset = -1;
        }

        @Override
        public void onExpand(long bytes) {
            bufferOutSize.add(bytes);
            bufferOutUsedSize.add(bytes);
        }

        @Override
        public void delete() {
            throw new IllegalStateException("Pooled buffers cannot be deleted directly.");
        }

        @Override
        public void reset() {
            throw new IllegalStateException("Pooled buffers cannot be reset directly.");
        }

        private void poolDelete() {
            super.delete();
        }

        private void poolReset() {
            super.reset();
        }

        public void allocateOffset() {
            offset = bufferSegments.getAndAdd(position / bufferSet.getVertexSize());
        }
    }
}
