package com.cleanroommc.modularui.core.mixin;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Slot.class, remap = false)
public interface SlotAccessorClient {

    @Invoker
    TextureMap invokeGetBackgroundMap();
}
