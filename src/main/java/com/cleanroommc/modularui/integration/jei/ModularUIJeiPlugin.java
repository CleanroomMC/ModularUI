package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.core.mixin.jei.GhostIngredientDragManagerAccessor;
import com.cleanroommc.modularui.core.mixin.jei.IngredientListOverlayAccessor;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

import mezz.jei.gui.ghost.GhostIngredientDrag;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import org.jetbrains.annotations.NotNull;

@JEIPlugin
public class ModularUIJeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public void register(@NotNull IModRegistry registry) {
        new ModularUIHandler<>(GuiContainerWrapper.class).register(registry);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ModularUIJeiPlugin.runtime = jeiRuntime;
    }

    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    public static GhostIngredientDragManager getGhostDragManager() {
        return ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).getGhostIngredientDragManager();
    }

    public static boolean hoveringOverIngredient(JeiGhostIngredientSlot<?> ingredientSlot) {
        Object hovered = getHoverdObject();
        if (hovered == null) return false;
        return ingredientSlot.castGhostIngredientIfValid(hovered) != null;
    }

    public static GhostIngredientDrag<?> getGhostDrag() {
        return ((GhostIngredientDragManagerAccessor) getGhostDragManager()).getGhostIngredientDrag();
    }

    public static Object getHoverdObject() {
        return ((GhostIngredientDragManagerAccessor) getGhostDragManager()).getHoveredIngredient();
    }

    public static boolean hasDraggingGhostIngredient() {
        return getGhostDrag() != null;
    }
}
