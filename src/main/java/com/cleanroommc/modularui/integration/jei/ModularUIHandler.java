package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.widget.IIngredientProvider;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModularUIHandler implements IAdvancedGuiHandler<ModularGui> {

    @Override
    public @NotNull Class<ModularGui> getGuiContainerClass() {
        return ModularGui.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull ModularGui guiContainer) {
        List<Rectangle> areas = new ArrayList<>();
        ModularUIContext ui = guiContainer.getContext();
        for (ModularWindow window : ui.getOpenWindows()) {
            if (window.isEnabled()) {
                areas.add(new Rectangle(window.getPos().x, window.getPos().y, window.getSize().width, window.getSize().height));
            }
        }
        Rectangle draggableRectangle = guiContainer.getContext().getCursor().getDraggableArea();
        if (draggableRectangle != null) {
            areas.add(draggableRectangle);
        }
        return areas;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull ModularGui guiContainer, int mouseX, int mouseY) {
        Widget hovered = guiContainer.getContext().getCursor().getHovered();
        return hovered instanceof IIngredientProvider ? ((IIngredientProvider) hovered).getIngredient() : null;
    }
}
