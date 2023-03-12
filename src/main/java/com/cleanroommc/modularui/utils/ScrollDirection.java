package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.GuiAxis;

/**
 * Scroll direction
 */
public enum ScrollDirection {

    VERTICAL(GuiAxis.X) {
        @Override
        public int getPosition(Area area, float x) {
            return area.y(x);
        }

        @Override
        public int getSide(Area area) {
            return Math.max(0, getFullSide(area) - area.getPadding().vertical());
        }

        @Override
        public int getFullSide(Area area) {
            return area.h();
        }

        @Override
        public int getScroll(ScrollArea area, int x, int y) {
            return y - area.y + area.getScrollY().scroll;
        }

        @Override
        public float getProgress(Area area, int x, int y) {
            return (y - area.y) / (float) area.height;
        }
    },
    HORIZONTAL(GuiAxis.Y) {
        @Override
        public int getPosition(Area area, float x) {
            return area.x(x);
        }

        @Override
        public int getSide(Area area) {
            return Math.max(0, getFullSide(area) - area.getPadding().horizontal());
        }

        @Override
        public int getFullSide(Area area) {
            return area.w();
        }

        @Override
        public int getScroll(ScrollArea area, int x, int y) {
            return x - area.x + area.getScrollX().scroll;
        }

        @Override
        public float getProgress(Area area, int x, int y) {
            return (x - area.x) / (float) area.width;
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
    public abstract int getSide(Area area);

    public abstract int getFullSide(Area area);

    /**
     * Get scrolled amount for given mouse position
     */
    public abstract int getScroll(ScrollArea area, int x, int y);

    /**
     * Get progress scalar between 0 and 1 which identifies how much
     * it is near the maximum side
     */
    public abstract float getProgress(Area area, int x, int y);
}
