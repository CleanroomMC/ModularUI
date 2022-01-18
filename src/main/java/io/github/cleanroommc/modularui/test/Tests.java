package io.github.cleanroommc.modularui.test;

import io.github.cleanroommc.modularui.builder.UIBuilder;
import io.github.cleanroommc.modularui.builder.UIInfo;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tests {

	static UIInfo<?, ?> diamondGui;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(Tests.class);
		diamondGui = UIBuilder.of().gui(((player, world, x, y, z) -> new DiamondGuiScreen())).build();
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (!event.getWorld().isRemote) {
			return;
		}
		if (event.getItemStack().getItem() == Items.DIAMOND) {
			diamondGui.open(event.getEntityPlayer());
		}
	}

	static class DiamondGuiScreen extends GuiYesNo {

		public DiamondGuiScreen() {
			super((result, id) -> {}, "Hi", "Hmm", 0);
		}

	}

}
