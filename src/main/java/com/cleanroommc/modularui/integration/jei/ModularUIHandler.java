package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.*;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.overlay.GuiProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class ModularUIHandler<T extends GuiContainer & IMuiScreen> implements IAdvancedGuiHandler<T>, IGhostIngredientHandler<T>, IGuiScreenHandler<T>, IRecipeTransferHandler<ModularContainer> {

    private static boolean registeredRecipeHandler = false;

    private final Class<T> clazz;

    public ModularUIHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(this);
        registry.addGhostIngredientHandler(this.clazz, this);
        registry.addGuiScreenHandler(this.clazz, this);
        if (registeredRecipeHandler) return;
        registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(this);
        registeredRecipeHandler = true;
    }

    @Override
    public @NotNull Class<T> getGuiContainerClass() {
        return clazz;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull T guiContainer) {
        return guiContainer.getScreen().getContext().getJeiSettings().getAllJeiExclusionAreas();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull T guiContainer, int mouseX, int mouseY) {
        IGuiElement hovered = guiContainer.getScreen().getContext().getHovered();
        return hovered instanceof JeiIngredientProvider jip ? jip.getIngredient() : null;
    }

    @Override
    public <I> @NotNull List<Target<I>> getTargets(T gui, @NotNull I ingredient, boolean doStart) {
        return gui.getScreen().getContext().getJeiSettings().getAllGhostIngredientTargets(ingredient);
    }

    @Override
    public void onComplete() {}

    @Override
    public boolean shouldHighlightTargets() {
        return false;
    }

    @Nullable
    @Override
    public IGuiProperties apply(@NotNull T guiScreen) {
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
