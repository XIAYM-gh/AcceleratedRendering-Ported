package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import lombok.Getter;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class MappedBuffer extends MutableBuffer implements IClientBuffer {

    protected long address;
    @Getter
    protected long position;

    public MappedBuffer(long initialSize) {
        super(initialSize, GL_MAP_PERSISTENT_BIT
                | GL_MAP_WRITE_BIT
                | GL_MAP_COHERENT_BIT);

        this.address = map();
        this.position = 0L;
    }

    @Override
    public long reserve(long bytes) {
        var position = this.position;
        this.position = position + bytes;

        if (this.position <= size) {
            return address + position;
        }

        resize(this.position);
        return address + position;
    }

    @Override
    public void data(ByteBuffer data) {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    @Override
    public long addressAt(long position) {
        return address + position;
    }

    @Override
    public void beforeExpand() {
        unmap();
    }

    @Override
    public void afterExpand() {
        address = map();
    }

    public long map() {
        return map(GL_MAP_WRITE_BIT
                | GL_MAP_PERSISTENT_BIT
                | GL_MAP_COHERENT_BIT
                | GL_MAP_UNSYNCHRONIZED_BIT);
    }

    public void reset() {
        position = 0;
    }
}
