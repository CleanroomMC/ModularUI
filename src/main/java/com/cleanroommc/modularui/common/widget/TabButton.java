package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.math.Pos2d;

import javax.annotation.Nullable;

public class TabButton extends Widget implements Interactable {

    private final int page;
    private TabContainer tabController;
    private IWidgetDrawable activeBackground;

    public TabButton(int page) {
        this.page = page;
    }

    @Override
    public boolean onClick(int buttonId, boolean doubleClick) {
        if (page != tabController.getCurrentPage()) {
            tabController.setActivePage(page);
            return true;
        }
        return false;
    }

    protected void setTabController(TabContainer tabController) {
        this.tabController = tabController;
    }

    public int getPage() {
        return page;
    }

    @Nullable
    @Override
    public IWidgetDrawable getDrawable() {
        if (isSelected() && activeBackground != null) {
            return activeBackground;
        }
        return super.getDrawable();
    }

    public boolean isSelected() {
        return page == tabController.getCurrentPage();
    }

    public TabButton setBackground(boolean active, IDrawable... drawables) {
        return setBackground(active, ((widget, partialTicks) -> {
            for (IDrawable drawable : drawables) {
                drawable.draw(Pos2d.ZERO, widget.getSize(), partialTicks);
            }
        }));
    }

    public TabButton setBackground(boolean active, @Nullable IWidgetDrawable drawable) {
        if (active) {
            this.activeBackground = drawable;
        } else {
            super.setBackground(drawable);
        }
        return this;
    }
}
