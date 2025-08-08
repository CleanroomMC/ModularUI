package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;

public interface IDragResizeable {

    default boolean isCurrentlyResizable() {
        return true;
    }

    default void onDragResize() {
        ((IWidget) this).scheduleResize();
    }

    default int getDragAreaSize() {
        return 3;
    }

    static Corner getDragResizeCornerUnderMouse(IDragResizeable widget, Area area, GuiContext context) {
        if (!widget.isCurrentlyResizable()) return null;

        int mx = context.getMouseX();
        int my = context.getMouseY();

        if (mx < 0 || my < 0 || mx > area.w() || my > area.h()) return null;

        int ras = widget.getDragAreaSize();
        if (mx < ras) {
            if (my < ras) return Corner.TOP_LEFT;
            if (my > area.h() - ras) return Corner.BOTTOM_LEFT;
            return null;
        }
        if (mx > area.w() - ras) {
            if (my < ras) return Corner.TOP_RIGHT;
            if (my > area.h() - ras) return Corner.BOTTOM_RIGHT;
        }
        return null;
    }

    static void applyDrag(IDragResizeable resizeable, IWidget widget, Corner corner, Area startArea, int dx, int dy) {
        if (dx != 0) {
            if (corner.left) {
                widget.flex().left(startArea.rx + dx);
                widget.flex().width(startArea.width - dx);
            } else {
                widget.flex().left(startArea.rx);
                widget.flex().width(startArea.width + dx);
            }
        }
        if (dy != 0) {
            if (corner.top) {
                widget.flex().top(startArea.ry + dy);
                widget.flex().height(startArea.height - dy);
            } else {
                widget.flex().top(startArea.ry);
                widget.flex().height(startArea.height + dy);
            }
        }
        resizeable.onDragResize();
    }

    enum Corner {
        TOP_LEFT(true, true),
        TOP_RIGHT(true, false),
        BOTTOM_LEFT(false, true),
        BOTTOM_RIGHT(false, false);

        public final boolean top, left;

        Corner(boolean top, boolean left) {
            this.top = top;
            this.left = left;
        }
    }
}
