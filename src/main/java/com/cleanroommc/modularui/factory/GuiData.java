package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Objects;

/**
 * This class and subclasses are holding necessary data to find the exact same GUI on client and server.
 * For example, if the GUI was opened by right-clicking a TileEntity, then this data needs a world and a block pos.
 * <p>
 * Also see {@link PosGuiData} (useful for TileEntities), {@link SidedPosGuiData} (useful for covers from GregTech) and
 * {@link HandGuiData} (useful for guis opened by interacting with an item in the players hand) for default implementations.
 * </p>
 */
public class GuiData {

    private final EntityPlayer player;

    public GuiData(EntityPlayer player) {
        this.player = Objects.requireNonNull(player);
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public ItemStack getMainHandItem() {
        return this.player.getHeldItemMainhand();
    }

    public ItemStack getOffHandItem() {
        return this.player.getHeldItemOffhand();
    }
}
