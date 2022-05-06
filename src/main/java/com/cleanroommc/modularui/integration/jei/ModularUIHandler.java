package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.widget.IIngredientProvider;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class ModularUIHandler implements IAdvancedGuiHandler<ModularGui> {

    @Override
    public @NotNull Class<ModularGui> getGuiContainerClass() {
        return ModularGui.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull ModularGui guiContainer) {
        return guiContainer.getContext().getJeiExclusionZones();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull ModularGui guiContainer, int mouseX, int mouseY) {
        Widget hovered = guiContainer.getContext().getCursor().getHovered();
        return hovered instanceof IIngredientProvider ? ((IIngredientProvider) hovered).getIngredient() : null;
    }
}
