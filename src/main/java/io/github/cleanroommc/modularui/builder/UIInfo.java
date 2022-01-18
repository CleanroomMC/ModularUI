package io.github.cleanroommc.modularui.builder;

import io.github.cleanroommc.modularui.ModularUI;
import io.github.cleanroommc.modularui.api.IContainerCreator;
import io.github.cleanroommc.modularui.api.IGuiCreator;
import io.github.cleanroommc.modularui.internal.InternalUIMapper;
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
		FMLNetworkHandler.openGui(player, ModularUI.INSTANCE, id, world, x, y, z);
	}

	public void open(EntityPlayer player, World world, BlockPos pos) {
		open(player, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public void open(EntityPlayer player) {
		open(player, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
	}

}
