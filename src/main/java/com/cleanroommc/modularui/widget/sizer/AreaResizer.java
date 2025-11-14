package com.cleanroommc.modularui.widget.sizer;

public class AreaResizer extends StaticResizer {

    private final Area area;

    public AreaResizer(Area area) {
        this.area = area;
    }

    @Override
    public Area getArea() {
        return area;
    }
}
