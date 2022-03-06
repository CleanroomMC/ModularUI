package com.cleanroommc.modularui.api;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@FunctionalInterface
public interface IGuiCreator<G extends GuiScreen> {

	G create(EntityPlayer player, World world, int x, int y, int z);

}
