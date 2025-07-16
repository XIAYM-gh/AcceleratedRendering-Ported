package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = LevelRenderer.class,
        priority = 998
)
public class LevelRendererMixin {

    @WrapMethod(method = "renderLevel")
    public void wrapRenderLevel(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Operation<Void> original
    ) {
        CoreFeature.setRenderingLevel();
        original.call(
                deltaTracker,
                renderBlockOutline,
                camera,
                gameRenderer,
                lightTexture,
                frustumMatrix,
                projectionMatrix
        );
        CoreFeature.resetRenderingLevel();
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"
            )
    )
    public void endOutlineBatches(
            DeltaTracker pDeltaTracker,
            boolean pRenderBlockOutline,
            Camera pCamera,
            GameRenderer pGameRenderer,
            LightTexture pLightTexture,
            Matrix4f pFrustumMatrix,
            Matrix4f pProjectionMatrix,
            CallbackInfo ci
    ) {
        CoreBuffers.POS_TEX_COLOR_OUTLINE.drawBuffers();
    }

    @WrapOperation(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V"
            )
    )
    public void drawCoreBuffers(MultiBufferSource.BufferSource instance, Operation<Void> original) {
        CoreBuffers.ENTITY.drawBuffers();
        CoreBuffers.BLOCK.drawBuffers();
        CoreBuffers.POS.drawBuffers();
        CoreBuffers.POS_TEX.drawBuffers();
        CoreBuffers.POS_TEX_COLOR.drawBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.drawBuffers();

        original.call(instance);
    }
}
