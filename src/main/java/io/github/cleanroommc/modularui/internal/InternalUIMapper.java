package io.github.cleanroommc.modularui.internal;

import io.github.cleanroommc.modularui.ModularUI;
import io.github.cleanroommc.modularui.api.IContainerCreator;
import io.github.cleanroommc.modularui.api.IGuiCreator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InternalUIMapper implements IGuiHandler {

	private static InternalUIMapper INSTANCE;

	public static InternalUIMapper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new InternalUIMapper();
		}
		return INSTANCE;
	}

	private int id;
	private final List<IContainerCreator<?>> serverContainers;
	private final List<IGuiCreator<?>> clientGuis;

	public InternalUIMapper() {
		NetworkRegistry.INSTANCE.registerGuiHandler(ModularUI.INSTANCE, this);
		this.serverContainers = new ArrayList<>();
		this.clientGuis = new ArrayList<>();
	}

	public <CC extends IContainerCreator<?>, GC extends IGuiCreator<?>> int register(CC containerCreator, GC guiCreator) {
		this.serverContainers.add(containerCreator);
		this.clientGuis.add(guiCreator);
		return id++;
	}

	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return serverContainers.get(id).create(player, world, x, y, z);
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return clientGuis.get(id).create(player, world, x, y, z);
	}

}
