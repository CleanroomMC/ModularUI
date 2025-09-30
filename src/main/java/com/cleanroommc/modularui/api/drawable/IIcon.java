package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.drawable.HoverableIcon;
import com.cleanroommc.modularui.drawable.InteractableIcon;
import com.cleanroommc.modularui.widget.sizer.Box;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link IDrawable} with a fixed size.
 */
public interface IIcon extends IDrawable {

    /**
     * @return the drawable this icon wraps or null if it doesn't wrap anything
     */
    @Nullable IDrawable getWrappedDrawable();

    /**
     * @return width of this icon or 0 if the width should be dynamic
     */
    int getWidth();

    /**
     * @return height of this icon or 0 of the height should be dynamic
     */
    int getHeight();

    /**
     * @return the margin of this icon. Only used if width or height is 0
     */
    Box getMargin();

    default IDrawable getRootDrawable() {
        IDrawable drawable = this;
        while (drawable instanceof IIcon icon) {
            drawable = icon.getWrappedDrawable();
            if (drawable == null) return icon;
        }
        return drawable;
    }

    /**
     * This returns a hoverable wrapper of this icon. This is only used in {@link com.cleanroommc.modularui.drawable.text.RichText RichText}.
     * This allows this icon to have its own tooltip.
     */
    default HoverableIcon asHoverable() {
        return new HoverableIcon(this);
    }

    /**
     * This returns an interactable wrapper of this icon. This is only used in
     * {@link com.cleanroommc.modularui.drawable.text.RichText RichText}. This allows this icon to be able to listen to clicks and other
     * inputs.
     */
    default InteractableIcon asInteractable() {
        return new InteractableIcon(this);
    }

    IIcon EMPTY_2PX = EMPTY.asIcon().height(2);
}
