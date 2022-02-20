package io.github.cleanroommc.modularui.test;

import io.github.cleanroommc.modularui.ModularUIMod;
import io.github.cleanroommc.modularui.api.math.Alignment;
import io.github.cleanroommc.modularui.api.math.Size;
import io.github.cleanroommc.modularui.builder.ModularUIBuilder;
import io.github.cleanroommc.modularui.drawable.IDrawable;
import io.github.cleanroommc.modularui.drawable.Text;
import io.github.cleanroommc.modularui.drawable.UITexture;
import io.github.cleanroommc.modularui.internal.ModularUI;
import io.github.cleanroommc.modularui.builder.UIBuilder;
import io.github.cleanroommc.modularui.builder.UIInfo;
import io.github.cleanroommc.modularui.internal.ModularGui;
import io.github.cleanroommc.modularui.internal.ModularUIContainer;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tests {

	static UIInfo<?, ?> diamondGui;
	static UIInfo<?, ?> modularGui;
	public static final IDrawable BACKGROUND = UITexture.fullImage(ModularUIMod.ID, "gui/background/background");

	public static void init() {
		MinecraftForge.EVENT_BUS.register(Tests.class);
		diamondGui = UIBuilder.of().gui(((player, world, x, y, z) -> new DiamondGuiScreen())).build();
		modularGui = UIBuilder.of()
				.gui((player, world, x, y, z) -> new ModularGui(new ModularUIContainer(createUI(player))))
				.container((player, world, x, y, z) -> {
					ModularUI modularUI = createUI(player);
					modularUI.initialise();
					return new ModularUIContainer(modularUI);
				}).build();
	}

	public static ModularUI createUI(EntityPlayer player) {
		return ModularUIBuilder.create(new Size(176, 166))
				.setAlignment(Alignment.Center)
				.drawable(BACKGROUND, Alignment.Center)
				.drawable(new Text("Hello"), Alignment.Center, new Size(30, 11))
				.build(player);
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getWorld().isRemote) {
			return;
		}
		if (event.getItemStack().getItem() == Items.DIAMOND) {
			modularGui.open(event.getEntityPlayer());
		}
	}

	static class DiamondGuiScreen extends GuiYesNo {

		public DiamondGuiScreen() {
			super((result, id) -> {}, "Hi", "Hmm", 0);
		}

	}

}
