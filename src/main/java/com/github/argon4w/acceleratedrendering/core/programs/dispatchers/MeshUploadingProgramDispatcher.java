package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public class MeshUploadingProgramDispatcher {

    private static final int GROUP_SIZE = 128;
    private static final int DISPATCH_COUNT_Y_Z = 1;

    private final ComputeProgram program;
    private final Uniform meshCountUniform;
    private final Uniform meshSizeUniform;
    private final Uniform vertexOffsetUniform;
    private final Uniform varyingOffsetUniform;

    public MeshUploadingProgramDispatcher(ResourceLocation key) {
        this.program = ComputeShaderProgramLoader.getProgram(key);
        this.meshCountUniform = program.getUniform("meshCount");
        this.meshSizeUniform = program.getUniform("meshSize");
        this.vertexOffsetUniform = program.getUniform("vertexOffset");
        this.varyingOffsetUniform = program.getUniform("varyingOffset");
    }

    public void dispatch(Collection<AcceleratedBufferBuilder> builders) {
        program.useProgram();

        for (var builder : builders) {
            var meshVertexCount = builder.getMeshVertexCount();
            var vertexBuffer = builder.getVertexBuffer();
            var varyingBuffer = builder.getVaryingBuffer();

            vertexBuffer.reserve(meshVertexCount * builder.getVertexSize());
            varyingBuffer.reserve(meshVertexCount * AcceleratedBufferBuilder.VARYING_SIZE);
            vertexBuffer.allocateOffset();
            varyingBuffer.allocateOffset();

            var vertexOffset = builder.getVertexCount() + vertexBuffer.getOffset();
            var varyingOffset = builder.getVertexCount() + varyingBuffer.getOffset();

            for (var uploader : builder.getMeshUploaders().values()) {
                var mesh = uploader.getServerMesh();
                var meshCount = (int) uploader.getMeshCount();
                var meshSize = (int) mesh.size();

                builder.getElementSegment().countElements(builder.getMode().indexCount(meshSize * meshCount));
                uploader.bindUploadBuffers();

                meshSizeUniform.uploadUnsignedInt(meshSize);
                meshCountUniform.uploadUnsignedInt(meshCount);
                vertexOffsetUniform.uploadUnsignedInt((int) vertexOffset);
                varyingOffsetUniform.uploadUnsignedInt((int) varyingOffset);

                program.dispatch(
                        (meshCount * meshSize + GROUP_SIZE - 1) / GROUP_SIZE,
                        DISPATCH_COUNT_Y_Z,
                        DISPATCH_COUNT_Y_Z
                );

                vertexOffset += uploader.getUploadSize();
                varyingOffset += uploader.getUploadSize();
            }
        }

        program.resetProgram();
        program.waitBarriers();
    }
}
