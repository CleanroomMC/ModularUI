package com.cleanroommc.modularui.core.mixin;

import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Container.class)
public interface ContainerAccessor {

    @Accessor
    int getDragEvent();
}
