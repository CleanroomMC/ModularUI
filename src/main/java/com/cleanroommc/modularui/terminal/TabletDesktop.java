package com.cleanroommc.modularui.terminal;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;

public class TabletDesktop extends ModularPanel {

    private static final UITexture FRAME = UITexture.fullImage(ModularUI.ID, "gui/tablet/tablet_frame", false);
    private static final int taskbarHeight = 11;

    private final TabletTaskbar tabletTaskbar;

    private boolean taskBarTop = false;

    public TabletDesktop(GuiContext context) {
        super(context);
        align(Alignment.Center).size(307, 230)
                .background(new Rectangle().setVerticalGradient(0xFFDDDDDD, 0xFFAAAAAA));

        this.tabletTaskbar = new TabletTaskbar()
                .height(taskbarHeight).width(1f)
                .background(new Rectangle().setColor(Color.withAlpha(0, 0.5f)));
        if (this.taskBarTop) {
            this.tabletTaskbar.top(0);
        } else {
            this.tabletTaskbar.bottom(0);
        }
    }

    public void drawFrame(GuiContext context) {
        FRAME.draw(context, -5, -5, getArea().width + 10, getArea().height + 10);
    }

    public void openApp(String name) {
        TabletApp app = AppRegistry.createApp(name);
        if (app != null) {
            app.width(1f).top(0).bottom(taskbarHeight);
            getScreen().openPanel(app);
        }
    }

    public TabletTaskbar getTabletTaskbar() {
        return tabletTaskbar;
    }
}
