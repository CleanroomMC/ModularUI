package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;
import mezz.jei.api.gui.*;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.overlay.GuiProperties;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class ModularUIHandler implements IAdvancedGuiHandler<GuiScreenWrapper>, IGhostIngredientHandler<GuiScreenWrapper>, IGuiScreenHandler<GuiScreenWrapper>, IRecipeTransferHandler<ModularContainer> {

    @Override
    public @NotNull Class<GuiScreenWrapper> getGuiContainerClass() {
        return GuiScreenWrapper.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull GuiScreenWrapper guiContainer) {
        return guiContainer.getScreen().context.getJeiSettings().getAllJeiExclusionAreas();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull GuiScreenWrapper guiContainer, int mouseX, int mouseY) {
        IGuiElement hovered = guiContainer.getScreen().context.getHovered();
        return hovered instanceof JeiIngredientProvider ? ((JeiIngredientProvider) hovered).getIngredient() : null;
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargets(GuiScreenWrapper gui, @NotNull I ingredient, boolean doStart) {
        return gui.getScreen().context.getJeiSettings().getAllGhostIngredientTargets(ingredient);
    }

    @Override
    public void onComplete() {
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull GuiScreenWrapper guiScreen) {
        return guiScreen.getScreen().context.getJeiSettings().isJeiEnabled(guiScreen.getScreen()) ? GuiProperties.create(guiScreen) : null;
    }

    @Override
    public @NotNull Class<ModularContainer> getContainerClass() {
        return ModularContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull ModularContainer container, @NotNull IRecipeLayout recipeLayout, @NotNull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen instanceof JeiRecipeTransferHandler) {
            return ((JeiRecipeTransferHandler) screen).transferRecipe(recipeLayout, maxTransfer, !doTransfer);
        }
        return null;
    }
}
