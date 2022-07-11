package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.widget.IGhostIngredientTarget;
import com.cleanroommc.modularui.api.widget.IIngredientProvider;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.gui.overlay.GuiProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ModularUIHandler implements IAdvancedGuiHandler<ModularGui>, IGhostIngredientHandler<ModularGui>, IGuiScreenHandler<ModularGui> {

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

    @Override
    public <I> @NotNull List<Target<I>> getTargets(ModularGui gui, @NotNull I ingredient, boolean doStart) {
        LinkedList<Target<I>> targets = new LinkedList<>();
        for (ModularWindow window : gui.getContext().getOpenWindowsReversed()) {
            IWidgetParent.forEachByLayer(window, true, widget -> {
                if (widget instanceof IGhostIngredientTarget) {
                    Target<?> target = ((IGhostIngredientTarget<?>) widget).getTarget(ingredient);
                    if (target != null) {
                        targets.addFirst((Target<I>) target);
                    }
                }
                return false;
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull ModularGui guiScreen) {
        return guiScreen.getContext().doShowJei() ? GuiProperties.create(guiScreen) : null;
    }
}
