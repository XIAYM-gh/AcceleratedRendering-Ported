package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class FixedPolygonProgramDispatcher implements IPolygonProgramDispatcher {

    public static final int VARYING_BUFFER_INDEX = 3;
    private static final int GROUP_SIZE = 128;
    private static final int DISPATCH_COUNT_Y_Z = 1;

    private final VertexFormat.Mode mode;
    private final ComputeProgram program;
    private final Uniform polygonCountUniform;
    private final Uniform vertexOffsetUniform;

    public FixedPolygonProgramDispatcher(VertexFormat.Mode mode, ComputeProgram program) {
        this.mode = mode;
        this.program = program;
        this.polygonCountUniform = this.program.getUniform("polygonCount");
        this.vertexOffsetUniform = this.program.getUniform("vertexOffset");
    }

    public FixedPolygonProgramDispatcher(VertexFormat.Mode mode, ResourceLocation key) {
        this(mode, ComputeShaderProgramLoader.getProgram(key));
    }

    @Override
    public int dispatch(AcceleratedBufferBuilder builder) {
        var vertexCount = builder.getTotalVertexCount();
        var polygonCount = vertexCount / mode.primitiveLength;

        builder.getVaryingBuffer().bindBase(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_INDEX);

        polygonCountUniform.uploadUnsignedInt(polygonCount);
        vertexOffsetUniform.uploadUnsignedInt((int) builder.getVertexBuffer().getOffset());

        program.useProgram();
        program.dispatch(
                (polygonCount + GROUP_SIZE - 1) / GROUP_SIZE,
                DISPATCH_COUNT_Y_Z,
                DISPATCH_COUNT_Y_Z
        );
        program.resetProgram();

        return program.getBarrierFlags();
    }
}
