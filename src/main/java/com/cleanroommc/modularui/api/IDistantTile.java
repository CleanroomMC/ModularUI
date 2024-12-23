package com.cleanroommc.modularui.api;
/**
 * An interface to implement on {@link net.minecraft.tileentity.TileEntity}.
 * Allow changing max distance between tile and player (default 8 blocks).
 */
public interface IDistantTile {
    double getInteractionDistance();
}
