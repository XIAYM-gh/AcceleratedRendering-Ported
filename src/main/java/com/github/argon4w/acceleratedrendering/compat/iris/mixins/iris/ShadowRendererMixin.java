package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffers;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShadowRenderer.class)
public class ShadowRendererMixin {

    @Inject(
            method = "renderShadows",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"
            )
    )
    public void endAllBatches(
            LevelRendererAccessor levelRenderer,
            Camera playerCamera,
            CallbackInfo ci
    ) {
        IrisCompatBuffers.BLOCK_SHADOW.drawBuffers();
        IrisCompatBuffers.ENTITY_SHADOW.drawBuffers();
        IrisCompatBuffers.GLYPH_SHADOW.drawBuffers();
        IrisCompatBuffers.POS_TEX_SHADOW.drawBuffers();
        IrisCompatBuffers.POS_TEX_COLOR_SHADOW.drawBuffers();
    }
}
