package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.screen.ModularScreen;

import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

/**
 * Keeps track of everything related to JEI in a Modular GUI.
 * By default, JEI is disabled in client only GUIs.
 * This class can be safely interacted with even when JEI/HEI is not installed.
 */
@ApiStatus.NonExtendable
public interface JeiSettings {

    /**
     * Force JEI to be enabled
     */
    void enableJei();

    /**
     * Force JEI to be disabled
     */
    void disableJei();

    /**
     * Only enabled JEI in synced GUIs
     */
    void defaultJei();

    /**
     * Checks if JEI is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if jei is enabled
     */
    boolean isJeiEnabled(ModularScreen screen);

    /**
     * Adds an exclusion zone. JEI will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addJeiExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    void addJeiExclusionArea(Rectangle area);

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    void removeJeiExclusionArea(Rectangle area);

    /**
     * Adds an exclusion zone of a widget. JEI will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    void addJeiExclusionArea(IWidget area);

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    void removeJeiExclusionArea(IWidget area);

    /**
     * Adds a JEI ghost slots. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from JEI into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & JeiGhostIngredientSlot<?>> void addJeiGhostIngredientSlot(W slot);

    /**
     * Removes a JEI ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & JeiGhostIngredientSlot<?>> void removeJeiGhostIngredientSlot(W slot);

    JeiSettings DUMMY = new JeiSettings() {
        @Override
        public void enableJei() {}

        @Override
        public void disableJei() {}

        @Override
        public void defaultJei() {}

        @Override
        public boolean isJeiEnabled(ModularScreen screen) {
            return false;
        }

        @Override
        public void addJeiExclusionArea(Rectangle area) {}

        @Override
        public void removeJeiExclusionArea(Rectangle area) {}

        @Override
        public void addJeiExclusionArea(IWidget area) {}

        @Override
        public void removeJeiExclusionArea(IWidget area) {}

        @Override
        public <W extends IWidget & JeiGhostIngredientSlot<?>> void addJeiGhostIngredientSlot(W slot) {}

        @Override
        public <W extends IWidget & JeiGhostIngredientSlot<?>> void removeJeiGhostIngredientSlot(W slot) {}
    };
}