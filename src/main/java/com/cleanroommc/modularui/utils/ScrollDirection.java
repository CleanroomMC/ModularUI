package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.widget.sizer.Area;

/**
 * Scroll direction
 */
public enum ScrollDirection {

    VERTICAL(GuiAxis.Y) {
        @Override
        public int getPosition(Area area, float x) {
            return area.y(x);
        }

        @Override
        public int getSide(ScrollArea area, boolean otherIsActive) {
            return Math.max(0, getFullSide(area, otherIsActive) - area.getPadding().vertical());
        }

        @Override
        public int getFullSide(ScrollArea area, boolean otherIsActive) {
            int offset = 0;
            if (otherIsActive || (area.getScrollX() != null && area.getScrollX().isScrollBarActive(area, true))) {
                offset = area.getScrollX().getScrollbarThickness();
            }
            return area.h() - offset;
        }

        @Override
        public int getScroll(ScrollArea area, int x, int y) {
            return y - area.y + area.getScrollY().scroll;
        }

        @Override
        public float getProgress(ScrollArea area, int x, int y) {
            return (y - area.y) / (float) getFullSide(area);
        }
    },
    HORIZONTAL(GuiAxis.X) {
        @Override
        public int getPosition(Area area, float x) {
            return area.x(x);
        }

        @Override
        public int getSide(ScrollArea area, boolean otherIsActive) {
            return Math.max(0, getFullSide(area, otherIsActive) - area.getPadding().horizontal());
        }

        @Override
        public int getFullSide(ScrollArea area, boolean otherIsActive) {
            int offset = 0;
            if (otherIsActive || (area.getScrollY() != null && area.getScrollY().isScrollBarActive(area, true))) {
                offset = area.getScrollY().getScrollbarThickness();
            }
            return area.w() - offset;
        }

        @Override
        public int getScroll(ScrollArea area, int x, int y) {
            return x - area.x + area.getScrollX().scroll;
        }

        @Override
        public float getProgress(ScrollArea area, int x, int y) {
            return (x - area.x) / (float) getFullSide(area);
        }
    };

    public final GuiAxis axis;

    ScrollDirection(GuiAxis axis) {
        this.axis = axis;
    }

    /**
     * Get position of the area, x = 0 minimum corner, x = 1 maximum corner
     */
    public abstract int getPosition(Area area, float x);

    /**
     * Get dominant side for this scrolling direction
     */
    public int getSide(ScrollArea area) {
        return getSide(area, false);
    }

    public abstract int getSide(ScrollArea area, boolean otherIsActive);

    public int getFullSide(ScrollArea area) {
        return getFullSide(area, false);
    }

    public abstract int getFullSide(ScrollArea area, boolean otherIsActive);

    /**
     * Get scrolled amount for given mouse position
     */
    public abstract int getScroll(ScrollArea area, int x, int y);

    /**
     * Get progress scalar between 0 and 1 which identifies how much
     * it is near the maximum side
     */
    public abstract float getProgress(ScrollArea area, int x, int y);
}
