package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IItemGuiHolder {

    void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player, ItemStack itemStack);

    ModularScreen createGuiScreen(EntityPlayer player, ItemStack itemStack);
}
