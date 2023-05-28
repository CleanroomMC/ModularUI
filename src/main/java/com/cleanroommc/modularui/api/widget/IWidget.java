package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A widget in a Gui
 */
public interface IWidget extends IGuiElement {

    /**
     * Validates and initialises this element.
     * This element now becomes valid
     *
     * @param parent the parent this element belongs to
     */
    void initialise(@NotNull IWidget parent);

    /**
     * Invalidates this element.
     */
    void dispose();

    /**
     * Determines if this element exist in an active gui.
     *
     * @return if this is in a valid gui
     */
    boolean isValid();

    /**
     * Draws the background of this widget
     *
     * @param context gui context
     */
    void drawBackground(GuiContext context);

    /**
     * Draws additional stuff in this widget.
     * x = 0 and y = 0 is now in the top left corner of this widget
     *
     * @param context gui context
     */
    @Override
    void draw(GuiContext context);

    /**
     * Draws foreground elements of this widget. For example tooltips.
     * No transformations are applied here.
     *
     * @param context gui context
     */
    void drawForeground(GuiContext context);

    default void transform(IViewportStack stack) {
        stack.translate(getArea().rx, getArea().ry);
    }

    default WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getFallback();
    }

    /**
     * Called approximately 60 times per second.
     */
    void onFrameUpdate();

    /**
     * @return the area this widget occupies
     */
    @Override
    Area getArea();

    /**
     * Calculates if a given pos is inside this widgets area.
     * This should be used over {@link Area#isInside(int, int)}, since this accounts for transformations.
     *
     * @param stack viewport stack
     * @param mx    x pos
     * @param my    y pos
     * @return if pos is inside this widgets area
     */
    default boolean isInside(IViewportStack stack, int mx, int my) {
        int x = stack.unTransformX(mx, my);
        int y = stack.unTransformY(mx, my);
        return x >= 0 && x < getArea().w() && y >= 0 && y < getArea().h();
    }

    /**
     * @return all children of this widget
     */
    @NotNull
    default List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    /**
     * @return if this widget has any children
     */
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    /**
     * @return the panel this widget is in
     */
    @NotNull
    ModularPanel getPanel();

    /**
     * Returns if this element is enabled. Disabled elements are not drawn and can not be interacted with.
     *
     * @return if this element is enabled
     */
    @Override
    boolean isEnabled();

    void setEnabled(boolean enabled);

    default boolean canBeSeen(IViewportStack stack) {
        return Stencil.isInsideScissorArea(getArea(), stack);
    }

    default boolean canHover() {
        return true;
    }

    /**
     * Marks this widget as dirty.
     * Mainly used for the tooltip.
     */
    void markDirty();

    /**
     * @return the parent of this widget
     */
    @NotNull
    IWidget getParent();

    /**
     * @return the context the current screen
     */
    GuiContext getContext();

    /**
     * @return flex of this widget. Creates a new one if it doesn't already have onw.
     */
    Flex flex();

    /**
     * @return resizer of this widget
     */
    @Nullable
    IResizeable resizer();

    /**
     * Sets the resizer of this widget.
     *
     * @param resizer resizer
     */
    void resizer(IResizeable resizer);

    /**
     * Called when the screen resizes. Handles the positioning and sizing of this element.
     */
    @Override
    default void resize() {
        IResizeable resizer = resizer();
        if (resizer != null) {
            if (resizer.isSkip()) return;
            resizer.apply(this);
        }

        if (hasChildren()) {
            getChildren().forEach(IWidget::resize);
        }

        if (this instanceof ILayoutWidget) {
            ((ILayoutWidget) this).layoutWidgets();
        }

        if (resizer != null) {
            resizer.postApply(this);
        }

        if (this instanceof ILayoutWidget) {
            ((ILayoutWidget) this).postLayoutWidgets();
        }
    }

    default void postResize() {
    }

    /**
     * @return flex of this widget
     */
    Flex getFlex();

    default boolean isExpanded() {
        Flex flex = getFlex();
        return flex != null && flex.isExpanded();
    }
}
