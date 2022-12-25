package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

/**
 * A element in a Gui
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

    void drawBackground(float partialTicks);

    @Override
    void draw(float partialTicks);

    void drawForeground(float partialTicks);

    default boolean hasTooltip() {
        return getTooltip() != null;
    }

    @Nullable
    Tooltip getTooltip();

    void onFrameUpdate();

    @Override
    Area getArea();

    @Unmodifiable
    @NotNull
    default List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    @NotNull
    ModularPanel getPanel();

    @Override
    boolean isEnabled();

    boolean canBeSeen();

    default boolean canHover() {
        return true;
    }

    void markDirty();

    @NotNull
    IWidget getParent();

    GuiContext getContext();

    Flex flex();

    @Nullable
    IResizeable resizer();

    void resizer(IResizeable resizer);

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

    @Nullable
    Flex getFlex();
}
