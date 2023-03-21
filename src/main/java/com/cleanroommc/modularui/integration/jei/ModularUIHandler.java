package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.gui.overlay.GuiProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ModularUIHandler implements IAdvancedGuiHandler<GuiScreenWrapper>, IGhostIngredientHandler<GuiScreenWrapper>, IGuiScreenHandler<GuiScreenWrapper> {

    @Override
    public @NotNull Class<GuiScreenWrapper> getGuiContainerClass() {
        return GuiScreenWrapper.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull GuiScreenWrapper guiContainer) {
        return guiContainer.getScreen().context.getAllJeiExclusionAreas();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull GuiScreenWrapper guiContainer, int mouseX, int mouseY) {
        //Widget hovered = guiContainer.getContext().getCursor().getHovered();
        //return hovered instanceof IIngredientProvider ? ((IIngredientProvider) hovered).getIngredient() : null;
        return null;
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargets(GuiScreenWrapper gui, @NotNull I ingredient, boolean doStart) {
        /*LinkedList<Target<I>> targets = new LinkedList<>();
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
        return targets;*/
        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull GuiScreenWrapper guiScreen) {
        return guiScreen.getScreen().context.isJeiEnabled() ? GuiProperties.create(guiScreen) : null;
    }
}
