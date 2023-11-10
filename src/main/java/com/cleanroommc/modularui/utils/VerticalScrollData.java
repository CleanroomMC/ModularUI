package com.cleanroommc.modularui.utils;

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
    public int getFullVisibleSize(ScrollArea area) {
        return area.w();
    }

    @Override
    public int getVisibleSize(ScrollArea area) {
        return Math.max(0, getFullVisibleSize(area) - area.getPadding().vertical());
    }

    @Override
    public float getProgress(ScrollArea area, int x, int y) {
        return (y - area.y) / (float) getFullVisibleSize(area);
    }

    @Override
    public HorizontalScrollData getOtherScrollData() {
        return null;
    }

    @Override
    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!area.isInside(x, y) || !isScrollBarActive(area)) {
            return false;
        }
        int scrollbar = getThickness();
        ScrollData data = getOtherScrollData();
        if (data != null && isOtherScrollBarActive(area)) {
            int thickness = data.getThickness();
            if (data.isOnAxisStart() ? y < area.y + thickness : y >= area.ey() - thickness) {
                return false;
            }
        }
        return isOnAxisStart() ? x >= area.x && x < area.x + scrollbar : x >= area.ex() - scrollbar && x < area.ex();
    }

    @Override
    public void drawScrollbar(ScrollArea area) {
        int l = this.getScrollBarLength(area);
        int x = isOnAxisStart() ? 0 : area.w() - getThickness();
        int y = 0;
        int w = getThickness();
        int h = area.height;
        GuiDraw.drawRect(x, y, w, h, area.getScrollBarBackgroundColor());

        y = ((getFullVisibleSize(area) - l) * getScroll()) / (getScrollSize() - getVisibleSize(area));
        ScrollData data2 = getOtherScrollData();
        if (data2 != null && isOtherScrollBarActive(area) && data2.isOnAxisStart()) {
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
