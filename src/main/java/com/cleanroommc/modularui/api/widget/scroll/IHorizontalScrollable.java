package com.cleanroommc.modularui.api.widget.scroll;

public interface IHorizontalScrollable {

    void setHorizontalScrollOffset(int offset);

    int getHorizontalScrollOffset();

    default int getHorizontalBarHeight() {
        return 2;
    }

    int getVisibleWidth();

    int getActualWidth();
}
