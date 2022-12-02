package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGuiHolder {

    void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player);

    @SideOnly(Side.CLIENT)
    ModularScreen createClientGui(EntityPlayer player);
}
