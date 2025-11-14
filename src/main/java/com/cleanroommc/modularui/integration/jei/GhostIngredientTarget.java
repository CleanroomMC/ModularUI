package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;

import net.minecraftforge.fml.common.Optional;

import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Optional.Interface(iface = "mezz.jei.api.gui.IGhostIngredientHandler$Target", modid = "jei")
public class GhostIngredientTarget<I> implements IGhostIngredientHandler.Target<I> {

    private final IWidget widget;
    private final RecipeViewerGhostIngredientSlot<I> ghostSlot;

    public static <I> GhostIngredientTarget<I> of(RecipeViewerGhostIngredientSlot<I> slot) {
        if (slot instanceof IWidget guiElement) {
            return new GhostIngredientTarget<>(guiElement, slot);
        }
        throw new IllegalArgumentException();
    }

    public static <I, W extends IWidget & RecipeViewerGhostIngredientSlot<I>> GhostIngredientTarget<I> of(W slot) {
        return new GhostIngredientTarget<>(slot, slot);
    }

    public GhostIngredientTarget(IWidget widget, RecipeViewerGhostIngredientSlot<I> ghostSlot) {
        this.widget = widget;
        this.ghostSlot = ghostSlot;
    }

    @Override
    public @NotNull Rectangle getArea() {
        return this.widget.getArea();
    }

    @Override
    public void accept(@NotNull I ingredient) {
        ingredient = this.ghostSlot.castGhostIngredientIfValid(ingredient);
        if (ingredient == null) {
            throw new IllegalStateException("Ghost slot did accept ingredient before, but now it doesn't.");
        }
        this.ghostSlot.setGhostIngredient(ingredient);
    }
}
