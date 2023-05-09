package com.cleanroommc.modularui.widgets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;

public class DropDownMenu extends SingleChildWidget<DropDownMenu> implements Interactable {
    private DropDownWrapper menu = new DropDownWrapper();

    public DropDownMenu() {
        menu.setEnabled(false);
        menu.background(GuiTextures.BUTTON);
        child(menu);
        overlay(GuiTextures.ARROW_UP);
    }

    public int getSelectedIndex() {
        return menu.getCurrentIndex();
    }

    public DropDownMenu setSelectedIndex(int index) {
        menu.setCurrentIndex(index);
        return getThis();
    }

    public DropDownMenu addChoice(Interactable button) {
        menu.addChoice(button);
        return getThis();
    }

    public DropDownMenu addChoice(IGuiAction.MouseReleased onSelect, IDrawable... drawable) {
        addChoice(new ButtonWidget<>().onMouseReleased(onSelect).onMouseTapped(m -> { menu.setStatus(false); overlay(GuiTextures.ARROW_UP); return true; }).background(GuiTextures.BUTTON).overlay(drawable));
        return getThis();
    }

    public DropDownMenu setDropDownDirection(DropDownDirection direction) {
        menu.setDropDownDirection(direction);
        if (direction == DropDownDirection.UP) {
            overlay(GuiTextures.ARROW_DOWN);
        } else {
            overlay(GuiTextures.ARROW_UP);
        }
        return getThis();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!menu.isOpen()) {
            menu.setStatus(true);
            menu.setEnabled(true);
            overlay(GuiTextures.ARROW_DOWN);
            return Result.SUCCESS;
        }

        menu.setStatus(false);
        menu.setEnabled(false);
        overlay(GuiTextures.ARROW_UP);
        return Result.SUCCESS;
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

    private static class DropDownWrapper extends Widget<DropDownWrapper> {
        private DropDownDirection direction = DropDownDirection.DOWN;
        ListWidget<Object, ButtonWidget<?>, ?> widgets = new ListWidget<>(obj -> {return (ButtonWidget<?>) obj;}, val -> val);
        List<IWidget> children = Collections.singletonList(widgets);
        private boolean open;
        private int lastIndex = 0;
        private int currentIndex = 0;

        public void setDropDownDirection(DropDownDirection direction) {
            this.direction = direction;
        }
        @Override
        public void initialise(@NotNull IWidget parent) {
            super.initialise(parent);
        }

        @Override
        public void onFrameUpdate() {
            if (!open) {
                setEnabled(false);
            }
        }

        public void setStatus(boolean open) {
            if (direction == DropDownDirection.UP) {
                widgets.top(-getParent().getArea().height-widgets.getArea().height);
            } else {
                widgets.top(getParent().getArea().height);
            }
            widgets.align(Alignment.Center);
            widgets.flex().coverChildren();
            coverChildren();
            this.open = open;
        }

        public boolean isOpen() {
            return open;
        }

        public void addChoice(Interactable button) {
            widgets.add(button, lastIndex++);
            widgets.coverChildren();
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public @NotNull List<? extends IWidget> getChildren() {
            return children;
        }
    }
}
