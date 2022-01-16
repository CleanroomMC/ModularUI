package io.github.cleanroommc.modularui.texture;

@FunctionalInterface
public interface IUITexture {

	IUITexture EMPTY = (x, y, width, height) -> { };

	void draw(double x, double y, int width, int height);

	default void updateTick() { }

}
