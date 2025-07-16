package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.ElementBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AcceleratedBufferBuilder implements IAcceleratedVertexConsumer, VertexConsumer {

    public static final long VARYING_SIZE = 3L * 4L;
    public static final IMemoryInterface VARYING_OFFSET = new SimpleMemoryInterface(0L * 4L, VARYING_SIZE);
    public static final IMemoryInterface VARYING_SHARING = new SimpleMemoryInterface(1L * 4L, VARYING_SIZE);
    public static final IMemoryInterface VARYING_FLAGS = new SimpleMemoryInterface(2L * 4L, VARYING_SIZE);

    public static final long SHARING_SIZE = 4L * 4L * 4L + 4L * 3L * 4L;
    public static final IMemoryInterface SHARING_TRANSFORM = new SimpleMemoryInterface(0L, SHARING_SIZE);
    public static final IMemoryInterface SHARING_NORMAL = new SimpleMemoryInterface(4L * 4L * 4L, SHARING_SIZE);

    @Getter
    private final Map<ServerMesh, MeshUploaderPool.MeshUploader> meshUploaders;
    @Getter
    private final StagingBufferPool.StagingBuffer vertexBuffer;
    @Getter
    private final StagingBufferPool.StagingBuffer varyingBuffer;
    @Getter
    private final ElementBufferPool.ElementSegment elementSegment;
    private final AcceleratedBufferSetPool.BufferSet bufferSet;

    @EqualsAndHashCode.Include
    private final IMemoryLayout<VertexFormatElement> layout;
    @EqualsAndHashCode.Include
    private final RenderType renderType;
    @Getter
    private final VertexFormat.Mode mode;
    @Getter
    private final long vertexSize;
    private final int polygonSize;
    private final int polygonElementCount;

    private final IMemoryInterface posOffset;
    private final IMemoryInterface colorOffset;
    private final IMemoryInterface uv0Offset;
    private final IMemoryInterface uv1Offset;
    private final IMemoryInterface uv2Offset;
    private final IMemoryInterface normalOffset;
    private final Matrix4f cachedTransformValue;
    private final Matrix3f cachedNormalValue;
    private int elementCount;
    @Getter
    private int meshVertexCount;
    @Getter
    private int vertexCount;
    private long vertexAddress;
    private long sharingAddress;
    private int activeSharing;
    private int cachedSharing;
    private Matrix4f cachedTransform;
    private Matrix3f cachedNormal;

    public AcceleratedBufferBuilder(
            StagingBufferPool.StagingBuffer vertexBuffer,
            StagingBufferPool.StagingBuffer varyingBuffer,
            ElementBufferPool.ElementSegment elementSegment,
            AcceleratedBufferSetPool.BufferSet bufferSet,
            RenderType renderType
    ) {
        this.meshUploaders = new Reference2ObjectLinkedOpenHashMap<>();
        this.vertexBuffer = vertexBuffer;
        this.varyingBuffer = varyingBuffer;
        this.elementSegment = elementSegment;
        this.bufferSet = bufferSet;

        this.layout = bufferSet.getLayout();
        this.renderType = renderType;
        this.mode = this.renderType.mode;
        this.vertexSize = this.bufferSet.getVertexSize();
        this.polygonSize = this.mode.primitiveLength;
        this.polygonElementCount = this.mode.indexCount(this.polygonSize);


        this.posOffset = this.layout.getElement(VertexFormatElement.POSITION);
        this.colorOffset = this.layout.getElement(VertexFormatElement.COLOR);
        this.uv0Offset = this.layout.getElement(VertexFormatElement.UV0);
        this.uv1Offset = this.layout.getElement(VertexFormatElement.UV1);
        this.uv2Offset = this.layout.getElement(VertexFormatElement.UV2);
        this.normalOffset = this.layout.getElement(VertexFormatElement.NORMAL);

        this.elementCount = 0;
        this.meshVertexCount = 0;
        this.vertexCount = 0;
        this.vertexAddress = -1;
        this.sharingAddress = -1;
        this.activeSharing = -1;
        this.cachedSharing = -1;

        this.cachedTransform = null;
        this.cachedNormal = null;

        this.cachedTransformValue = new Matrix4f();
        this.cachedNormalValue = new Matrix3f();
    }

    @Override
    public VertexConsumer addVertex(
            PoseStack.Pose pPose,
            float pX,
            float pY,
            float pZ
    ) {
        beginTransform(pPose.pose(), pPose.normal());
        return addVertex(
                pX,
                pY,
                pZ
        );
    }

    @Override
    public VertexConsumer addVertex(
            float pX,
            float pY,
            float pZ
    ) {
        var vertexAddress = vertexBuffer.reserve(vertexSize);
        var varyingAddress = varyingBuffer.reserve(VARYING_SIZE);

        this.vertexAddress = vertexAddress;

        posOffset.putFloat(vertexAddress + 0L, pX);
        posOffset.putFloat(vertexAddress + 4L, pY);
        posOffset.putFloat(vertexAddress + 8L, pZ);

        VARYING_OFFSET.putInt(varyingAddress, 0);
        VARYING_SHARING.putInt(varyingAddress, activeSharing);

        var data = bufferSet.getExtraVertex(mode);
        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        vertexCount++;
        elementCount++;

        if (elementCount >= polygonSize) {
            elementSegment.countElements(polygonElementCount);
            elementCount = 0;
            activeSharing = -1;
        }

        return this;
    }

    @Override
    public VertexConsumer setColor(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        colorOffset.putByte(vertexAddress + 0L, (byte) pRed);
        colorOffset.putByte(vertexAddress + 1L, (byte) pGreen);
        colorOffset.putByte(vertexAddress + 2L, (byte) pBlue);
        colorOffset.putByte(vertexAddress + 3L, (byte) pAlpha);

        return this;
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        uv0Offset.putFloat(vertexAddress + 0L, pU);
        uv0Offset.putFloat(vertexAddress + 4L, pV);

        return this;
    }

    @Override
    public VertexConsumer setUv1(int pU, int pV) {
        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        uv1Offset.putShort(vertexAddress + 0L, (short) pU);
        uv1Offset.putShort(vertexAddress + 2L, (short) pV);

        return this;
    }

    @Override
    public VertexConsumer setUv2(int pU, int pV) {
        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        uv2Offset.putShort(vertexAddress + 0L, (short) pU);
        uv2Offset.putShort(vertexAddress + 2L, (short) pV);

        return this;
    }

    @Override
    public VertexConsumer setNormal(
            PoseStack.Pose pPose,
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        var normal = pPose.normal();

        if (activeSharing == -1) {
            return VertexConsumer.super.setNormal(
                    pPose,
                    pNormalX,
                    pNormalY,
                    pNormalZ
            );
        }

        if (!normal.equals(cachedNormal)) {
            SHARING_NORMAL.putMatrix3f(sharingAddress, normal);
        }

        return setNormal(
                pNormalX,
                pNormalY,
                pNormalZ
        );
    }

    @Override
    public VertexConsumer setNormal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        if (vertexAddress == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        normalOffset.putNormal(vertexAddress + 0L, pNormalX);
        normalOffset.putNormal(vertexAddress + 1L, pNormalY);
        normalOffset.putNormal(vertexAddress + 2L, pNormalZ);

        return this;
    }

    @Override
    public void addVertex(
            float pX,
            float pY,
            float pZ,
            int pColor,
            float pU,
            float pV,
            int pPackedOverlay,
            int pPackedLight,
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        var vertexAddress = vertexBuffer.reserve(vertexSize);
        var varyingAddress = varyingBuffer.reserve(VARYING_SIZE);
        var data = bufferSet.getExtraVertex(mode);

        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        posOffset.putFloat(vertexAddress + 0L, pX);
        posOffset.putFloat(vertexAddress + 4L, pY);
        posOffset.putFloat(vertexAddress + 8L, pZ);
        colorOffset.putInt(vertexAddress, FastColor.ABGR32.fromArgb32(pColor));
        uv0Offset.putFloat(vertexAddress + 0L, pU);
        uv0Offset.putFloat(vertexAddress + 4L, pV);
        uv1Offset.putInt(vertexAddress, pPackedOverlay);
        uv2Offset.putInt(vertexAddress, pPackedLight);
        normalOffset.putNormal(vertexAddress + 0L, pNormalX);
        normalOffset.putNormal(vertexAddress + 1L, pNormalY);
        normalOffset.putNormal(vertexAddress + 2L, pNormalZ);

        VARYING_OFFSET.putInt(varyingAddress, 0);
        VARYING_SHARING.putInt(varyingAddress, activeSharing);

        vertexCount++;
        elementCount++;

        if (elementCount >= polygonSize) {
            elementSegment.countElements(polygonElementCount);
            elementCount = 0;
            activeSharing = -1;
        }
    }

    @Override
    public void beginTransform(Matrix4f transform, Matrix3f normal) {
        if (CoreFeature.shouldCacheIdenticalPose()
                && transform.equals(cachedTransform)
                && normal.equals(cachedNormal)
        ) {
            activeSharing = cachedSharing;
            return;
        }

        cachedTransform = cachedTransformValue.set(transform);
        cachedNormal = cachedNormalValue.set(normal);

        cachedSharing = bufferSet.getSharing();
        activeSharing = cachedSharing;
        sharingAddress = bufferSet.reserveSharing();

        SHARING_TRANSFORM.putMatrix4f(sharingAddress, transform);
        SHARING_NORMAL.putMatrix3f(sharingAddress, normal);
    }

    @Override
    public void endTransform() {
        cachedTransform = null;
        cachedNormal = null;
        activeSharing = -1;
        cachedSharing = -1;
    }

    @Override
    public void addClientMesh(
            ByteBuffer meshBuffer,
            int size,
            int color,
            int light,
            int overlay
    ) {
        var bufferSize = vertexSize * size;
        var vertexAddress = vertexBuffer.reserve(bufferSize);
        var varyingAddress = varyingBuffer.reserve(VARYING_SIZE * size);
        var data = bufferSet.getExtraVertex(mode);

        data.addExtraVertex(vertexAddress);
        data.addExtraVarying(varyingAddress);

        MemoryUtil.memCopy(
                MemoryUtil.memAddress0(meshBuffer),
                vertexAddress,
                bufferSize
        );

        colorOffset.putInt(vertexAddress, FastColor.ABGR32.fromArgb32(color));
        uv1Offset.putInt(vertexAddress, overlay);
        uv2Offset.putInt(vertexAddress, light);
        VARYING_SHARING.putInt(varyingAddress, activeSharing);

        for (int i = 0; i < size; i++) {
            VARYING_OFFSET
                    .at(i)
                    .putInt(varyingAddress, i);
        }

        elementSegment.countElements(mode.indexCount(size));
        vertexCount += size;
    }

    @Override
    public void addServerMesh(
            ServerMesh serverMesh,
            int color,
            int light,
            int overlay
    ) {
        var size = (int) serverMesh.size();
        var meshUploader = meshUploaders.get(serverMesh);
        meshVertexCount = meshVertexCount + size;

        if (meshUploader == null) {
            meshUploader = bufferSet.getMeshUploader();

            meshUploaders.put(serverMesh, meshUploader);
            meshUploader.set(
                    layout,
                    mode,
                    serverMesh
            );
        }

        meshUploader.addUpload(
                color,
                light,
                overlay,
                activeSharing
        );
    }

    @Override
    public <T> void doRender(
            IAcceleratedRenderer<T> renderer,
            T context,
            Matrix4f transform,
            Matrix3f normal,
            int light,
            int overlay,
            int color
    ) {
        renderer.render(
                this,
                context,
                transform,
                normal,
                light,
                overlay,
                color
        );
    }

    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return buffer;
    }

    @Override
    public boolean isAccelerated() {
        return true;
    }

    @Override
    public RenderType getRenderType() {
        return renderType;
    }

    @Override
    public AcceleratedBufferSetPool.BufferSet getBufferSet() {
        return bufferSet;
    }

    public boolean isEmpty() {
        return (vertexCount + meshVertexCount) == 0;
    }

    public int getTotalVertexCount() {
        return vertexCount + meshVertexCount;
    }
}
