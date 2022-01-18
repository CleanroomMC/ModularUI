package io.github.cleanroommc.modularui.builder;

import io.github.cleanroommc.modularui.api.IContainerCreator;
import io.github.cleanroommc.modularui.api.IGuiCreator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;

public class UIBuilder<CC extends Container, GC extends GuiScreen> {

	public static UIBuilder of() {
		return new UIBuilder();
	}

	private IContainerCreator<CC> containerCreator;
	private IGuiCreator<GC> guiCreator;

	public UIBuilder<CC, GC> container(IContainerCreator<CC> containerCreator) {
		this.containerCreator = containerCreator;
		return this;
	}

	public UIBuilder<CC, GC> gui(IGuiCreator<GC> guiCreator) {
		this.guiCreator = guiCreator;
		return this;
	}

	public UIInfo<IContainerCreator<CC>, IGuiCreator<GC>> build() {
		return new UIInfo<>(this.containerCreator, this.guiCreator);
	}

	private UIBuilder() { }

}
