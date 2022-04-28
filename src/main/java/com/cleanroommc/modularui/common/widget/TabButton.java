package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;

import javax.annotation.Nullable;

public class TabButton extends Widget implements Interactable {

    private final int page;
    private TabContainer tabController;
    private IDrawable[] activeBackground;

    public TabButton(int page) {
        this.page = page;
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (page != tabController.getCurrentPage()) {
            tabController.setActivePage(page);
        }
        return ClickResult.ACCEPT;
    }

    protected void setTabController(TabContainer tabController) {
        this.tabController = tabController;
    }

    public int getPage() {
        return page;
    }

    @Nullable
    @Override
    public IDrawable[] getBackground() {
        if (isSelected() && activeBackground != null) {
            return activeBackground;
        }
        return super.getBackground();
    }

    public boolean isSelected() {
        return page == tabController.getCurrentPage();
    }

    public TabButton setBackground(boolean active, IDrawable... drawables) {
        if (active) {
            this.activeBackground = drawables;
        } else {
            setBackground(drawables);
        }
        return this;
    }
}
