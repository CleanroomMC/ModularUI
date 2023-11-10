package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.drawable.GuiDraw;

public class HorizontalScrollData extends ScrollData {

    public HorizontalScrollData() {
        this(false);
    }

    public HorizontalScrollData(boolean topAlignment) {
        this(topAlignment, 4);
    }

    public HorizontalScrollData(boolean topAlignment, int thickness) {
        super(GuiAxis.X, topAlignment, thickness);
    }

    @Override
    public int getFullVisibleSize(ScrollArea area) {
        return area.w();
    }

    @Override
    public int getVisibleSize(ScrollArea area) {
        return Math.max(0, getFullVisibleSize(area) - area.getPadding().horizontal());
    }

    @Override
    public float getProgress(ScrollArea area, int x, int y) {
        return (x - area.x) / (float) getFullVisibleSize(area);
    }

    @Override
    public VerticalScrollData getOtherScrollData() {
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
            if (data.isOnAxisStart() ? x < area.x + thickness : x >= area.ex() - thickness) {
                return false;
            }
        }
        return isOnAxisStart() ? y >= area.y && y < area.y + scrollbar : y >= area.ey() - scrollbar && y < area.ey();
    }

    @Override
    public void drawScrollbar(ScrollArea area) {
        int l = getScrollBarLength(area);
        int x = 0;
        int y = isOnAxisStart() ? 0 : area.height - getThickness();
        int w = area.width;
        int h = getThickness();
        GuiDraw.drawRect(x, y, w, h, area.getScrollBarBackgroundColor());

        x = ((getFullVisibleSize(area) - l) * getScroll()) / (getScrollSize() - getVisibleSize(area));
        ScrollData data2 = getOtherScrollData();
        if (data2 != null && isOtherScrollBarActive(area) && data2.isOnAxisStart()) {
            x += data2.getThickness();
        }

        w = l;
        drawScrollBar(x, y, w, h);
    }

    @Override
    public boolean onMouseClicked(ScrollArea area, int x, int y, int button) {
        if (isOnAxisStart() ? y <= area.y + getThickness() : y >= area.ey() - getThickness()) {
            this.dragging = true;
            return true;
        }
        return false;
    }
}
