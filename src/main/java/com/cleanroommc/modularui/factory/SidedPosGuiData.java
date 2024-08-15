package com.cleanroommc.modularui.factory;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class SidedPosGuiData extends PosGuiData {

    private final Direction side;

    public SidedPosGuiData(Player player, int x, int y, int z, Direction side) {
        super(player, x, y, z);
        this.side = side;
    }

    public Direction getSide() {
        return this.side;
    }
}
