package io.github.cleanroommc.modularui.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ModularUIContainer extends Container {

	private final ModularUI gui;

	public ModularUIContainer(ModularUI gui) {
		this.gui = gui;
	}

	public ModularUI getGui() {
		return gui;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
}
