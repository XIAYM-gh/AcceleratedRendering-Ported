package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.utils.CullerUtils;
import com.github.argon4w.acceleratedrendering.core.utils.TextureUtils;
import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class CulledMeshCollector implements VertexConsumer, IMeshCollector {

    private final int polygonSize;
    private final Vertex[] polygon;
    private final NativeImage texture;
    private final SimpleMeshCollector meshCollector;

    private int vertexIndex;

    public CulledMeshCollector(RenderType renderType, IMemoryLayout<VertexFormatElement> layout) {
        this.polygonSize = renderType.mode.primitiveLength;
        this.polygon = new Vertex[this.polygonSize];
        this.texture = TextureUtils.downloadTexture(renderType, 0);
        this.meshCollector = new SimpleMeshCollector(layout);

        this.vertexIndex = -1;
    }

    public void flush() {
        if (vertexIndex >= polygonSize - 1) {
            vertexIndex = -1;

            if (!CullerUtils.shouldCull(polygon, texture)) {
                for (var vertex : polygon) {
                    var vertexPosition = vertex.getPosition();
                    var vertexUV = vertex.getUv();
                    var vertexNormal = vertex.getNormal();

                    meshCollector.addVertex(
                            vertexPosition.x,
                            vertexPosition.y,
                            vertexPosition.z,
                            vertex.getPackedColor(),
                            vertexUV.x,
                            vertexUV.y,
                            OverlayTexture.NO_OVERLAY,
                            vertex.getPackedLight(),
                            vertexNormal.x,
                            vertexNormal.y,
                            vertexNormal.z
                    );
                }
            }
        }
    }

    @Override
    public VertexConsumer addVertex(
            float pX,
            float pY,
            float pZ
    ) {
        flush();
        polygon[++vertexIndex] = new Vertex();
        polygon[vertexIndex].getPosition().x = pX;
        polygon[vertexIndex].getPosition().y = pY;
        polygon[vertexIndex].getPosition().z = pZ;

        return this;
    }

    @Override
    public VertexConsumer setColor(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getColor().x = pRed;
        polygon[vertexIndex].getColor().y = pGreen;
        polygon[vertexIndex].getColor().z = pBlue;
        polygon[vertexIndex].getColor().w = pAlpha;

        return this;
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getUv().x = pU;
        polygon[vertexIndex].getUv().y = pV;

        return this;
    }

    @Override
    public VertexConsumer setUv1(int pU, int pV) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int pU, int pV) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getLight().x = pU;
        polygon[vertexIndex].getLight().y = pV;

        return this;
    }

    @Override
    public VertexConsumer setNormal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        if (vertexIndex < 0) {
            throw new IllegalStateException("Vertex not building!");
        }

        polygon[vertexIndex].getNormal().x = pNormalX;
        polygon[vertexIndex].getNormal().y = pNormalY;
        polygon[vertexIndex].getNormal().z = pNormalZ;
        return this;
    }

    @Override
    public ByteBufferBuilder getBuffer() {
        return meshCollector.getBuffer();
    }

    @Override
    public IMemoryLayout<VertexFormatElement> getLayout() {
        return meshCollector.getLayout();
    }

    @Override
    public int getVertexCount() {
        return meshCollector.getVertexCount();
    }
}
