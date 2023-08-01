package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.jei.GhostIngredientTarget;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiState;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JeiSettings {

    private JeiState jeiState = JeiState.DEFAULT;
    private final List<IWidget> jeiExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> jeiExclusionAreas = new ArrayList<>();
    private final List<JeiGhostIngredientSlot<?>> jeiGhostIngredientSlots = new ArrayList<>();

    public void enableJei() {
        this.jeiState = JeiState.ENABLED;
    }

    public void disableJei() {
        this.jeiState = JeiState.DISABLED;
    }

    public void defaultJei() {
        this.jeiState = JeiState.DEFAULT;
    }

    public boolean isJeiEnabled(ModularScreen screen) {
        return this.jeiState.test(screen);
    }

    public void addJeiExclusionArea(Rectangle area) {
        if (!this.jeiExclusionAreas.contains(area)) {
            this.jeiExclusionAreas.add(area);
        }
    }

    public void removeJeiExclusionArea(Rectangle area) {
        this.jeiExclusionAreas.remove(area);
    }

    public void addJeiExclusionArea(IWidget area) {
        if (!this.jeiExclusionWidgets.contains(area)) {
            this.jeiExclusionWidgets.add(area);
        }
    }

    public void removeJeiExclusionArea(IWidget area) {
        this.jeiExclusionWidgets.remove(area);
    }

    public <W extends IWidget & JeiGhostIngredientSlot<?>> void addJeiGhostIngredientSlot(W slot) {
        this.jeiGhostIngredientSlots.add(slot);
    }

    public <W extends IWidget & JeiGhostIngredientSlot<?>> void removeJeiGhostIngredientSlot(W slot) {
        this.jeiGhostIngredientSlots.remove(slot);
    }

    public java.util.List<Rectangle> getJeiExclusionAreas() {
        return this.jeiExclusionAreas;
    }

    public java.util.List<IWidget> getJeiExclusionWidgets() {
        return this.jeiExclusionWidgets;
    }

    public java.util.List<Rectangle> getAllJeiExclusionAreas() {
        this.jeiExclusionWidgets.removeIf(widget -> !widget.isValid());
        java.util.List<Rectangle> areas = new ArrayList<>(this.jeiExclusionAreas);
        areas.addAll(this.jeiExclusionWidgets.stream()
                .filter(IWidget::isEnabled)
                .map(IWidget::getArea)
                .collect(Collectors.toList()));
        return areas;
    }

    public java.util.List<JeiGhostIngredientSlot<?>> getJeiGhostIngredientSlots() {
        return this.jeiGhostIngredientSlots;
    }

    @Optional.Method(modid = "jei")
    public <I> List<IGhostIngredientHandler.Target<I>> getAllGhostIngredientTargets(@NotNull I ingredient) {
        this.jeiGhostIngredientSlots.removeIf(widget -> !((IWidget) widget).isValid());
        return this.jeiGhostIngredientSlots.stream()
                .filter(slot -> ((IWidget) slot).isEnabled())
                .filter(slot -> slot.castGhostIngredientIfValid(ingredient) != null)
                .map(slot -> (IGhostIngredientHandler.Target<I>) GhostIngredientTarget.of(slot))
                .collect(Collectors.toList());
    }
}
