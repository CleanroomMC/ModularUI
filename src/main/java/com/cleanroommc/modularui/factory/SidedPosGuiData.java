package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public class SidedPosGuiData extends PosGuiData {

    private final EnumFacing side;

    public SidedPosGuiData(EntityPlayer player, int x, int y, int z, EnumFacing side) {
        super(player, x, y, z);
        this.side = side;
    }

    public EnumFacing getSide() {
        return this.side;
    }
}
