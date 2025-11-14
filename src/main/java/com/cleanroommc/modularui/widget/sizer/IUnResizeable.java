package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.layout.IResizeParent;
import com.cleanroommc.modularui.api.layout.IResizeable;

/**
 * A variation of {@link IResizeable} with default implementations which don't do anything
 */
public interface IUnResizeable extends IResizeParent {

    IUnResizeable INSTANCE = () -> {
        Area.SHARED.set(0, 0, 0, 0);
        return Area.SHARED;
    };

    @Override
    default boolean isXCalculated() {
        return true;
    }

    @Override
    default boolean isYCalculated() {
        return true;
    }

    @Override
    default boolean isWidthCalculated() {
        return true;
    }

    @Override
    default boolean isHeightCalculated() {
        return true;
    }

    @Override
    default boolean areChildrenCalculated() {
        return true;
    }

    @Override
    default boolean isLayoutDone() {
        return true;
    }

    @Override
    default boolean canRelayout(boolean isParentLayout) {
        return false;
    }

    @Override
    default boolean isXMarginPaddingApplied() {
        return true;
    }

    @Override
    default boolean isYMarginPaddingApplied() {
        return true;
    }
}
