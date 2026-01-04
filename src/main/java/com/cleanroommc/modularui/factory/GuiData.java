package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class and subclasses are holding necessary data to find the exact same GUI on client and server.
 * For example, if the GUI was opened by right-clicking a TileEntity, then this data needs a world and a block pos.
 * <p>
 * Also see {@link PosGuiData} (useful for TileEntities), {@link SidedPosGuiData} (useful for covers from GregTech) and
 * {@link PlayerInventoryGuiData} (useful for guis opened by interacting with an item in the players inventory) for default implementations.
 * </p>
 */
public class GuiData {

    private final EntityPlayer player;

    public GuiData(@NotNull EntityPlayer player) {
        this.player = Objects.requireNonNull(player);
    }

    @NotNull
    public EntityPlayer getPlayer() {
        return this.player;
    }

    public World getWorld() {
        return this.player.getEntityWorld();
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
