package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class TabContainer extends PagedWidget<TabContainer> implements ILayoutWidget {

    private final List<IWidget> allChildren = new ArrayList<>();
    private final List<TabButton> tabButtons = new ArrayList<>();
    private Side buttonBarSide = Side.TOP;
    private int tabButtonWidth = 0, tabButtonHeight = 0;

    public TabContainer() {
        useThemeBackground(false).useThemeHoverBackground(false);
    }

    @Override
    public void onInit() {
        this.allChildren.addAll(getPages());
        this.allChildren.addAll(this.tabButtons);
        if (this.tabButtonWidth > 0 || this.tabButtonHeight > 0) {
            for (TabButton tabButton : this.tabButtons) {
                if (this.tabButtonWidth > 0 && !tabButton.flex().hasWidth()) {
                    tabButton.width(this.tabButtonWidth);
                }
                if (this.tabButtonHeight > 0 && !tabButton.flex().hasHeight()) {
                    tabButton.height(this.tabButtonHeight);
                }
            }
        }
    }

    @Override
    public void layoutWidgets() {
        int current = 0;
        for (TabButton tabButton : this.tabButtons) {
            if (tabButton.flex().hasXPos() || tabButton.flex().hasYPos()) {
                continue;
            }
            if (current == 0) {
                tabButton.start();
            }
            int x, y;
            int inset = tabButton.getTextureInset();
            if (this.buttonBarSide.horizontal) {
                x = current;
                current += tabButton.getArea().w();
                if (this.buttonBarSide.positive) {
                    y = -tabButton.getArea().h() + inset;
                } else {
                    y = getArea().h() - inset;
                }
            } else {
                if (this.buttonBarSide.positive) {
                    x = -tabButton.getArea().w() + inset;
                } else {
                    x = getArea().w() - inset;
                }
                y = current;
                current += tabButton.getArea().h();
            }
            tabButton.getArea().rx = x;
            tabButton.getArea().ry = y;
            tabButton.updateDefaultTexture();
        }
    }

    @Override
    public @Unmodifiable @NotNull List<IWidget> getChildren() {
        return this.allChildren;
    }

    public TabContainer tabButton(TabButton tabButton) {
        this.tabButtons.add(tabButton);
        tabButton.setTabContainer(this);
        return this;
    }

    public TabContainer buttonBarSide(Side buttonBarSide) {
        this.buttonBarSide = buttonBarSide;
        return this;
    }

    public TabContainer tabButtonSize(int width, int height) {
        this.tabButtonWidth = width;
        this.tabButtonHeight = height;
        return this;
    }

    public Side getButtonBarSide() {
        return buttonBarSide;
    }

    public enum Side {

        LEFT(false, true),
        RIGHT(false, false),
        TOP(true, true),
        BOTTOM(true, false);

        public final boolean horizontal, positive;

        Side(boolean horizontal, boolean positive) {
            this.horizontal = horizontal;
            this.positive = positive;
        }
    }
}
