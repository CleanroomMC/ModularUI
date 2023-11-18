package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.drawable.GuiDraw;

public class VerticalScrollData extends ScrollData {

    public VerticalScrollData() {
        this(false);
    }

    public VerticalScrollData(boolean leftAlignment) {
        this(leftAlignment, 4);
    }

    public VerticalScrollData(boolean leftAlignment, int thickness) {
        super(GuiAxis.Y, leftAlignment, thickness);
    }

    @Override
    public float getProgress(ScrollArea area, int x, int y) {
        return (y - area.y) / (float) getFullVisibleSize(area);
    }

    @Override
    public HorizontalScrollData getOtherScrollData(ScrollArea area) {
        return area.getScrollX();
    }

    @Override
    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!area.isInside(x, y) || !isScrollBarActive(area)) {
            return false;
        }
        int scrollbar = getThickness();
        ScrollData data = getOtherScrollData(area);
        if (data != null && isOtherScrollBarActive(area, true)) {
            int thickness = data.getThickness();
            if (data.isOnAxisStart() ? y < area.y + thickness : y >= area.ey() - thickness) {
                return false;
            }
        }
        return isOnAxisStart() ? x >= area.x && x < area.x + scrollbar : x >= area.ex() - scrollbar && x < area.ex();
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

        y = ((getFullVisibleSize(area, isOtherActive) - l) * getScroll()) / (getScrollSize() - getVisibleSize(area, isOtherActive));
        ScrollData data2 = getOtherScrollData(area);
        if (data2 != null && isOtherActive && data2.isOnAxisStart()) {
            y += data2.getThickness();
        }
        h = l;
        drawScrollBar(x, y, w, h);
    }

    @Override
    public boolean onMouseClicked(ScrollArea area, int x, int y, int button) {
        if (isOnAxisStart() ? x <= area.x + getThickness() : x >= area.ex() - getThickness()) {
            this.dragging = true;
            return true;
        }
        return false;
    }
}
