package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class SidedPosGuiData extends PosGuiData {

    private final EnumFacing side;

    public SidedPosGuiData(@NotNull EntityPlayer player, int x, int y, int z, @NotNull EnumFacing side) {
        super(player, x, y, z);
        this.side = Objects.requireNonNull(side);
    }

    @NotNull
    public EnumFacing getSide() {
        return this.side;
    }
}
