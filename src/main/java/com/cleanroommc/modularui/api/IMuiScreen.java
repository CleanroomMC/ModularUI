package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ClientScreenHandler;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.IntConsumer;

public interface IMuiScreen {

    @NotNull
    ModularScreen getScreen();

    default void handleDrawBackground(int tint, IntConsumer drawFunction) {
        if (ClientScreenHandler.shouldDrawWorldBackground()) {
            drawFunction.accept(tint);
        }
        ClientScreenHandler.drawDarkBackground((GuiScreen) this, tint);
    }

    default void updateGuiArea(Rectangle area) {
        if (this instanceof GuiContainer container) {
            ClientScreenHandler.updateGuiArea(container, area);
        }
    }
}
