package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PopupMenu<W extends PopupMenu<W>> extends Widget<W> {

    private final MenuWrapper menu;
    private final List<IWidget> children;

    public PopupMenu(IWidget child) {
        this.menu = new MenuWrapper(child);
        child.flex().relative(this.getArea());
        this.menu.setEnabled(false);
        this.children = Collections.singletonList(this.menu);
    }

    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return this.children;
    }

    @Override
    public void onMouseStartHover() {
        this.menu.setEnabled(true);
        this.menu.mightClose = false;
    }

    @Override
    public void onMouseEndHover() {
        this.menu.mightClose = true;
    }

    public static class MenuWrapper extends Widget<MenuWrapper> {

        private final IWidget child;
        private final List<IWidget> children;
        private boolean mightClose = false;

        private MenuWrapper(IWidget child) {
            this.child = child;
            this.children = Collections.singletonList(child);
            flex().coverChildren().cancelMovementX().cancelMovementY();
        }

        @Override
        public @NotNull List<IWidget> getChildren() {
            return this.children;
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            if (this.mightClose && !isBelowMouse()) {
                setEnabled(false);
            }
        }

        public void setMightClose(boolean mightClose) {
            this.mightClose = mightClose;
        }
    }
}
