package io.github.cleanroommc.modularui.internal;

import io.github.cleanroommc.modularui.api.IWidgetParent;
import io.github.cleanroommc.modularui.api.math.GuiArea;
import io.github.cleanroommc.modularui.widget.Widget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.util.List;

public class ModularUIContainer extends Container implements IWidgetParent {

	private final GuiArea area;
	private List<Widget> widgets;

	public ModularUIContainer(GuiArea area) {
		this.area = area;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	@Override
	public GuiArea getArea() {
		return area;
	}

	@Override
	public List<Widget> getChildren() {
		return null;
	}
}
