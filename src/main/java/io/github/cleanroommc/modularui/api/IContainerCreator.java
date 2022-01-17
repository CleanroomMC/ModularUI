package io.github.cleanroommc.modularui.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

@FunctionalInterface
public interface IContainerCreator<C extends Container> {

	C create(EntityPlayer player, World world, int x, int y, int z);

}
