package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.entity.player.EntityPlayer;

import mezz.jei.api.gui.*;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.overlay.GuiProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class ModularUIHandler implements IAdvancedGuiHandler<GuiContainerWrapper>, IGhostIngredientHandler<GuiContainerWrapper>, IGuiScreenHandler<GuiContainerWrapper>, IRecipeTransferHandler<ModularContainer> {

    @Override
    public @NotNull Class<GuiContainerWrapper> getGuiContainerClass() {
        return GuiContainerWrapper.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull GuiContainerWrapper guiContainer) {
        return guiContainer.getScreen().getContext().getJeiSettings().getAllJeiExclusionAreas();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull GuiContainerWrapper guiContainer, int mouseX, int mouseY) {
        IGuiElement hovered = guiContainer.getScreen().getContext().getHovered();
        return hovered instanceof JeiIngredientProvider jip ? jip.getIngredient() : null;
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargets(GuiContainerWrapper gui, @NotNull I ingredient, boolean doStart) {
        return gui.getScreen().getContext().getJeiSettings().getAllGhostIngredientTargets(ingredient);
    }

    @Override
    public void onComplete() {
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull GuiContainerWrapper guiScreen) {
        return guiScreen.getScreen().getContext().getJeiSettings().isJeiEnabled(guiScreen.getScreen()) ? GuiProperties.create(guiScreen) : null;
    }

    @Override
    public @NotNull Class<ModularContainer> getContainerClass() {
        return ModularContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull ModularContainer container, @NotNull IRecipeLayout recipeLayout, @NotNull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        ModularScreen screen = container.getScreen();
        if (screen instanceof JeiRecipeTransferHandler recipeTransferHandler) {
            return recipeTransferHandler.transferRecipe(recipeLayout, maxTransfer, !doTransfer);
        }
        return null;
    }
}
