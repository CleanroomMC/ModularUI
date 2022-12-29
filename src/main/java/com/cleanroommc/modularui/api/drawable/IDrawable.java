package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

public interface IDrawable {

    void draw(int x, int y, int width, int height);

    default void draw(Area area) {
        draw(area.x, area.y, area.width, area.height);
    }

    default DrawableWidget asWidget() {
        return new DrawableWidget(this);
    }

    default Icon asIcon() {
        return new Icon(this);
    }

    class DrawableWidget extends Widget<DrawableWidget> {
        public DrawableWidget(IDrawable drawable) {
            background(drawable);
        }
    }
}
