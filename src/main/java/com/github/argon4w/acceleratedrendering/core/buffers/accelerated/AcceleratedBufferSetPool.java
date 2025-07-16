package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.Sync;
import com.github.argon4w.acceleratedrendering.core.backends.VertexArray;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.*;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;
import java.util.Map;

import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER;

public class AcceleratedBufferSetPool {

    private final IBufferEnvironment bufferEnvironment;

    private BufferSet[] bufferSets;
    private int size;

    public AcceleratedBufferSetPool(IBufferEnvironment bufferEnvironment) {
        this.bufferEnvironment = bufferEnvironment;
        this.size = CoreFeature.getPooledBufferSetSize();
        this.bufferSets = new BufferSet[this.size];

        for (var i = 0; i < this.size; i++) {
            this.bufferSets[i] = new BufferSet();
        }
    }

    public BufferSet getBufferSet(boolean force) {
        for (var i = 0; i < size; i++) {
            var buffer = bufferSets[i];

            if (buffer.isFree()) {
                buffer.setUsed();
                return buffer;
            }
        }

        var index = 0;

        if (force) {
            index = size;
            size = size * 2;
            bufferSets = Arrays.copyOf(bufferSets, size);

            for (var i = index; i < size; i++) {
                bufferSets[i] = new BufferSet();
            }
        }

        var bufferSet = bufferSets[index];
        bufferSet.waitSync();
        bufferSet.setUsed();

        return bufferSet;
    }

    public class BufferSet {

        public static final int VERTEX_BUFFER_OUT_INDEX = 1;
        public static final int SHARING_BUFFER_INDEX = 2;
        public static final int VARYING_BUFFER_OUT_INDEX = 4;

        private final MeshUploaderPool meshUploaderPool;
        private final DrawContextPool drawContextPool;
        private final ElementBufferPool elementBufferPool;
        private final MappedBuffer sharingBuffer;
        private final StagingBufferPool varyingBuffer;
        private final StagingBufferPool vertexBuffer;
        private final VertexArray vertexArray;
        private final Sync sync;
        private final MutableInt sharing;
        @Getter
        private final Map<RenderType, AcceleratedBufferBuilder> builders;

        private boolean used;
        private IMemoryLayout<VertexFormatElement> layout;

        public BufferSet() {
            var size = CoreFeature.getPooledElementBufferSize();
            this.meshUploaderPool = new MeshUploaderPool(this);
            this.drawContextPool = new DrawContextPool(size);
            this.elementBufferPool = new ElementBufferPool(size);
            this.sharingBuffer = new MappedBuffer(64L);
            this.varyingBuffer = new StagingBufferPool(size, this);
            this.vertexBuffer = new StagingBufferPool(size, this);
            this.vertexArray = new VertexArray();
            this.sync = new Sync();
            this.sharing = new MutableInt(0);
            this.builders = new Object2ObjectLinkedOpenHashMap<>();

            this.used = false;
        }

        public void reset() {
            vertexArray.unbindVertexArray();
            meshUploaderPool.reset();
            drawContextPool.reset();
            elementBufferPool.reset();
            varyingBuffer.reset();
            sharingBuffer.reset();
            vertexBuffer.reset();
            sharing.setValue(0);
            builders.clear();
        }

        public void bindTransformBuffers() {
            vertexBuffer.getBufferOut().bindBase(GL_SHADER_STORAGE_BUFFER, VERTEX_BUFFER_OUT_INDEX);
            varyingBuffer.getBufferOut().bindBase(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_OUT_INDEX);
            sharingBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, SHARING_BUFFER_INDEX);
        }

        public void bindDrawBuffers() {
            vertexArray.bindVertexArray();
            drawContextPool.getContext().bind(GL_DRAW_INDIRECT_BUFFER);

            if (!bufferEnvironment.getLayout().equals(layout)
                    || elementBufferPool.getElementBufferOut().isResized()
                    || vertexBuffer.getBufferOut().isResized()) {
                layout = bufferEnvironment.getLayout();
                elementBufferPool.getElementBufferOut().bind(GL_ELEMENT_ARRAY_BUFFER);
                elementBufferPool.getElementBufferOut().resetResized();
                vertexBuffer.getBufferOut().bind(GL_ARRAY_BUFFER);
                vertexBuffer.getBufferOut().resetResized();
                bufferEnvironment.setupBufferState();
            }
        }

        public void prepare() {
            vertexBuffer.prepare();
            elementBufferPool.prepare();
        }

        public MeshUploaderPool.MeshUploader getMeshUploader() {
            return meshUploaderPool.get();
        }

        public StagingBufferPool.StagingBuffer getVertexBuffer() {
            return vertexBuffer.get();
        }

        public StagingBufferPool.StagingBuffer getVaryingBuffer() {
            return varyingBuffer.get();
        }

        public ElementBufferPool.ElementSegment getElementSegment() {
            return elementBufferPool.get();
        }

        public DrawContextPool.IndirectDrawContext getDrawContext() {
            return drawContextPool.get();
        }

        public IServerBuffer getElementBuffer() {
            return elementBufferPool.getElementBufferOut();
        }

        public long getVertexSize() {
            return bufferEnvironment.getVertexSize();
        }

        public int getSharing() {
            return sharing.getAndIncrement();
        }

        public long reserveSharing() {
            return sharingBuffer.reserve(4L * 4L * 4L + 4L * 4L * 3L);
        }

        public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
            return bufferEnvironment.getExtraVertex(mode);
        }

        public IMemoryLayout<VertexFormatElement> getLayout() {
            return bufferEnvironment.getLayout();
        }

        public void setUsed() {
            used = true;
        }

        public void setInFlight() {
            used = false;
            sync.setSync();
        }

        protected void waitSync() {
            if (!sync.isSyncSet()) {
                return;
            }

            sync.waitSync();
            sync.resetSync();
        }

        public boolean isFree() {
            if (used) {
                return false;
            }

            if (!sync.isSyncSet()) {
                return true;
            }

            if (!sync.isSyncSignaled()) {
                return false;
            }

            sync.resetSync();

            return true;
        }
    }
}
