package io.github.cleanroommc.modularui.widget;

import net.minecraft.client.gui.Gui;

import java.awt.*;

/**
 * This class depicts a functional element of a ModularUI
 */
public abstract class Widget extends Gui {

	protected Rectangle parentPosition;
	protected Rectangle currentPosition;

	public Widget(Rectangle position) {
		this.parentPosition = position;
		this.currentPosition = position;
	}

	public Widget(int x, int y, int width, int height) {
		this(new Rectangle(x, y, width, height));
	}

	protected void onPositionUpdate() {

	}

}
