package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @WrapMethod(method = "renderHandsWithItems")
    public void wrapRenderHandsWithItems(
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource.BufferSource buffer,
            LocalPlayer playerEntity,
            int combinedLight,
            Operation<Void> original
    ) {
        CoreFeature.setRenderingHand();
        original.call(
                partialTicks,
                poseStack,
                buffer,
                playerEntity,
                combinedLight
        );
        CoreFeature.resetRenderingHand();
    }
}
