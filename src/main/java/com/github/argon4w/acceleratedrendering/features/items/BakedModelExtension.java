package com.github.argon4w.acceleratedrendering.features.items;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;

public class BakedModelExtension {

    public static IAcceleratedBakedModel getAccelerated(BakedModel in) {
        return (IAcceleratedBakedModel) in;
    }

    public static IAcceleratedBakedQuad getAccelerated(BakedQuad in) {
        return (IAcceleratedBakedQuad) in;
    }
}
