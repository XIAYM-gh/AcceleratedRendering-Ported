package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public interface IMemoryInterface {

    void putByte(long address, byte value);

    void putShort(long address, short value);

    void putInt(long address, int value);

    void putFloat(long address, float value);

    void putNormal(long address, float value);

    void putMatrix4f(long address, Matrix4f value);

    void putMatrix3f(long address, Matrix3f value);

    IMemoryInterface at(int index);
}
