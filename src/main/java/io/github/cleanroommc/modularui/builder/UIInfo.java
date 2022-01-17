package io.github.cleanroommc.modularui.builder;

import io.github.cleanroommc.modularui.api.IContainerCreator;
import io.github.cleanroommc.modularui.api.IGuiCreator;
import io.github.cleanroommc.modularui.internal.InternalUIMapper;

public class UIInfo<CR extends IContainerCreator, GR extends IGuiCreator> {

	private final int id;
	private final CR containerCreator;
	private final GR guiCreator;

	UIInfo(CR containerCreator, GR guiCreator) {
		this.id = InternalUIMapper.getInstance().register(containerCreator, guiCreator);
		this.containerCreator = containerCreator;
		this.guiCreator = guiCreator;
	}



}
