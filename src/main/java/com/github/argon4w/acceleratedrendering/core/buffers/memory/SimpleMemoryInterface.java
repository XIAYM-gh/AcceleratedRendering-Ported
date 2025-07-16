package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import com.github.argon4w.acceleratedrendering.core.utils.MemUtils;
import lombok.AllArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

@AllArgsConstructor
public class SimpleMemoryInterface implements IMemoryInterface {

    private final long offset;
    private final long size;

    @Override
    public void putByte(long address, byte value) {
        MemoryUtil.memPutByte(address + offset, value);
    }

    @Override
    public void putShort(long address, short value) {
        MemoryUtil.memPutShort(address + offset, value);
    }

    @Override
    public void putInt(long address, int value) {
        MemoryUtil.memPutInt(address + offset, value);
    }

    @Override
    public void putFloat(long address, float value) {
        MemoryUtil.memPutFloat(address + offset, value);
    }

    @Override
    public void putNormal(long address, float value) {
        MemUtils.putNormal(address + offset, value);
    }

    @Override
    public void putMatrix4f(long address, Matrix4f value) {
        MemUtils.putMatrix4f(address + offset, value);
    }

    @Override
    public void putMatrix3f(long address, Matrix3f value) {
        MemUtils.putMatrix3f(address + offset, value);
    }

    @Override
    public IMemoryInterface at(int index) {
        return new SimpleMemoryInterface(index * size + offset, 0);
    }
}
