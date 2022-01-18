package io.github.cleanroommc.modularui.widget;

import java.awt.*;

/**
 * This class depicts a functional element of a ModularUI
 */
public class Widget {

	protected Rectangle parentPosition;
	protected Rectangle currentPosition;

	public Widget(Rectangle position) {
		this.parentPosition = position;
		this.currentPosition = position;
	}

	public Widget(int x, int y, int width, int height) {
		this(new Rectangle(x, y, width, height));
	}

	public void setParentPosition(Rectangle parentPosition) {
		this.parentPosition = parentPosition;

	}

	protected void recomputePosition() {
		// this.currentPosition = this.parentPosition.add(selfPosition);
		onPositionUpdate();
	}

	protected void onPositionUpdate() {

	}

}
