package com.cleanroommc.modularui.widget.scroll;

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
    public float getProgress(ScrollArea area, int x, int y) {
        return (x - area.x) / (float) getFullVisibleSize(area);
    }

    @Override
    public VerticalScrollData getOtherScrollData(ScrollArea area) {
        return area.getScrollY();
    }

    @Override
    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!area.isInside(x, y) || !isScrollBarActive(area, false)) {
            return false;
        }
        int scrollbar = getThickness();
        ScrollData data = getOtherScrollData(area);
        if (data != null && isOtherScrollBarActive(area, true)) {
            int thickness = data.getThickness();
            if (data.isOnAxisStart() ? x < area.x + thickness : x >= area.ex() - thickness) {
                return false;
            }
        }
        return isOnAxisStart() ? y >= area.y && y < area.y + scrollbar : y >= area.ey() - scrollbar && y < area.ey();
    }

    @Override
    public void drawScrollbar(ScrollArea area) {
        boolean isOtherActive = isOtherScrollBarActive(area, true);
        int l = getScrollBarLength(area);
        int x = 0;
        int y = isOnAxisStart() ? 0 : area.height - getThickness();
        int w = area.width;
        int h = getThickness();
        GuiDraw.drawRect(x, y, w, h, area.getScrollBarBackgroundColor());

        x = ((getFullVisibleSize(area, isOtherActive) - l) * getScroll()) / (getScrollSize() - getVisibleSize(area, isOtherActive));
        ScrollData data2 = getOtherScrollData(area);
        if (data2 != null && isOtherActive && data2.isOnAxisStart()) {
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
