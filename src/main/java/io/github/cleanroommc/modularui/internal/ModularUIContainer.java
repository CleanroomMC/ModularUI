package io.github.cleanroommc.modularui.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ModularUIContainer extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return false;
	}

}
