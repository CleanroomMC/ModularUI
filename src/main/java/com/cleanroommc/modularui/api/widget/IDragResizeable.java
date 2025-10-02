package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.GuiScreen;

/**
 * Implement this interface on a {@link IWidget} to allow it being resized by dragging the edges similar to windows.
 */
public interface IDragResizeable {

    /**
     * @return if this widget can currently be resized by dragging an edge
     */
    default boolean isCurrentlyResizable() {
        return true;
    }

    /**
     * @return if the center position of this widget should be retained, by also resizing the opposite edge
     */
    default boolean keepPosOnDragResize() {
        return true;
    }

    /**
     * Called every time the mouse moves one or more pixel while this widget is resized by dragging an edge.
     */
    default void onDragResize() {
        ((IWidget) this).scheduleResize();
    }

    /**
     * @return The border size in witch to allow drag resizing in pixels.
     */
    default int getDragAreaSize() {
        return 3;
    }

    /**
     * @return The minimum width this widget can be dragged to.
     */
    default int getMinDragWidth() {
        return 18;
    }

    /**
     * @return The minimum height this widget can be dragged to.
     */
    default int getMinDragHeight() {
        return 18;
    }

    /**
     * An internal method to detect if the mouse is currently hovering an area where a drag resize can be started.
     */
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

    /**
     * An internal method to actually resize the widget while an edge is being dragged.
     */
    static void applyDrag(IDragResizeable resizeable, IWidget widget, ResizeDragArea dragArea, Area startArea, int dx, int dy) {
        int keepPosFactor = resizeable.keepPosOnDragResize() || GuiScreen.isShiftKeyDown() ? 2 : 1;
        if (dx != 0) {
            if (dragArea.left) {
                int s = startArea.width - dx * keepPosFactor;
                if (s >= resizeable.getMinDragWidth()) {
                    widget.flex().left(startArea.rx + dx);
                    widget.flex().width(s);
                }
            } else if (dragArea.right) {
                int s = startArea.width + dx * keepPosFactor;
                if (s >= resizeable.getMinDragWidth()) {
                    widget.flex().left(startArea.rx - dx * (keepPosFactor - 1));
                    widget.flex().width(s);
                }
            }
        }
        if (dy != 0) {
            if (dragArea.top) {
                int s = startArea.height - dy * keepPosFactor;
                if (s >= resizeable.getMinDragHeight()) {
                    widget.flex().top(startArea.ry + dy);
                    widget.flex().height(s);
                }
            } else if (dragArea.bottom) {
                int s = startArea.height + dy * keepPosFactor;
                if (s >= resizeable.getMinDragHeight()) {
                    widget.flex().top(startArea.ry - dy * (keepPosFactor - 1));
                    widget.flex().height(s);
                }
            }
        }
        resizeable.onDragResize();
    }

}
