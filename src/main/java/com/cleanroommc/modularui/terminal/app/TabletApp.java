package com.cleanroommc.modularui.terminal.app;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.terminal.TabletDesktop;
import com.cleanroommc.modularui.terminal.TabletScreen;

public abstract class TabletApp extends ModularPanel {

    public TabletApp(GuiContext context) {
        super(context);
        background(TabletDesktop.BACKGROUND);
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    public TabletDesktop getDesktop() {
        return ((TabletScreen) getScreen()).getDesktop();
    }
}
