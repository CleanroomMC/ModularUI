package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class TabContainer extends PageControlWidget<TabContainer> implements ILayoutWidget {

    private final List<IWidget> allChildren = new ArrayList<>();
    private final List<TabButton> tabButtons = new ArrayList<>();
    private Side buttonBarSide = Side.TOP;

    @Override
    public void onInit() {
        this.allChildren.addAll(getPages());
        this.allChildren.addAll(this.tabButtons);
    }

    @Override
    public void layoutWidgets() {
        int current = 0;
        for (TabButton tabButton : this.tabButtons) {
            if (current == 0) {
                tabButton.start();
            }
            int x, y;
            if (this.buttonBarSide.horizontal) {
                x = current;
                current += tabButton.getArea().w();
                if (this.buttonBarSide.positive) {
                    y = -tabButton.getArea().h() + 4;
                } else {
                    y = getArea().h() - 4;
                }
            } else {
                if (this.buttonBarSide.positive) {
                    x = -tabButton.getArea().w() + 4;
                } else {
                    x = getArea().w() - 4;
                }
                y = current;
                current += tabButton.getArea().h();
            }
            tabButton.flex().setRelativeX(x);
            tabButton.flex().setRelativeY(y);
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
