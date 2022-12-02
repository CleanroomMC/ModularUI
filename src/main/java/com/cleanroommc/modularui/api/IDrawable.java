package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.utils.Area;

public interface IDrawable {

    void draw(int x, int y, int width, int height);

    default void draw(Area area) {
        draw(area.x, area.y, area.w, area.h);
    }

    /*default Widget asWidget() {
        return new Widget().background(this);
    }*/
}
