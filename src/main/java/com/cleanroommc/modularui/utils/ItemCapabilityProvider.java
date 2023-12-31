package com.cleanroommc.modularui.utils;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemCapabilityProvider extends ICapabilityProvider {

    @Nullable
    <T> T getCapability(@NotNull Capability<T> capability);

    @Nullable
    @Override
    default <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return getCapability(capability);
    }

    @Override
    default boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability) != null;
    }
}
