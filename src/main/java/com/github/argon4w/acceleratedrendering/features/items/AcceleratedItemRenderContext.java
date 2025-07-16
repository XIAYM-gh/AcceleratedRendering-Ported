package com.github.argon4w.acceleratedrendering.features.items;

import com.github.argon4w.acceleratedrendering.features.items.mixins.ItemColorsAccessor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

@Getter
public class AcceleratedItemRenderContext {

    private final ItemStack itemStack;
    private final ItemColor itemColor;
    private final BakedModel bakedModel;
    private final RandomSource random;

    public AcceleratedItemRenderContext(
            ItemStack itemStack,
            BakedModel bakedModel,
            RandomSource random
    ) {
        this.itemStack = itemStack;
        this.itemColor = ((ItemColorsAccessor) Minecraft.getInstance().getItemColors()).getItemColors()
                .getOrDefault(itemStack.getItem(), EmptyItemColor.INSTANCE);
        this.bakedModel = bakedModel;
        this.random = random;
    }
}
