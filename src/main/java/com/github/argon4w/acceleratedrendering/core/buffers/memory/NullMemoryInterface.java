package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class NullMemoryInterface implements IMemoryInterface {

    public static final NullMemoryInterface INSTANCE = new NullMemoryInterface();

    @Override
    public void putByte(long address, byte value) {

    }

    @Override
    public void putShort(long address, short value) {

    }

    @Override
    public void putInt(long address, int value) {

    }

    @Override
    public void putFloat(long address, float value) {

    }

    @Override
    public void putNormal(long address, float value) {

    }

    @Override
    public void putMatrix4f(long address, Matrix4f value) {

    }

    @Override
    public void putMatrix3f(long address, Matrix3f value) {

    }

    @Override
    public IMemoryInterface at(int index) {
        return INSTANCE;
    }
}
