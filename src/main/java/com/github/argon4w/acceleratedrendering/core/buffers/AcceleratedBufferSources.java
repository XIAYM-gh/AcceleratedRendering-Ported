package com.github.argon4w.acceleratedrendering.core.buffers;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;
import java.util.Set;

public class AcceleratedBufferSources {

    private final Map<VertexFormat, IAcceleratedBufferSource> sources;
    private final Set<VertexFormat.Mode> validModes;
    private final Set<String> invalidNames;
    private final boolean canSort;

    private AcceleratedBufferSources(
            Map<VertexFormat, IAcceleratedBufferSource> sources,
            Set<VertexFormat.Mode> validModes,
            Set<String> invalidNames,
            boolean canSort
    ) {
        this.sources = sources;
        this.validModes = validModes;
        this.invalidNames = invalidNames;
        this.canSort = canSort;
    }

    public static Builder builder() {
        return new Builder();
    }

    public VertexConsumer get(RenderType pRenderType) {
        if (pRenderType != null
                && (CoreFeature.shouldForceAccelerateTranslucent() || canSort || !pRenderType.sortOnUpload)
                && validModes.contains(pRenderType.mode)
                && !invalidNames.contains(pRenderType.name)
                && sources.containsKey(pRenderType.format)
        ) {
            return sources
                    .get(pRenderType.format)
                    .getBuffer(pRenderType);
        }

        return null;
    }

    public static class Builder {

        private final Map<VertexFormat, IAcceleratedBufferSource> sources;
        private final Set<VertexFormat.Mode> validModes;
        private final Set<String> invalidNames;

        private boolean canSort;

        private Builder() {
            this.sources = new Object2ObjectOpenHashMap<>();
            this.validModes = new ReferenceOpenHashSet<>();
            this.invalidNames = new ObjectOpenHashSet<>();

            this.canSort = false;
        }

        public Builder source(IAcceleratedBufferSource bufferSource) {
            sources.putAll(Maps.asMap(
                    bufferSource
                            .getBufferEnvironment()
                            .getVertexFormats(),
                    $ -> bufferSource
            ));
            return this;
        }

        public Builder mode(VertexFormat.Mode mode) {
            validModes.add(mode);
            return this;
        }

        public Builder invalid(String name) {
            invalidNames.add(name);
            return this;
        }

        public Builder canSort() {
            canSort = true;
            return this;
        }

        public AcceleratedBufferSources build() {
            return new AcceleratedBufferSources(
                    sources,
                    validModes,
                    invalidNames,
                    canSort
            );
        }
    }
}
