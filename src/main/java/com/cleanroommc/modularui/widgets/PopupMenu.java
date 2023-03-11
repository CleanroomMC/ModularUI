package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PopupMenu<W extends PopupMenu<W>> extends Widget<W> {

    private final Menu menu;
    private final List<IWidget> children;

    public PopupMenu(IWidget child) {
        this.menu = new Menu(child);
        child.flex().relative(this);
        this.menu.setEnabled(false);
        this.children = Collections.singletonList(this.menu);
    }

    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return children;
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

    private static class Menu extends Widget<Menu> {

        private final IWidget child;
        private final List<IWidget> children;
        private boolean mightClose = false;

        private Menu(IWidget child) {
            this.child = child;
            this.children = Collections.singletonList(child);
            flex().coverChildren().cancelMovementX().cancelMovementY();
        }

        @Override
        public @NotNull List<IWidget> getChildren() {
            return children;
        }

        @Override
        public void onFrameUpdate() {
            if (this.mightClose && !isBelowMouse()) {
                setEnabled(false);
            }
        }
    }
}
