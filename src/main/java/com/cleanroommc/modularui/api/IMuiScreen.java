package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.IntConsumer;

/**
 * Implement this interface on a {@link GuiScreen} to be able to use it as a custom wrapper.
 * See {@link com.cleanroommc.modularui.screen.GuiScreenWrapper GuiScreenWrapper} and {@link com.cleanroommc.modularui.screen.GuiContainerWrapper GuiContainerWrapper}
 * for default implementations.
 */
public interface IMuiScreen {

    @NotNull
    ModularScreen getScreen();

    default void setFocused(boolean focused) {
        getGuiScreen().setFocused(focused);
    }

    default void handleDrawBackground(int tint, IntConsumer drawFunction) {
        if (ClientScreenHandler.shouldDrawWorldBackground()) {
            drawFunction.accept(tint);
        }
        ClientScreenHandler.drawDarkBackground(getGuiScreen(), tint);
    }

    default void updateGuiArea(Rectangle area) {
        if (getGuiScreen() instanceof GuiContainer container) {
            ClientScreenHandler.updateGuiArea(container, area);
        }
    }

    default boolean isGuiContainer() {
        return getGuiScreen() instanceof GuiContainer;
    }

    default void setHoveredSlot(Slot slot) {
        if (getGuiScreen() instanceof GuiContainerAccessor acc) {
            acc.setHoveredSlot(slot);
        }
    }

    default GuiScreen getGuiScreen() {
        return (GuiScreen) this;
    }
}
