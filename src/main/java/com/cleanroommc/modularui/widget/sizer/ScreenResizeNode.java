package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.screen.ModularScreen;

public class ScreenResizeNode extends StaticResizer {

    private final ModularScreen screen;

    public ScreenResizeNode(ModularScreen screen) {
        this.screen = screen;
    }

    public ModularScreen getScreen() {
        return screen;
    }

    @Override
    public Area getArea() {
        return screen.getScreenArea();
    }

    @Override
    public String getDebugDisplayName() {
        return "screen '" + this.screen + "'";
    }

    @Override
    public String toString() {
        return "ScreenResizeNode(" + this.screen + ")";
    }
}
