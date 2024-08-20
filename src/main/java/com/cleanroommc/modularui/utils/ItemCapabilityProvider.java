package com.cleanroommc.modularui.utils;

import net.minecraft.core.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemCapabilityProvider extends ICapabilityProvider {

    @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability);

    @Override
    default @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return getCapability(capability);
    }
}
