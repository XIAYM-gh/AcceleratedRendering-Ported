package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandRenderer.class)
public class HandRendererMixin {

    @Inject(
            method = "renderSolid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/batchedentityrendering/impl/FullyBufferedMultiBufferSource;endBatch()V"
            )
    )
    public void drawHandBuffersSolid(
            Matrix4fc modelMatrix,
            float tickDelta,
            Camera camera,
            GameRenderer gameRenderer,
            WorldRenderingPipeline pipeline,
            CallbackInfo ci
    ) {
        CoreBuffers.ENTITY.drawBuffers();
        CoreBuffers.BLOCK.drawBuffers();
        CoreBuffers.POS.drawBuffers();
        CoreBuffers.POS_TEX.drawBuffers();
        CoreBuffers.POS_TEX_COLOR.drawBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.drawBuffers();
    }

    @Inject(
            method = "renderTranslucent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/batchedentityrendering/impl/FullyBufferedMultiBufferSource;endBatch()V"
            )
    )
    public void drawHandBuffersTranslucent(
            Matrix4fc modelMatrix,
            float tickDelta,
            Camera camera,
            GameRenderer gameRenderer,
            WorldRenderingPipeline pipeline,
            CallbackInfo ci
    ) {
        CoreBuffers.ENTITY.drawBuffers();
        CoreBuffers.BLOCK.drawBuffers();
        CoreBuffers.POS.drawBuffers();
        CoreBuffers.POS_TEX.drawBuffers();
        CoreBuffers.POS_TEX_COLOR.drawBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.drawBuffers();
    }
}
