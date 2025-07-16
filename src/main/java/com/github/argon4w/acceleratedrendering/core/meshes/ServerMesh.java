package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.IMeshCollector;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

public record ServerMesh(long size, IServerBuffer meshBuffer) implements IMesh {

    @Override
    public void write(
            IAcceleratedVertexConsumer extension,
            int color,
            int light,
            int overlay
    ) {
        extension.addServerMesh(
                this,
                color,
                light,
                overlay
        );
    }

    public static class Builder implements IMesh.Builder {

        public static final Builder INSTANCE = new Builder();

        private final List<ServerMesh> meshes;

        private Builder() {
            this.meshes = new ObjectArrayList<>();
        }

        @Override
        public IMesh build(IMeshCollector collector) {
            var vertexCount = collector.getVertexCount();

            if (vertexCount == 0) {
                return EmptyMesh.INSTANCE;
            }

            var builder = collector.getBuffer();
            var result = builder.build();

            if (result == null) {
                builder.close();
                return EmptyMesh.INSTANCE;
            }

            var clientBuffer = result.byteBuffer();
            var serverBuffer = new MutableBuffer(clientBuffer.capacity(), GL_DYNAMIC_STORAGE_BIT);
            var mesh = new ServerMesh(vertexCount, serverBuffer);

            meshes.add(mesh);
            serverBuffer.data(clientBuffer);
            builder.close();

            return mesh;
        }

        @Override
        public void close() {
            for (var mesh : meshes) {
                mesh
                        .meshBuffer
                        .delete();
            }
        }
    }
}
