package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import com.github.argon4w.acceleratedrendering.core.utils.MutableSize;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableLong;

import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

public class ElementBufferPool extends SimpleResetPool<ElementBufferPool.ElementSegment, Void> {

    @Getter
    private final MutableBuffer elementBufferOut;
    private final MutableLong elementBufferSegments;
    private final MutableLong elementBufferOutSize;
    private final MutableLong elementBufferOutUsedSize;

    public ElementBufferPool(int size) {
        super(size, null);

        this.elementBufferOut = new MutableBuffer(64L * size, GL_DYNAMIC_STORAGE_BIT);
        this.elementBufferSegments = new MutableLong(0L);
        this.elementBufferOutSize = new MutableLong(64L * size);
        this.elementBufferOutUsedSize = new MutableLong(64L * size);
    }

    public void prepare() {
        elementBufferOut.resizeTo(elementBufferOutSize.getValue());
        elementBufferSegments.setValue(0L);
    }

    @Override
    public void delete() {
        elementBufferOut.delete();
        super.delete();
    }

    @Override
    public void reset() {
        elementBufferOutUsedSize.setValue(0L);
        super.reset();
    }

    @Override
    protected ElementSegment create(Void value, int i) {
        return new ElementSegment();
    }

    @Override
    protected void reset(ElementSegment elementSegment) {
        elementSegment.reset();
    }

    @Override
    protected void delete(ElementSegment elementSegment) {

    }

    @Override
    public boolean test(ElementSegment elementSegment) {
        return elementBufferOutUsedSize.addAndGet(elementSegment.getSize()) <= (2L * 1024L * 1024L * 1024L);
    }

    @Getter
    public class ElementSegment extends MutableSize {

        private long bytes;
        private long offset;

        public ElementSegment() {
            super(64L);
            this.bytes = 0L;
            this.offset = -1L;
        }

        @Override
        public void onExpand(long bytes) {
            elementBufferOutSize.add(bytes);
            elementBufferOutUsedSize.add(bytes);
        }

        private void reset() {
            bytes = 0L;
        }

        public void allocateOffset() {
            offset = elementBufferSegments.getAndAdd(size);
        }

        public void countElements(int count) {
            bytes += count * 4L;

            if (bytes > size) {
                resize(bytes);
            }
        }
    }
}
