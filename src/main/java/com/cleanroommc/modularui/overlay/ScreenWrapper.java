package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.GuiScreen;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Wraps the current gui screen and uses it for overlays.
 */
@ApiStatus.Experimental
public class ScreenWrapper implements IMuiScreen {

    private final GuiScreen guiScreen;
    private final ModularScreen screen;

    public ScreenWrapper(GuiScreen guiScreen, ModularScreen screen) {
        this.guiScreen = guiScreen;
        this.screen = screen;
    }

    @Override
    public @NotNull ModularScreen getScreen() {
        return screen;
    }

    @Override
    public GuiScreen getGuiScreen() {
        return guiScreen;
    }

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
