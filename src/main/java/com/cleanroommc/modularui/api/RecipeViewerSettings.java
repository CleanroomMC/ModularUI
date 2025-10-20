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
    void enable();

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void enableRecipeViewer() {enable();}

    /**
     * Force recipe viewer to be disabled
     */
    void disable();

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void disableRecipeViewer() {disable();}

    /**
     * Only enable recipe viewer in synced GUIs
     */
    void defaultState();

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void defaultRecipeViewerState() {defaultState();}

    /**
     * Checks if recipe viewer is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if jei is enabled
     */
    boolean isEnabled(ModularScreen screen);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default boolean isRecipeViewerEnabled(ModularScreen screen) {return isEnabled(screen);}

    /**
     * Adds an exclusion zone. Recipe viewer will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    void addExclusionArea(Rectangle area);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void addRecipeViewerExclusionArea(Rectangle area) {addExclusionArea(area);}

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    void removeExclusionArea(Rectangle area);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void removeRecipeViewerExclusionArea(Rectangle area) {removeExclusionArea(area);}

    /**
     * Adds an exclusion zone of a widget. Recipe viewer will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    void addExclusionArea(IWidget area);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void addRecipeViewerExclusionArea(IWidget area) {addExclusionArea(area);}

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    void removeExclusionArea(IWidget area);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default void removeRecipeViewerExclusionArea(IWidget area) {removeExclusionArea(area);}

    /**
     * Adds a recipe viewer ghost slots. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from recipe viewer into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void addGhostIngredientSlot(W slot);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void addRecipeViewerGhostIngredientSlot(W slot) {addGhostIngredientSlot(slot);}

    /**
     * Removes a recipe viewer ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void removeGhostIngredientSlot(W slot);

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void removeRecipeViewerGhostIngredientSlot(W slot) {removeGhostIngredientSlot(slot);}

    RecipeViewerSettings DUMMY = new RecipeViewerSettings() {
        @Override
        public void enable() {}

        @Override
        public void disable() {}

        @Override
        public void defaultState() {}

        @Override
        public boolean isEnabled(ModularScreen screen) {
            return false;
        }

        @Override
        public void addExclusionArea(Rectangle area) {}

        @Override
        public void removeExclusionArea(Rectangle area) {}

        @Override
        public void addExclusionArea(IWidget area) {}

        @Override
        public void removeExclusionArea(IWidget area) {}

        @Override
        public <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void addGhostIngredientSlot(W slot) {}

        @Override
        public <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void removeGhostIngredientSlot(W slot) {}
    };
}
