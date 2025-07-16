package com.github.argon4w.acceleratedrendering.core.programs;

import com.github.argon4w.acceleratedrendering.core.backends.programs.BarrierFlags;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

@Getter
public class LoadComputeShaderEvent extends Event implements IModBusEvent {

    private final ImmutableMap.Builder<ResourceLocation, ComputeShaderProgramLoader.ShaderDefinition> shaderLocations;

    public LoadComputeShaderEvent(ImmutableMap.Builder<ResourceLocation, ComputeShaderProgramLoader.ShaderDefinition> builder) {
        this.shaderLocations = builder;
    }

    public void loadComputeShader(
            ResourceLocation key,
            ResourceLocation location,
            BarrierFlags... barrierFlags
    ) {
        shaderLocations.put(key, new ComputeShaderProgramLoader.ShaderDefinition(location, BarrierFlags.getFlags(barrierFlags)));
    }
}
