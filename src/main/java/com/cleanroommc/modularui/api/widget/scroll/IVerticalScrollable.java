package com.cleanroommc.modularui.api.widget.scroll;

public interface IVerticalScrollable {

    void setVerticalScrollOffset(int offset);

    int getVerticalScrollOffset();

    default int getVerticalBarWidth() {
        return 2;
    }

    int getVisibleHeight();

    int getActualHeight();
}
