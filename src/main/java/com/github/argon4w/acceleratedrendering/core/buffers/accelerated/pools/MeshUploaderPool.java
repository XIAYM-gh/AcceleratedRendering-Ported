package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import lombok.Getter;
import net.minecraft.util.FastColor;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class MeshUploaderPool extends SimpleResetPool<MeshUploaderPool.MeshUploader, AcceleratedBufferSetPool.BufferSet> {

    public MeshUploaderPool(AcceleratedBufferSetPool.BufferSet bufferSet) {
        super(128, bufferSet);
    }

    @Override
    protected MeshUploader create(AcceleratedBufferSetPool.BufferSet context, int i) {
        return new MeshUploader(context);
    }

    @Override
    protected void reset(MeshUploader meshUploader) {
        meshUploader.reset();
    }

    @Override
    protected void delete(MeshUploader meshUploader) {
        meshUploader.delete();
    }

    @Override
    public MeshUploader fail() {
        expand();
        return get();
    }

    public static class MeshUploader {

        public static final int MESH_BUFFER_INDEX = 7;
        public static final int MESH_INFO_BUFFER_INDEX = 8;
        public static final int EXTRA_VERTEX_BUFFER_INDEX = 9;
        public static final int EXTRA_VARYING_BUFFER_INDEX = 10;

        public static final long MESH_INFO_SIZE = 4L * 4L;
        public static final IMemoryInterface MESH_INFO_SHARING = new SimpleMemoryInterface(0L * 4L, MESH_INFO_SIZE);
        public static final IMemoryInterface MESH_INFO_COLOR = new SimpleMemoryInterface(1L * 4L, MESH_INFO_SIZE);
        public static final IMemoryInterface MESH_INFO_UV1 = new SimpleMemoryInterface(2L * 4L, MESH_INFO_SIZE);
        public static final IMemoryInterface MESH_INFO_UV2 = new SimpleMemoryInterface(3L * 4L, MESH_INFO_SIZE);

        private final AcceleratedBufferSetPool.BufferSet bufferSet;
        private final MappedBuffer meshInfoBuffer;
        private final MappedBuffer extraVertexBuffer;
        private final MappedBuffer extraVaryingBuffer;

        private IMemoryLayout<VertexFormatElement> layout;
        private VertexFormat.Mode mode;
        @Getter
        private ServerMesh serverMesh;
        @Getter
        private long meshCount;

        public MeshUploader(AcceleratedBufferSetPool.BufferSet bufferSet) {
            this.bufferSet = bufferSet;
            this.meshInfoBuffer = new MappedBuffer(64L);
            this.extraVertexBuffer = new MappedBuffer(64L);
            this.extraVaryingBuffer = new MappedBuffer(64L);

            this.layout = null;
            this.mode = null;
            this.serverMesh = null;
            this.meshCount = 0L;
        }

        public void set(
                IMemoryLayout<VertexFormatElement> layout,
                VertexFormat.Mode mode,
                ServerMesh serverMesh
        ) {
            this.layout = layout;
            this.mode = mode;
            this.serverMesh = serverMesh;
        }

        public void addUpload(
                int color,
                int light,
                int overlay,
                int sharing
        ) {
            var meshInfoAddress = meshInfoBuffer.reserve(MESH_INFO_SIZE);
            var extraVertexAddress = extraVertexBuffer.reserve(layout.getSize());
            var extraVaryingAddress = extraVaryingBuffer.reserve(AcceleratedBufferBuilder.VARYING_SIZE);
            var data = bufferSet.getExtraVertex(mode);

            data.addExtraVertex(extraVertexAddress);
            data.addExtraVarying(extraVaryingAddress);

            MESH_INFO_SHARING.putInt(meshInfoAddress, sharing);
            MESH_INFO_COLOR.putInt(meshInfoAddress, FastColor.ABGR32.fromArgb32(color));
            MESH_INFO_UV1.putInt(meshInfoAddress, overlay);
            MESH_INFO_UV2.putInt(meshInfoAddress, light);

            meshCount++;
        }

        public void bindUploadBuffers() {
            serverMesh.meshBuffer().bindBase(GL_SHADER_STORAGE_BUFFER, MESH_BUFFER_INDEX);
            meshInfoBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, MESH_INFO_BUFFER_INDEX);
            extraVertexBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, EXTRA_VERTEX_BUFFER_INDEX);
            extraVaryingBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, EXTRA_VARYING_BUFFER_INDEX);
        }

        public void reset() {
            meshInfoBuffer.reset();
            extraVertexBuffer.reset();
            extraVaryingBuffer.reset();

            layout = null;
            mode = null;
            serverMesh = null;
            meshCount = 0;
        }

        public void delete() {
            meshInfoBuffer.delete();
            extraVertexBuffer.delete();
            extraVaryingBuffer.delete();
        }

        public long getUploadSize() {
            return meshCount * serverMesh.size();
        }
    }
}
