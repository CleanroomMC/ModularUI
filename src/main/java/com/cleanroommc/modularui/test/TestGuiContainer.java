package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

public class TestGuiContainer extends GuiContainerWrapper {

    public TestGuiContainer(ModularContainer container, ModularScreen screen) {
        super(container, screen);
        ModularUI.LOGGER.info("Created custom gui container");
    }
}
