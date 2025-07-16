package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER;

public class TransformProgramDispatcher {

    public static final int VERTEX_BUFFER_IN_INDEX = 0;
    public static final int VARYING_BUFFER_INDEX = 3;
    private static final int GROUP_SIZE = 128;
    private static final int DISPATCH_COUNT_Y_Z = 1;

    private final ComputeProgram program;
    private final Uniform vertexCountUniform;
    private final Uniform vertexOffsetUniform;
    private final Uniform varyingOffsetUniform;

    public TransformProgramDispatcher(ResourceLocation key) {
        this.program = ComputeShaderProgramLoader.getProgram(key);
        this.vertexCountUniform = program.getUniform("vertexCount");
        this.vertexOffsetUniform = program.getUniform("vertexOffset");
        this.varyingOffsetUniform = program.getUniform("varyingOffset");
    }

    public void dispatch(Collection<AcceleratedBufferBuilder> builders) {
        program.useProgram();

        for (var builder : builders) {
            var vertexCount = builder.getVertexCount();
            var vertexBuffer = builder.getVertexBuffer();
            var varyingBuffer = builder.getVaryingBuffer();

            if (vertexCount != 0) {

                vertexBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, VERTEX_BUFFER_IN_INDEX);
                varyingBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_INDEX);

                vertexCountUniform.uploadUnsignedInt(vertexCount);
                vertexOffsetUniform.uploadUnsignedInt((int) vertexBuffer.getOffset());
                varyingOffsetUniform.uploadUnsignedInt((int) varyingBuffer.getOffset());

                program.dispatch(
                        (vertexCount + GROUP_SIZE - 1) / GROUP_SIZE,
                        DISPATCH_COUNT_Y_Z,
                        DISPATCH_COUNT_Y_Z
                );
            }
        }

        program.resetProgram();
        program.waitBarriers();
    }
}
