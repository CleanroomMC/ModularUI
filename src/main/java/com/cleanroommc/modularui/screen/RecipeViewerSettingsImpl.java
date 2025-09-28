package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.RecipeViewerSettings;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.jei.GhostIngredientTarget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerState;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of everything related to recipe viewer in a Modular GUI. Recipe viewer is a mod like JEI, NEI and EMI.
 * By default, recipe viewer is disabled in client only GUIs.
 * This class can be safely interacted with even when recipe viewer is not installed.
 */
@SideOnly(Side.CLIENT)
public class RecipeViewerSettingsImpl implements RecipeViewerSettings {

    private RecipeViewerState recipeViewerState = RecipeViewerState.DEFAULT;
    private final List<IWidget> recipeViewerExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> recipeViewerExclusionAreas = new ArrayList<>();
    private final List<RecipeViewerGhostIngredientSlot<?>> recipeViewerGhostIngredientSlots = new ArrayList<>();

    /**
     * Force recipe viewer to be enabled
     */
    @Override
    public void enableRecipeViewer() {
        this.recipeViewerState = RecipeViewerState.ENABLED;
    }

    /**
     * Force recipe viewer to be disabled
     */
    @Override
    public void disableRecipeViewer() {
        this.recipeViewerState = RecipeViewerState.DISABLED;
    }

    /**
     * Only enabled recipe viewer in synced GUIs
     */
    @Override
    public void defaultRecipeViewerState() {
        this.recipeViewerState = RecipeViewerState.DEFAULT;
    }

    /**
     * Checks if recipe viewer is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if recipe viewer is enabled
     */
    @Override
    public boolean isRecipeViewerEnabled(ModularScreen screen) {
        return this.recipeViewerState.test(screen);
    }

    /**
     * Adds an exclusion zone. Recipe viewer will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addRecipeViewerExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    @Override
    public void addRecipeViewerExclusionArea(Rectangle area) {
        if (!this.recipeViewerExclusionAreas.contains(area)) {
            this.recipeViewerExclusionAreas.add(area);
        }
    }

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    @Override
    public void removeRecipeViewerExclusionArea(Rectangle area) {
        this.recipeViewerExclusionAreas.remove(area);
    }

    /**
     * Adds an exclusion zone of a widget. Recipe viewer will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    @Override
    public void addRecipeViewerExclusionArea(IWidget area) {
        if (!this.recipeViewerExclusionWidgets.contains(area)) {
            this.recipeViewerExclusionWidgets.add(area);
        }
    }

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    @Override
    public void removeRecipeViewerExclusionArea(IWidget area) {
        this.recipeViewerExclusionWidgets.remove(area);
    }

    /**
     * Adds a recipe viewer ghost slots. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from recipe viewer into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    @Override
    public <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void addRecipeViewerGhostIngredientSlot(W slot) {
        if (!this.recipeViewerGhostIngredientSlots.contains(slot)) {
            this.recipeViewerGhostIngredientSlots.add(slot);
        }
    }

    /**
     * Removes a recipe viewer ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    @Override
    public <W extends IWidget & RecipeViewerGhostIngredientSlot<?>> void removeRecipeViewerGhostIngredientSlot(W slot) {
        this.recipeViewerGhostIngredientSlots.remove(slot);
    }

    @UnmodifiableView
    public List<Rectangle> getRecipeViewerExclusionAreas() {
        return Collections.unmodifiableList(this.recipeViewerExclusionAreas);
    }

    @UnmodifiableView
    public List<IWidget> getRecipeViewerExclusionWidgets() {
        return Collections.unmodifiableList(this.recipeViewerExclusionWidgets);
    }

    @UnmodifiableView
    public List<RecipeViewerGhostIngredientSlot<?>> getRecipeViewerGhostIngredientSlots() {
        return Collections.unmodifiableList(this.recipeViewerGhostIngredientSlots);
    }

    @ApiStatus.Internal
    public List<Rectangle> getAllRecipeViewerExclusionAreas() {
        this.recipeViewerExclusionWidgets.removeIf(widget -> !widget.isValid());
        List<Rectangle> areas = new ArrayList<>(this.recipeViewerExclusionAreas);
        for (Iterator<IWidget> iterator = this.recipeViewerExclusionWidgets.iterator(); iterator.hasNext(); ) {
            IWidget widget = iterator.next();
            if (!widget.isValid()) {
                iterator.remove();
                continue;
            }
            if (widget.isEnabled()) {
                areas.add(widget.getArea());
            }
        }
        return areas;
    }

    @ApiStatus.Internal
    @Optional.Method(modid = "jei")
    public <I> List<IGhostIngredientHandler.Target<I>> getAllGhostIngredientTargets(@NotNull I ingredient) {
        List<IGhostIngredientHandler.Target<I>> ghostHandlerTargets = new ArrayList<>();
        for (Iterator<RecipeViewerGhostIngredientSlot<?>> iterator = this.recipeViewerGhostIngredientSlots.iterator(); iterator.hasNext(); ) {
            RecipeViewerGhostIngredientSlot<?> slot = iterator.next();
            IWidget widget = (IWidget) slot;
            if (!widget.isValid()) {
                iterator.remove();
                continue;
            }
            if (widget.isEnabled() && slot.castGhostIngredientIfValid(ingredient) != null) {
                RecipeViewerGhostIngredientSlot<I> slotWithType = (RecipeViewerGhostIngredientSlot<I>) slot;
                ghostHandlerTargets.add(new GhostIngredientTarget<>(widget, slotWithType));
            }
        }
        return ghostHandlerTargets;
    }
}
