package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.resizer.Box;
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

    @NotNull
    IWidget getParent();

    GuiContext getContext();

    @Override
    Box getMargin();

    @Override
    Box getPadding();

    Flex flex();

    @Nullable
    IResizeable resizer();

    void resizer(IResizeable resizer);

    @Override
    default void resize() {
        IResizeable resizer = resizer();
        if (resizer != null) {
            resizer.apply(this);
        }

        if (hasChildren()) {
            getChildren().forEach(IWidget::resize);
        }

        /*if (this.resizer != null) {
            this.resizer.postApply(this.area);
        }*/
        ModularUI.LOGGER.info("Resized {}: {}", getClass().getSimpleName(), getArea());
    }

    @Nullable
    Flex getFlex();
}
