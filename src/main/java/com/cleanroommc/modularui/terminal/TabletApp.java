package com.cleanroommc.modularui.terminal;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

public abstract class TabletApp extends ModularPanel {

    public TabletApp(GuiContext context) {
        super(context);
    }

    public abstract IDrawable getIcon();

    public TabletDesktop getDesktop() {
        return ((TabletScreen) getScreen()).getDesktop();
    }
}
