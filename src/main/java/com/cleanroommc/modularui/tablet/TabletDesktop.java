package com.cleanroommc.modularui.tablet;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.tablet.app.AppRegistry;
import com.cleanroommc.modularui.tablet.app.IApp;
import com.cleanroommc.modularui.tablet.app.TabletApp;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

public class TabletDesktop extends ModularPanel {

    private static final UITexture FRAME = UITexture.fullImage(ModularUI.ID, "gui/tablet/tablet_frame", false);
    private static final int taskbarHeight = 11;
    private static final int screenWidth = 307, screenHeight = 230;

    private final TabletTaskbar tabletTaskbar;

    private boolean taskBarTop = false;

    public TabletDesktop(GuiContext context) {
        super(context);
        align(Alignment.Center).size(screenWidth, screenHeight) ;

        this.tabletTaskbar = new TabletTaskbar()
                .height(taskbarHeight).width(1f)
                .background(new Rectangle().setColor(Color.withAlpha(0, 0.5f)))
                .child(new ButtonWidget<>()
                        .width(11).height(taskbarHeight)
                        .right(0)
                        .background(GuiTextures.CLOSE.asIcon().size(10))
                        .onMousePressed(mouseButton -> {
                            getScreen().getWindowManager().closeTopPanel(false, true);
                            return true;
                        }));
        child(this.tabletTaskbar);
        if (this.taskBarTop) {
            this.tabletTaskbar.top(0);
        } else {
            this.tabletTaskbar.bottom(0);
        }
        child(new Grid()
                .left(5).right(5).top(5).bottom(taskbarHeight + 5)
                .minColWidth(25)
                .minRowHeight(25)
                .mapTo(12, AppRegistry.appsView, (i, app) -> new ButtonWidget<>()
                        .size(20, 20)
                        .background(app.getIcon())
                        .onMousePressed(mouseButton -> {
                            openApp(app);
                            return true;
                        })));
    }

    public void drawFrame(GuiContext context) {
        FRAME.draw(context, -5, -5, getArea().width + 10, getArea().height + 10);
    }

    public void openApp(IApp<?> app) {
        if (getScreen().isPanelOpen(app.getName())) return;
        TabletApp tabletApp = app.createApp(getContext());
        tabletApp.name(app.getName()).width(1f).top(0).bottom(taskbarHeight).relative(this);
        getScreen().openPanel(tabletApp);
    }

    public TabletTaskbar getTabletTaskbar() {
        return tabletTaskbar;
    }
}
