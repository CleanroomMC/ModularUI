package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.widget.sizer.Area;

public interface IDrawable {

    void draw(int x, int y, int width, int height);

    default void draw(Area area) {
        draw(area.x, area.y, area.width, area.height);
    }

    /*default Widget asWidget() {
        return new Widget().background(this);
    }*/

    default Icon asIcon() {
        return new Icon(this);
    }
}
