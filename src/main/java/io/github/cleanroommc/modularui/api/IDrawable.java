package io.github.cleanroommc.modularui.api;

public interface IDrawable {

	void drawInForeground(int mouseX, int mouseY, float partialTicks);

	void drawInBackground(int mouseX, int mouseY, float partialTicks);

}
