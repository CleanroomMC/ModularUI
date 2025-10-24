package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.widget.INotifyEnabled;
import com.cleanroommc.modularui.api.widget.IWidget;

/**
 * This is responsible for laying out widgets.
 */
public interface ILayoutWidget extends INotifyEnabled {

    /**
     * Called after the children tried to calculate their size. This method responsible for laying out its children
     * in itself. This includes calling {@link IResizeable#setSizeResized(boolean, boolean)} or one of its variants after a size with
     * {@link com.cleanroommc.modularui.widget.sizer.Area#setSize(GuiAxis, int)} or one of its variants on each child. The same goes for
     * position. If this widget also applies margin and padding (this is usually the case), then {@link IResizeable#setMarginPaddingApplied(boolean)}
     * or one of its variants needs to be called to.
     * <p>
     * Note that even if {@link #shouldIgnoreChildSize(IWidget)} returns false at least one of the {@code setResized} methods in
     * {@link IResizeable} must be called. There is a no arg variant {@link IResizeable#updateResized()} which can also be used.
     * Not doing so may result failure to resize the widget tree fully.
     *
     * @return true if the layout was successful and no further iteration is needed
     */
    boolean layoutWidgets();

    /**
     * Called after post calculation of this widget. The last call guarantees, that this widget is fully calculated.
     *
     * @return true if the layout was successful and no further iteration is needed
     */
    default boolean postLayoutWidgets() {
        return true;
    }

    default boolean canCoverByDefaultSize(GuiAxis axis) {
        return false;
    }

    /**
     * Called when determining wrapping size of this widget.
     * If this method returns true, size and margin of the queried child will be ignored for calculation.
     * Typically return true when the child is disabled and you want to collapse it for layout.
     * This method should also be used for layouting children with {@link #layoutWidgets} if it might return true.
     */
    default boolean shouldIgnoreChildSize(IWidget child) {
        return false;
    }

    @Override
    default void onChildChangeEnabled(IWidget child, boolean enabled) {
        layoutWidgets();
        postLayoutWidgets();
    }
}
