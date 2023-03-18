package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.widget.sizer.Area;

public interface IViewportTransformation {

    int transformX(int x, Area area, boolean toLocal);

    int transformY(int y, Area area, boolean toLocal);

    void applyOpenGlTransformation();

    void unapplyOpenGlTransformation();

    IViewportTransformation EMPTY = new IViewportTransformation() {

        @Override
        public int transformX(int x, Area area, boolean toLocal) {
            return x;
        }

        @Override
        public int transformY(int y, Area area, boolean toLocal) {
            return y;
        }

        @Override
        public void applyOpenGlTransformation() {
        }

        @Override
        public void unapplyOpenGlTransformation() {
        }
    };
}
