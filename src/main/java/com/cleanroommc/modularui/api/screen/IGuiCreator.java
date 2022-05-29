package com.cleanroommc.modularui.api.screen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IGuiCreator {

    @SideOnly(Side.CLIENT)
    Object create(EntityPlayer player, World world, int x, int y, int z);

}
