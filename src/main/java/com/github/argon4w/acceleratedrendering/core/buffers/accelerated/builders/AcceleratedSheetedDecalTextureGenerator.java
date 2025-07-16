package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.EqualsAndHashCode;
import lombok.experimental.ExtensionMethod;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

@ExtensionMethod(VertexConsumerExtension.class)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = false
)
public class AcceleratedSheetedDecalTextureGenerator extends AcceleratedVertexConsumerWrapper {

    @EqualsAndHashCode.Include
    private final VertexConsumer delegate;
    @EqualsAndHashCode.Include
    private final Matrix4f cameraInverse;
    private final Matrix3f normalInverse;
    private final float textureScale;

    private final Vector3f cachedCamera;
    private final Vector3f cachedNormal;

    private float vertexX;
    private float vertexY;
    private float vertexZ;

    public AcceleratedSheetedDecalTextureGenerator(
            VertexConsumer delegate,
            Matrix4f cameraInverse,
            Matrix3f normalInverse,
            float textureScale
    ) {
        this.delegate = delegate;
        this.cameraInverse = cameraInverse;
        this.normalInverse = normalInverse;
        this.textureScale = textureScale;

        this.cachedCamera = new Vector3f();
        this.cachedNormal = new Vector3f();

        this.vertexX = 0;
        this.vertexY = 0;
        this.vertexZ = 0;
    }

    @Override
    protected VertexConsumer getDelegate() {
        return delegate;
    }

    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return new AcceleratedSheetedDecalTextureGenerator(
                getDelegate()
                        .getAccelerated()
                        .decorate(buffer),
                cameraInverse,
                normalInverse,
                textureScale
        );
    }

    @Override
    public void addClientMesh(
            ByteBuffer meshBuffer,
            int size,
            int color,
            int light,
            int overlay
    ) {
        getDelegate()
                .getAccelerated()
                .addClientMesh(
                        meshBuffer,
                        size,
                        -1,
                        light,
                        overlay
                );
    }

    @Override
    public void addServerMesh(
            ServerMesh serverMesh,
            int color,
            int light,
            int overlay
    ) {
        getDelegate()
                .getAccelerated()
                .addServerMesh(
                        serverMesh,
                        -1,
                        light,
                        overlay
                );
    }

    @Override
    public VertexConsumer addVertex(
            float pX,
            float pY,
            float pZ
    ) {
        vertexX = pX;
        vertexY = pY;
        vertexZ = pZ;

        delegate.addVertex(
                pX,
                pY,
                pZ
        );
        return this;
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        return this;
    }

    @Override
    public VertexConsumer setColor(
            int pRed,
            int pGreen,
            int pBlue,
            int pAlpha
    ) {
        delegate.setColor(-1);
        return this;
    }

    @Override
    public VertexConsumer setNormal(
            float pNormalX,
            float pNormalY,
            float pNormalZ
    ) {
        delegate.setNormal(
                pNormalX,
                pNormalY,
                pNormalZ
        );

        var normal = normalInverse.transform(
                pNormalX,
                pNormalY,
                pNormalZ,
                cachedNormal
        );

        var camera = cameraInverse.transformPosition(
                vertexX,
                vertexY,
                vertexZ,
                cachedCamera
        );

        var direction = Direction.getNearest(
                normal.x(),
                normal.y(),
                normal.z()
        );

        camera.rotateY((float) Math.PI);
        camera.rotateX((float) (-Math.PI / 2));
        camera.rotate(direction.getRotation());

        delegate.setUv(-camera.x() * textureScale, -camera.y() * textureScale);
        return this;
    }

    @Override
    public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int packedOverlay,
            int packedLight,
            float normalX,
            float normalY,
            float normalZ
    ) {
        this
                .addVertex(x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(normalX, normalY, normalZ);
    }
}
