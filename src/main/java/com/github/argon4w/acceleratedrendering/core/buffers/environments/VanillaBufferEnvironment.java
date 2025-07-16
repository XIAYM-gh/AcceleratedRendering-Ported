package com.github.argon4w.acceleratedrendering.core.buffers.environments;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.EmptyServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexFormatMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.MeshUploadingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.TransformProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.extras.IExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.programs.processing.IPolygonProcessor;
import com.github.argon4w.acceleratedrendering.core.programs.processing.LoadPolygonProcessorEvent;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;

import java.util.Set;

public class VanillaBufferEnvironment implements IBufferEnvironment {

    private final VertexFormat vertexFormat;
    private final IMemoryLayout<VertexFormatElement> layout;

    private final MeshUploadingProgramDispatcher meshUploadingProgramDispatcher;
    private final TransformProgramDispatcher transformProgramDispatcher;
    private final ICullingProgramSelector cullingProgramSelector;
    private final IPolygonProcessor polygonProcessor;

    public VanillaBufferEnvironment(
            VertexFormat vertexFormat,
            ResourceLocation meshUploadingProgramKey,
            ResourceLocation transformProgramKey
    ) {
        this.vertexFormat = vertexFormat;
        this.layout = new VertexFormatMemoryLayout(vertexFormat);

        this.meshUploadingProgramDispatcher = new MeshUploadingProgramDispatcher(meshUploadingProgramKey);
        this.transformProgramDispatcher = new TransformProgramDispatcher(transformProgramKey);
        this.cullingProgramSelector = ModLoader.postEventWithReturn(new LoadCullingProgramSelectorEvent(this.vertexFormat))
                .getSelector();
        this.polygonProcessor = ModLoader.postEventWithReturn(new LoadPolygonProcessorEvent(this.vertexFormat))
                .getProcessor();
    }

    @Override
    public void setupBufferState() {
        vertexFormat.setupBufferState();
    }

    @Override
    public Set<VertexFormat> getVertexFormats() {
        return Set.of(vertexFormat);
    }

    @Override
    public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
        return cullingProgramSelector.getExtraVertex(mode);
    }

    @Override
    public IMemoryLayout<VertexFormatElement> getLayout() {
        return layout;
    }

    @Override
    public MeshUploadingProgramDispatcher selectMeshUploadingProgramDispatcher() {
        return meshUploadingProgramDispatcher;
    }

    @Override
    public TransformProgramDispatcher selectTransformProgramDispatcher() {
        return transformProgramDispatcher;
    }

    @Override
    public IPolygonProgramDispatcher selectCullProgramDispatcher(RenderType renderType) {
        return cullingProgramSelector.select(renderType);
    }

    @Override
    public IPolygonProgramDispatcher selectProcessingProgramDispatcher(VertexFormat.Mode mode) {
        return polygonProcessor.select(mode);
    }

    @Override
    public boolean isAccelerated(VertexFormat vertexFormat) {
        return this.vertexFormat == vertexFormat;
    }

    @Override
    public int getVertexSize() {
        return vertexFormat.getVertexSize();
    }
}
