package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.screen.Tooltip;

public abstract class AbstractDefaultTheme implements ITheme {

    private WidgetTheme panel;
    private WidgetTheme button;
    private WidgetSlotTheme itemSlot;
    private WidgetSlotTheme fluidSlot;
    private WidgetTextFieldTheme textField;
    private WidgetThemeSelectable toggleButtonTheme;

    @Override
    public ITheme getParentTheme() {
        return null;
    }

    @Override
    public WidgetTheme getPanelTheme() {
        if (this.panel == null) {
            this.panel = getWidgetTheme(Theme.PANEL);
        }
        return this.panel;
    }

    @Override
    public WidgetTheme getButtonTheme() {
        if (this.button == null) {
            this.button = getWidgetTheme(Theme.BUTTON);
        }
        return this.button;
    }

    @Override
    public WidgetSlotTheme getItemSlotTheme() {
        if (this.itemSlot == null) {
            this.itemSlot = (WidgetSlotTheme) getWidgetTheme(Theme.ITEM_SLOT);
        }
        return this.itemSlot;
    }

    @Override
    public WidgetSlotTheme getFluidSlotTheme() {
        if (this.fluidSlot == null) {
            this.fluidSlot = (WidgetSlotTheme) getWidgetTheme(Theme.FLUID_SLOT);
        }
        return this.fluidSlot;
    }

    @Override
    public WidgetTextFieldTheme getTextFieldTheme() {
        if (this.textField == null) {
            this.textField = (WidgetTextFieldTheme) getWidgetTheme(Theme.TEXT_FIELD);
        }
        return this.textField;
    }

    @Override
    public WidgetThemeSelectable getToggleButtonTheme() {
        if (this.toggleButtonTheme == null) {
            this.toggleButtonTheme = (WidgetThemeSelectable) getWidgetTheme(Theme.TOGGLE_BUTTON);
        }
        return this.toggleButtonTheme;
    }

    @Override
    public int getOpenCloseAnimationOverride() {
        return ModularUIConfig.panelOpenCloseAnimationTime;
    }

    @Override
    public boolean getSmoothProgressBarOverride() {
        return ModularUIConfig.smoothProgressBar;
    }

    @Override
    public Tooltip.Pos getTooltipPosOverride() {
        return ModularUIConfig.tooltipPos;
    }
}
