package com.github.argon4w.acceleratedrendering.features.items;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public interface IAcceleratedBakedModel {

    void renderItemFast(ItemStack itemStack, PoseStack poseStack, IAcceleratedVertexConsumer extension, int combinedLight, int combinedOverlay);

    int getCustomColor(int layer, int color);

    boolean isAccelerated();

    boolean isAcceleratedInHand();
}
