package com.cleanroommc.modularui.common.builder;

import com.cleanroommc.modularui.api.screen.IContainerCreator;
import com.cleanroommc.modularui.api.screen.IGuiCreator;

public class UIBuilder {

    private static final IContainerCreator DUMMY_CONTAINER_CREATOR = (player, world, x, y, z) -> null;
    private static final IGuiCreator DUMMY_GUI_CREATOR = (player, world, x, y, z) -> null;

    public static UIBuilder of() {
        return new UIBuilder();
    }

    private IContainerCreator containerCreator = DUMMY_CONTAINER_CREATOR;
    private IGuiCreator guiCreator = DUMMY_GUI_CREATOR;

    private UIBuilder() {

    }

    public UIBuilder container(IContainerCreator containerCreator) {
        this.containerCreator = containerCreator;
        return this;
    }

    public UIBuilder gui(IGuiCreator guiCreator) {
        this.guiCreator = guiCreator;
        return this;
    }

    public UIInfo<IContainerCreator, IGuiCreator> build() {
        return new UIInfo<>(this.containerCreator, this.guiCreator);
    }

}
