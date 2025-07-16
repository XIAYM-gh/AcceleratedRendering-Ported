package com.github.argon4w.acceleratedrendering.core.buffers.memory;

public interface IMemoryLayout<T> {

    IMemoryInterface getElement(T element);

    long getSize();
}
