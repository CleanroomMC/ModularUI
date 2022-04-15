package com.cleanroommc.modularui.api.screen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

@FunctionalInterface
public interface IContainerCreator<C extends Container> {

	C create(EntityPlayer player, World world, int x, int y, int z);

}
