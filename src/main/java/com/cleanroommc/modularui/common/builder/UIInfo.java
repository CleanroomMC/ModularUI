package com.cleanroommc.modularui.common.builder;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.IContainerCreator;
import com.cleanroommc.modularui.api.IGuiCreator;
import com.cleanroommc.modularui.common.internal.InternalUIMapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class UIInfo<CC extends IContainerCreator<?>, GC extends IGuiCreator<?>> {

	private final int id;
	private final CC containerCreator;
	private final GC guiCreator;

	UIInfo(CC containerCreator, GC guiCreator) {
		this.id = InternalUIMapper.getInstance().register(containerCreator, guiCreator);
		this.containerCreator = containerCreator;
		this.guiCreator = guiCreator;
	}

	public void open(EntityPlayer player, World world, int x, int y, int z) {
		FMLNetworkHandler.openGui(player, ModularUIMod.INSTANCE, id, world, x, y, z);
	}

	public void open(EntityPlayer player, World world, BlockPos pos) {
		open(player, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public void open(EntityPlayer player) {
		open(player, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
	}

}
