package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.widget.sizer.Box;

/**
 * A {@link IDrawable} but with a fixed size.
 */
public interface IIcon extends IDrawable {

    /**
     * @return width of this icon
     */
    int getWidth();

    /**
     * @return height of this icon
     */
    int getHeight();

    Box getMargin();

    IIcon EMPTY_2PX = EMPTY.asIcon().height(2);
}
