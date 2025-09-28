package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.core.mixins.late.jei.GhostIngredientDragManagerAccessor;
import com.cleanroommc.modularui.core.mixins.late.jei.IngredientListOverlayAccessor;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.test.CraftingModularContainer;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.config.Config;
import mezz.jei.gui.ghost.GhostIngredientDrag;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JEIPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public void register(@NotNull IModRegistry registry) {
        ModularScreenJEIHandler.register(GuiContainerWrapper.class, registry);
        ModularScreenJEIHandler.register(GuiScreenWrapper.class, registry);
        ModularContainerJEIHandler.register(ModularContainer.class, registry);
        ModularContainerJEIHandler.register(CraftingModularContainer.class, registry);
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        ModularUIJeiPlugin.runtime = jeiRuntime;
    }

    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    public static GhostIngredientDragManager getGhostDragManager() {
        return ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).getGhostIngredientDragManager();
    }

    public static boolean hoveringOverIngredient(RecipeViewerGhostIngredientSlot<?> ingredientSlot) {
        if (Config.isCheatItemsEnabled()) return false;
        Object hovered = getHoverdObject();
        if (hovered == null) return false;
        return ingredientSlot.castGhostIngredientIfValid(hovered) != null;
    }

    public static boolean draggingValidIngredient(RecipeViewerGhostIngredientSlot<?> ingredientSlot) {
        Object dragging = getDraggedObject();
        if (dragging == null) return false;
        return ingredientSlot.castGhostIngredientIfValid(dragging) != null;
    }

    @Nullable
    public static GhostIngredientDrag<?> getGhostDrag() {
        return ((GhostIngredientDragManagerAccessor) getGhostDragManager()).getGhostIngredientDrag();
    }

    @Nullable
    public static Object getDraggedObject() {
        GhostIngredientDrag<?> drag = getGhostDrag();
        return drag == null ? null : drag.getIngredient();
    }

    @Nullable
    public static Object getHoverdObject() {
        return ((GhostIngredientDragManagerAccessor) getGhostDragManager()).getHoveredIngredient();
    }

    public static boolean hasDraggingGhostIngredient() {
        return getGhostDrag() != null;
    }
}
