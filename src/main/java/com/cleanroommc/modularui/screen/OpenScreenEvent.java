package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IMuiScreen;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.Event;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenScreenEvent extends Event {

    private final GuiScreen screen;
    private final List<ModularScreen> overlays = new ArrayList<>();

    public OpenScreenEvent(GuiScreen screen) {
        this.screen = screen;
    }

    public GuiScreen getScreen() {
        return screen;
    }

    public boolean isModularScreen() {
        return screen instanceof IMuiScreen;
    }

    public @Nullable ModularScreen getModularScreen() {
        return screen instanceof IMuiScreen muiScreen ? muiScreen.getScreen() : null;
    }

    public List<ModularScreen> getOverlays() {
        return overlays;
    }

    public void addOverlay(ModularScreen screen) {
        this.overlays.add(screen);
    }
}
