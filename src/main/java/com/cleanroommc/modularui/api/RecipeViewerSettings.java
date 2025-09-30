package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.ModularScreen;

import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

/**
 * Keeps track of everything related to recipe viewer in a Modular GUI. Recipe viewer is a mod like JEI, NEI and EMI.
 * By default, recipe viewer is disabled in client only GUIs.
 * This class can be safely interacted with even when recipe viewer is not installed.
 */
@ApiStatus.NonExtendable
public interface RecipeViewerSettings {

    /**
     * Force recipe viewer to be enabled
     */
    void enableRecipeViewer();

    /**
     * Force recipe viewer to be disabled
     */
    void disableRecipeViewer();

    /**
     * Only enable recipe viewer in synced GUIs
     */
    void defaultRecipeViewerState();

    /**
     * Checks if recipe viewer is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if jei is enabled
     */
    boolean isRecipeViewerEnabled(ModularScreen screen);

    /**
     * Adds an exclusion zone. Recipe viewer will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addRecipeViewerExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    void addRecipeViewerExclusionArea(Rectangle area);

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    void removeRecipeViewerExclusionArea(Rectangle area);

    /**
     * Adds an exclusion zone of a widget. Recipe viewer will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    void addRecipeViewerExclusionArea(IWidget area);

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    void removeRecipeViewerExclusionArea(IWidget area);

    /**
     * Adds a recipe viewer ghost slots. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from recipe viewer into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void addRecipeViewerGhostIngredientSlot(W slot);

    /**
     * Removes a recipe viewer ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void removeRecipeViewerGhostIngredientSlot(W slot);

    RecipeViewerSettings DUMMY = new RecipeViewerSettings() {
        @Override
        public void enableRecipeViewer() {}

        @Override
        public void disableRecipeViewer() {}

        @Override
        public void defaultRecipeViewerState() {}

        @Override
        public boolean isRecipeViewerEnabled(ModularScreen screen) {
            return false;
        }

        @Override
        public void addRecipeViewerExclusionArea(Rectangle area) {}

        @Override
        public void removeRecipeViewerExclusionArea(Rectangle area) {}

        @Override
        public void addRecipeViewerExclusionArea(IWidget area) {}

        @Override
        public void removeRecipeViewerExclusionArea(IWidget area) {}

        @Override
        public <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void addRecipeViewerGhostIngredientSlot(W slot) {}

        @Override
        public <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void removeRecipeViewerGhostIngredientSlot(W slot) {}
    };
}
