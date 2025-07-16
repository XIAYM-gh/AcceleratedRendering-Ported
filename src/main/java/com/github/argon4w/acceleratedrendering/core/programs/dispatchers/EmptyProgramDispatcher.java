package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;

public class EmptyProgramDispatcher implements IPolygonProgramDispatcher {

    public static final EmptyProgramDispatcher INSTANCE = new EmptyProgramDispatcher();

    @Override
    public int dispatch(AcceleratedBufferBuilder builder) {
        return 0;
    }
}
