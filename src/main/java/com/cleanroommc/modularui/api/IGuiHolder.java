package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;

public interface IGuiHolder {

    void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player);

    @SideOnly(Side.CLIENT)
    ModularScreen createClientGui(EntityPlayer player);
}
