package com.cleanroommc.modularui.tablet.app;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.tablet.TabletDesktop;
import com.cleanroommc.modularui.tablet.TabletScreen;

public abstract class TabletApp extends ModularPanel {

    public TabletApp(GuiContext context) {
        super(context);
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    public TabletDesktop getDesktop() {
        return ((TabletScreen) getScreen()).getDesktop();
    }
}
