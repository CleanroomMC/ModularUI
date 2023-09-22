package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;

public interface IDraggable extends IViewport {

    /**
     * Gets called every frame after everything else is rendered.
     * Is only called when {@link #isMoving()} is true.
     * Translate to the mouse pos and draw with {@link com.cleanroommc.modularui.widget.WidgetTree#drawTree(IWidget, GuiContext, boolean)}.
     *
     * @param partialTicks difference from last from
     */
    void drawMovingState(GuiContext context, float partialTicks);

    /**
     * @param button the mouse button that's holding down
     * @return false if the action should be canceled
     */
    boolean onDragStart(int button);

    /**
     * The dragging has ended and getState == IDLE
     *
     * @param successful is false if this returned to it's old position
     */
    void onDragEnd(boolean successful);

    void onDrag(int mouseButton, long timeSinceLastClick);

    /**
     * Gets called when the mouse is released
     *
     * @param widget current top most widget below the mouse
     * @return if the location is valid
     */
    default boolean canDropHere(int x, int y, @Nullable IGuiElement widget) {
        return true;
    }

    /**
     * @return the size and pos during move
     */
    @Nullable
    Area getMovingArea();

    boolean isMoving();

    void setMoving(boolean moving);

    void transform(IViewportStack viewportStack);

    @Override
    default void transformChildren(IViewportStack stack) {
    }

    @Override
    default void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
    }
}
