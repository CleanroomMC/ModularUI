package com.cleanroommc.modularui.api.widget.scroll;

import org.jetbrains.annotations.Range;

public interface IHorizontalScrollable {

    void setHorizontalScrollOffset(int offset);

    int getHorizontalScrollOffset();

    default @Range(from = 1, to = 20) int getHorizontalBarHeight() {
        return 2;
    }

    int getVisibleWidth();

    int getActualWidth();
}
