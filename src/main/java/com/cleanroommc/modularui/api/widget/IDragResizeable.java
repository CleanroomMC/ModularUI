package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.layout.IViewportStack;
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

    static ResizeDragArea getDragResizeCorner(IDragResizeable widget, Area area, IViewportStack stack, int x, int y) {
        if (!widget.isCurrentlyResizable()) return null;

        int mx = stack.unTransformX(x, y);
        int my = stack.unTransformY(x, y);

        if (mx < 0 || my < 0 || mx > area.w() || my > area.h()) return null;

        int ras = widget.getDragAreaSize();
        if (mx < ras) {
            if (my < ras) return ResizeDragArea.TOP_LEFT;
            if (my > area.h() - ras) return ResizeDragArea.BOTTOM_LEFT;
            return ResizeDragArea.LEFT;
        }
        if (mx > area.w() - ras) {
            if (my < ras) return ResizeDragArea.TOP_RIGHT;
            if (my > area.h() - ras) return ResizeDragArea.BOTTOM_RIGHT;
            return ResizeDragArea.RIGHT;
        }
        if (my < ras) return ResizeDragArea.TOP;
        if (my > area.h() - ras) return ResizeDragArea.BOTTOM;
        return null;
    }

    static void applyDrag(IDragResizeable resizeable, IWidget widget, ResizeDragArea dragArea, Area startArea, int dx, int dy) {
        if (dx != 0) {
            if (dragArea.left) {
                widget.flex().left(startArea.rx + dx);
                widget.flex().width(startArea.width - dx);
            } else if (dragArea.right) {
                widget.flex().left(startArea.rx);
                widget.flex().width(startArea.width + dx);
            }
        }
        if (dy != 0) {
            if (dragArea.top) {
                widget.flex().top(startArea.ry + dy);
                widget.flex().height(startArea.height - dy);
            } else if (dragArea.bottom) {
                widget.flex().top(startArea.ry);
                widget.flex().height(startArea.height + dy);
            }
        }
        resizeable.onDragResize();
    }

}
