package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.JeiSettingsImpl;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Objects;

/**
 * This class and subclasses are holding necessary data to find the exact same GUI on client and server.
 * For example, if the GUI was opened by right-clicking a TileEntity, then this data needs a world and a block pos.
 * Additionally, this can be used to configure JEI via {@link #getJeiSettings()}.
 * <p>
 * Also see {@link PosGuiData} (useful for TileEntities), {@link SidedPosGuiData} (useful for covers from GregTech) and
 * {@link HandGuiData} (useful for guis opened by interacting with an item in the players hand) for default implementations.
 * </p>
 */
public class GuiData {

    private final EntityPlayer player;
    private final int openedDimension;
    private JeiSettings jeiSettings;

    public GuiData(EntityPlayer player) {
        this.player = Objects.requireNonNull(player);
        this.openedDimension = player.dimension;
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

    public JeiSettings getJeiSettings() {
        if (this.jeiSettings == null) {
            throw new IllegalStateException("Not yet initialised!");
        }
        return this.jeiSettings;
    }

    final JeiSettingsImpl getJeiSettingsImpl() {
        return (JeiSettingsImpl) this.jeiSettings;
    }

    final void setJeiSettings(JeiSettings jeiSettings) {
        this.jeiSettings = Objects.requireNonNull(jeiSettings);
    }

    public boolean canInteractWith(EntityPlayer playerIn){
        return playerIn == player && playerIn.dimension == openedDimension;
    }
}
