package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An interface to implement on {@link net.minecraft.tileentity.TileEntity}
 * to use with {@link com.cleanroommc.modularui.manager.GuiInfos#TILE_ENTITY}
 */
public interface IGuiHolder {

    /**
     * Called on server and client. Register sync handlers here. Must be the same on both sides.
     *
     * @param guiSyncHandler register sync handlers here
     * @param player         player who opens the UI
     */
    void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player);

    /**
     * Only called on client. Creates the UI screen
     *
     * @param player player who opens the UI
     * @return the UI screen
     */
    @SideOnly(Side.CLIENT)
    ModularScreen createClientGui(EntityPlayer player);
}
