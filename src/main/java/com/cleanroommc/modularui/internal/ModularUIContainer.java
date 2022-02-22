package com.cleanroommc.modularui.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ModularUIContainer extends Container {

	private final ModularUI gui;

	public ModularUIContainer(ModularUI gui) {
		this.gui = gui;
		this.gui.setContainer(this);
	}

	public ModularUI getGui() {
		return gui;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.isEntityAlive();
	}

	@Override
	public Slot addSlotToContainer(Slot slotIn) {
		return super.addSlotToContainer(slotIn);
	}
}
