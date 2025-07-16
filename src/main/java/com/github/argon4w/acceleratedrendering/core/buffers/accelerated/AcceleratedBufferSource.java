package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.DrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL46.*;

public class AcceleratedBufferSource extends MultiBufferSource.BufferSource implements IAcceleratedBufferSource {

    private final IBufferEnvironment bufferEnvironment;
    private final AcceleratedBufferSetPool acceleratedBufferSetPool;
    private final Set<AcceleratedBufferSetPool.BufferSet> bufferSets;
    private final Map<RenderType, DrawContextPool.IndirectDrawContext> drawContexts;

    private AcceleratedBufferSetPool.BufferSet currentBufferSet;
    private boolean used;

    public AcceleratedBufferSource(IBufferEnvironment bufferEnvironment) {
        super(null, null);

        this.bufferEnvironment = bufferEnvironment;
        this.acceleratedBufferSetPool = new AcceleratedBufferSetPool(this.bufferEnvironment);
        this.currentBufferSet = this.acceleratedBufferSetPool.getBufferSet(false);
        this.bufferSets = ObjectLinkedOpenHashSet.of(this.currentBufferSet);
        this.drawContexts = new Object2ObjectLinkedOpenHashMap<>();
        this.used = false;
    }

    @Override
    public void endLastBatch() {

    }

    @Override
    public void endBatch() {

    }

    @Override
    public void endBatch(RenderType pRenderType) {

    }

    @Override
    public IBufferEnvironment getBufferEnvironment() {
        return bufferEnvironment;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        var builders = currentBufferSet.getBuilders();
        var builder = builders.get(renderType);

        if (builder != null) {
            return builder;
        }

        var vertexBuffer = currentBufferSet.getVertexBuffer();
        var varyingBuffer = currentBufferSet.getVaryingBuffer();
        var elementSegment = currentBufferSet.getElementSegment();

        if (vertexBuffer == null) {
            currentBufferSet = acceleratedBufferSetPool.getBufferSet(true);
            builders = currentBufferSet.getBuilders();
            vertexBuffer = currentBufferSet.getVertexBuffer();
            varyingBuffer = currentBufferSet.getVaryingBuffer();
            elementSegment = currentBufferSet.getElementSegment();

            bufferSets.add(currentBufferSet);
        }

        builder = new AcceleratedBufferBuilder(
                vertexBuffer,
                varyingBuffer,
                elementSegment,
                currentBufferSet,
                renderType
        );

        used = true;
        builders.put(renderType, builder);

        return builder;
    }

    @Override
    public void drawBuffers() {
        if (!used) {
            return;
        }

        for (var bufferSet : bufferSets) {
            var builders = bufferSet.getBuilders();
            var program = glGetInteger(GL_CURRENT_PROGRAM);
            var barrier = 0;

            if (builders.isEmpty()) {
                continue;
            }

            bufferSet.prepare();
            bufferSet.bindTransformBuffers();
            bufferEnvironment.selectMeshUploadingProgramDispatcher().dispatch(builders.values());
            bufferEnvironment.selectTransformProgramDispatcher().dispatch(builders.values());

            for (var renderType : builders.keySet()) {
                var builder = builders.get(renderType);
                var elementSegment = builder.getElementSegment();

                if (builder.isEmpty()) {
                    continue;
                }

                var mode = renderType.mode;
                var drawContext = bufferSet.getDrawContext();

                drawContext.bindComputeBuffers(bufferSet.getElementBuffer(), elementSegment);
                drawContexts.put(renderType, drawContext);

                barrier |= bufferEnvironment.selectProcessingProgramDispatcher(mode).dispatch(builder);
                barrier |= bufferEnvironment.selectCullProgramDispatcher(renderType).dispatch(builder);
            }

            glMemoryBarrier(barrier);
            glUseProgram(program);
            BufferUploader.invalidate();
            bufferSet.bindDrawBuffers();

            for (var renderType : drawContexts.keySet()) {
                renderType.setupRenderState();

                var drawContext = drawContexts.get(renderType);
                var shader = RenderSystem.getShader();
                var mode = renderType.mode;

                shader.setDefaultUniforms(
                        mode,
                        RenderSystem.getModelViewMatrix(),
                        RenderSystem.getProjectionMatrix(),
                        Minecraft.getInstance().getWindow()
                );

                shader.apply();
                drawContext.drawElements(mode);
                shader.clear();
                renderType.clearRenderState();
            }

            glMemoryBarrier(GL_ELEMENT_ARRAY_BARRIER_BIT | GL_COMMAND_BARRIER_BIT);
            bufferSet.reset();
            bufferSet.setInFlight();
            drawContexts.clear();
        }

        currentBufferSet = acceleratedBufferSetPool.getBufferSet(false);
        used = false;

        bufferSets.add(currentBufferSet);
    }
}
