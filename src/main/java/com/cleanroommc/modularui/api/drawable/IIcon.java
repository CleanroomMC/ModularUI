package com.cleanroommc.modularui.api.drawable;

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
}
