package com.cleanroommc.modularui.common.drawable;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.widget.DrawableWidget;

@FunctionalInterface
public interface IDrawable {

	IDrawable EMPTY = (pos, size, partialTicks) -> { };

	void draw(Pos2d pos, Size size, float partialTicks);

	default void tick() {
	}

	default DrawableWidget asWidget() {
		return new DrawableWidget().setDrawable(this);
	}
}
