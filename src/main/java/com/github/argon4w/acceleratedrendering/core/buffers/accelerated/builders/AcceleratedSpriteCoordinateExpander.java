package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@ExtensionMethod(VertexConsumerExtension.class)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AcceleratedSpriteCoordinateExpander extends AcceleratedVertexConsumerWrapper {

    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    @Override
    public VertexConsumer getDelegate() {
        return delegate;
    }

    @Override
    public VertexConsumer decorate(VertexConsumer buffer) {
        return new AcceleratedSpriteCoordinateExpander(
                getDelegate()
                        .getAccelerated()
                        .decorate(buffer),
                sprite
        );
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        delegate.setUv(
                sprite.getU(pU),
                sprite.getV(pV)
        );
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
        delegate.addVertex(
                pX,
                pY,
                pZ,
                pColor,
                sprite.getU(pU),
                sprite.getV(pV),
                pPackedOverlay,
                pPackedLight,
                pNormalX,
                pNormalY,
                pNormalZ
        );
    }
}
