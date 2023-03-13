package com.cleanroommc.modularui.widget.sizer;

public enum GuiAxis {

    X, Y;

    public boolean isHorizontal() {
        return this == X;
    }

    public boolean isVertical() {
        return this == Y;
    }

    public GuiAxis getOther() {
        return this == X ? Y : X;
    }
}
