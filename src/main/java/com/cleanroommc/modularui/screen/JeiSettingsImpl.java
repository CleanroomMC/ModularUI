package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.jei.GhostIngredientTarget;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiState;

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
 * Keeps track of everything related to JEI in a Modular GUI.
 * By default, JEI is disabled in client only GUIs.
 * This class can be safely interacted with even when JEI/HEI is not installed.
 */
@SideOnly(Side.CLIENT)
public class JeiSettingsImpl implements JeiSettings {

    private JeiState jeiState = JeiState.DEFAULT;
    private final List<IWidget> jeiExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> jeiExclusionAreas = new ArrayList<>();
    private final List<JeiGhostIngredientSlot<?>> jeiGhostIngredientSlots = new ArrayList<>();

    /**
     * Force JEI to be enabled
     */
    @Override
    public void enableJei() {
        this.jeiState = JeiState.ENABLED;
    }

    /**
     * Force JEI to be disabled
     */
    @Override
    public void disableJei() {
        this.jeiState = JeiState.DISABLED;
    }

    /**
     * Only enabled JEI in synced GUIs
     */
    @Override
    public void defaultJei() {
        this.jeiState = JeiState.DEFAULT;
    }

    /**
     * Checks if JEI is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if jei is enabled
     */
    @Override
    public boolean isJeiEnabled(ModularScreen screen) {
        return this.jeiState.test(screen);
    }

    /**
     * Adds an exclusion zone. JEI will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addJeiExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    @Override
    public void addJeiExclusionArea(Rectangle area) {
        if (!this.jeiExclusionAreas.contains(area)) {
            this.jeiExclusionAreas.add(area);
        }
    }

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    @Override
    public void removeJeiExclusionArea(Rectangle area) {
        this.jeiExclusionAreas.remove(area);
    }

    /**
     * Adds an exclusion zone of a widget. JEI will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    @Override
    public void addJeiExclusionArea(IWidget area) {
        if (!this.jeiExclusionWidgets.contains(area)) {
            this.jeiExclusionWidgets.add(area);
        }
    }

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    @Override
    public void removeJeiExclusionArea(IWidget area) {
        this.jeiExclusionWidgets.remove(area);
    }

    /**
     * Adds a JEI ghost slots. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from JEI into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    @Override
    public <W extends IWidget & JeiGhostIngredientSlot<?>> void addJeiGhostIngredientSlot(W slot) {
        if (!this.jeiGhostIngredientSlots.contains(slot)) {
            this.jeiGhostIngredientSlots.add(slot);
        }
    }

    /**
     * Removes a JEI ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    @Override
    public <W extends IWidget & JeiGhostIngredientSlot<?>> void removeJeiGhostIngredientSlot(W slot) {
        this.jeiGhostIngredientSlots.remove(slot);
    }

    @UnmodifiableView
    public List<Rectangle> getJeiExclusionAreas() {
        return Collections.unmodifiableList(this.jeiExclusionAreas);
    }

    @UnmodifiableView
    public List<IWidget> getJeiExclusionWidgets() {
        return Collections.unmodifiableList(this.jeiExclusionWidgets);
    }

    @UnmodifiableView
    public List<JeiGhostIngredientSlot<?>> getJeiGhostIngredientSlots() {
        return Collections.unmodifiableList(this.jeiGhostIngredientSlots);
    }

    @ApiStatus.Internal
    public List<Rectangle> getAllJeiExclusionAreas() {
        this.jeiExclusionWidgets.removeIf(widget -> !widget.isValid());
        List<Rectangle> areas = new ArrayList<>(this.jeiExclusionAreas);
        for (Iterator<IWidget> iterator = this.jeiExclusionWidgets.iterator(); iterator.hasNext(); ) {
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
        for (Iterator<JeiGhostIngredientSlot<?>> iterator = this.jeiGhostIngredientSlots.iterator(); iterator.hasNext(); ) {
            JeiGhostIngredientSlot<?> slot = iterator.next();
            IWidget widget = (IWidget) slot;
            if (!widget.isValid()) {
                iterator.remove();
                continue;
            }
            if (widget.isEnabled() && slot.castGhostIngredientIfValid(ingredient) != null) {
                JeiGhostIngredientSlot<I> slotWithType = (JeiGhostIngredientSlot<I>) slot;
                ghostHandlerTargets.add(new GhostIngredientTarget<>(widget, slotWithType));
            }
        }
        return ghostHandlerTargets;
    }
}
