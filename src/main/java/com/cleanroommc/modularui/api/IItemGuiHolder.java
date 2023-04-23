package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An interface to implement on {@link net.minecraft.item.Item}
 * to use with {@link GuiInfos#PLAYER_ITEM_MAIN_HAND} or {@link GuiInfos#PLAYER_ITEM_OFF_HAND}.
 */
public interface IItemGuiHolder {

    /**
     * Called on server and client. Register sync handlers here. Must be the same on both sides.
     *
     * @param guiSyncHandler register sync handlers here
     * @param player         player who opens the UI
     * @param itemStack      the item in the players hand which opens the ui
     */
    void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player, ItemStack itemStack);

    /**
     * Only called on client. Creates the UI screen
     *
     * @param player    player who opens the UI
     * @param itemStack the item in the players hand which opens the ui
     * @return the UI screen
     */
    @SideOnly(Side.CLIENT)
    ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack);
}
