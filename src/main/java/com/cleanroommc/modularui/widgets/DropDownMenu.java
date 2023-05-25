package com.cleanroommc.modularui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.keys.StringKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

public class DropDownMenu extends SingleChildWidget<DropDownMenu> implements Interactable {
    private static final StringKey NONE = new StringKey("None");
    private DropDownWrapper menu = new DropDownWrapper();
    private IDrawable arrowClosed;
    private IDrawable arrowOpened;

    public DropDownMenu() {
        menu.setEnabled(false);
        menu.background(GuiTextures.BUTTON);
        child(menu);
        setArrows(GuiTextures.ARROW_UP, GuiTextures.ARROW_DOWN);
    }

    public int getSelectedIndex() {
        return menu.getCurrentIndex();
    }

    public DropDownMenu setSelectedIndex(int index) {
        menu.setCurrentIndex(index);
        return getThis();
    }

    public DropDownMenu addChoice(ButtonWidget<?> button) {
        menu.addChoice(button);
        return getThis();
    }

    public DropDownMenu setArrows(IDrawable arrowClosed, IDrawable arrowOpened) {
        this.arrowClosed = arrowClosed;
        this.arrowOpened = arrowOpened;
        return getThis();
    }

    public DropDownMenu addChoice(IGuiAction.MouseReleased onSelect, IDrawable... drawable) {
        ButtonWidget<?> button = new ButtonWidget<>();
        return addChoice(button.onMouseReleased(onSelect).onMouseTapped(m -> {
                menu.setStatus(false);
                menu.setSelectedWidget(button);
                return true; 
            })
            .background(GuiTextures.BUTTON)
            .overlay(drawable));
    }

    public DropDownMenu addChoice(IGuiAction.MouseReleased onSelect, String text) {
        return addChoice(onSelect, new StringKey(text));
    }

    public DropDownMenu setDropDownDirection(DropDownDirection direction) {
        menu.setDropDownDirection(direction);
        return getThis();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!menu.isOpen()) {
            menu.setStatus(true);
            menu.setEnabled(true);
            return Result.SUCCESS;
        }
        menu.setStatus(false);
        menu.setEnabled(false);
        return Result.SUCCESS;
    }

    @Override
    public void draw(GuiContext context) {
        super.draw(context);
        Area area = getArea();
        int smallerSide = area.width > area.height ? area.height : area.width;
        if (menu.getSelectedWidget() != null) {
            menu.getSelectedWidget().setEnabled(true);
            menu.getSelectedWidget().draw(context);
        } else {
            NONE.draw(context, 0, 0, area.width - smallerSide, area.height);
        }

        if (menu.isOpen()) {
            arrowOpened.draw(context, area.width - smallerSide , 0, smallerSide, smallerSide);
        } else {
            arrowClosed.draw(context, area.width - smallerSide , 0, smallerSide, smallerSide);
        }
    }

    @Override
    public DropDownMenu background(IDrawable... background) {
        menu.background(background);
        return super.background(background);
    }

    public static class DropDownDirection {
        public static final DropDownDirection UP = new DropDownDirection(0, -1);
        public static final DropDownDirection DOWN = new DropDownDirection(0, 1);

        private int xOffset = 0;
        private int yOffset = 0;

        private DropDownDirection(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public int getXOffset() {
            return xOffset;
        }

        public int getYOffset() {
            return yOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DropDownDirection)) {
                return false;
            }
            DropDownDirection dir = (DropDownDirection)obj;
            return dir.xOffset == this.xOffset && dir.yOffset == this.yOffset;
        }
    }

    private static class DropDownWrapper extends ScrollWidget<DropDownWrapper> {
        private DropDownDirection direction = DropDownDirection.DOWN;
        private int maxItemsOnDisplay = 10;
        private List<IWidget> children = new ArrayList<>();
        private IWidget selectedWidget = null;
        private boolean open;
        private int count = 0;
        private int currentIndex = 0;

        public DropDownWrapper() {
            super();
        }

        public void setDropDownDirection(DropDownDirection direction) {
            this.direction = direction;
        }

        @Override
        public void onFrameUpdate() {
            if (!open) {
                setEnabled(false);
            }
        }

        public void setStatus(boolean open) {
            Area parentArea = getParent().getArea();
            size(parentArea.width, parentArea.height * maxItemsOnDisplay);
            if (direction == DropDownDirection.UP) {
                bottom(parentArea.height * (maxItemsOnDisplay + 1));
            } else {
                top(parentArea.height);
            }
            for (IWidget child : getChildren()) {
                child.getArea().setSize(parentArea.width, parentArea.height);
                child.setEnabled(open);
            }
            this.open = open;
        }

        public boolean isOpen() {
            return open;
        }

        public void addChoice(ButtonWidget<?> button) {
            children.add(currentIndex, button);
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public @NotNull List<IWidget> getChildren() {
            return children;
        }

        public IWidget getSelectedWidget() {
            return selectedWidget;
        }

        public void setSelectedWidget(IWidget selectedWidget) {
            this.selectedWidget = selectedWidget;
        }

        @Override
        public DropDownWrapper background(IDrawable... background) {
            for (IWidget child : getChildren()) {
                if (!(child instanceof Widget<?>)) continue;
                Widget<?> childAsWidget = (Widget<?>) child;
                childAsWidget.background(background);
            }
            return super.background();
        }
    }
}
