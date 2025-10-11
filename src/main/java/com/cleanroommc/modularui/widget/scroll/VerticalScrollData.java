package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.drawable.GuiDraw;

public class VerticalScrollData extends ScrollData {

    /**
     * Creates vertical scroll data which handles scrolling and scroll bar.
     * Scrollbar is 4 pixel wide and is placed on the right.
     */
    public VerticalScrollData() {
        this(false, DEFAULT_THICKNESS);
    }

    /**
     * Creates vertical scroll data which handles scrolling and scroll bar.
     * Scrollbar is 4 pixel wide.
     *
     * @param leftAlignment if the scroll bar should be placed on the left
     */
    public VerticalScrollData(boolean leftAlignment) {
        this(leftAlignment, DEFAULT_THICKNESS);
    }

    /**
     * Creates vertical scroll data which handles scrolling and scroll bar.
     *
     * @param leftAlignment if the scroll bar should be placed on the left
     * @param thickness     width of the scroll bar in pixel
     */
    public VerticalScrollData(boolean leftAlignment, int thickness) {
        super(GuiAxis.Y, leftAlignment, thickness);
    }

    public VerticalScrollData cancelScrollEdge(boolean cancelScrollEdge) {
        setCancelScrollEdge(cancelScrollEdge);
        return this;
    }

    @Override
    public HorizontalScrollData getOtherScrollData(ScrollArea area) {
        return area.getScrollX();
    }

    @Override
    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!isScrollBarActive(area)) {
            return false;
        }
        int scrollbar = getThickness();
        ScrollData data = getOtherScrollData(area);
        if (data != null && isOtherScrollBarActive(area, true)) {
            int thickness = data.getThickness();
            if (data.isOnAxisStart() ? y < thickness : y >= area.h() - thickness) {
                return false;
            }
        }
        return isOnAxisStart() ? x >= 0 && x < scrollbar : x >= area.w() - scrollbar && x < area.w();
    }

    @Override
    public void drawScrollbar(ScrollArea area) {
        boolean isOtherActive = isOtherScrollBarActive(area, true);
        int l = this.getScrollBarLength(area);
        int x = isOnAxisStart() ? 0 : area.w() - getThickness();
        int y = 0;
        int w = getThickness();
        int h = area.height;
        GuiDraw.drawRect(x, y, w, h, area.getScrollBarBackgroundColor());

        y = getScrollBarStart(area, l, isOtherActive);
        ScrollData data2 = getOtherScrollData(area);
        if (data2 != null && isOtherActive && data2.isOnAxisStart()) {
            y += data2.getThickness();
        }
        h = l;
        drawScrollBar(x, y, w, h);
    }
}
