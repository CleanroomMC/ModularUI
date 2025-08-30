package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;

public abstract class AbstractDefaultTheme implements ITheme {

    private WidgetTheme fallback;
    private WidgetTheme panel;
    private WidgetTheme button;
    private SlotTheme itemSlot;
    private SlotTheme fluidSlot;
    private TextFieldTheme textField;
    private SelectableTheme toggleButtonTheme;

    @Override
    public ITheme getParentTheme() {
        return null;
    }

    @Override
    public WidgetTheme getFallback() {
        if (this.fallback == null) {
            this.fallback = getWidgetTheme(IThemeApi.FALLBACK);
        }
        return this.fallback;
    }

    @Override
    public WidgetTheme getPanelTheme() {
        if (this.panel == null) {
            this.panel = getWidgetTheme(IThemeApi.PANEL);
        }
        return this.panel;
    }

    @Override
    public WidgetTheme getButtonTheme() {
        if (this.button == null) {
            this.button = getWidgetTheme(IThemeApi.BUTTON);
        }
        return this.button;
    }

    @Override
    public SlotTheme getItemSlotTheme() {
        if (this.itemSlot == null) {
            this.itemSlot = getWidgetTheme(IThemeApi.ITEM_SLOT);
        }
        return this.itemSlot;
    }

    @Override
    public SlotTheme getFluidSlotTheme() {
        if (this.fluidSlot == null) {
            this.fluidSlot = getWidgetTheme(IThemeApi.FLUID_SLOT);
        }
        return this.fluidSlot;
    }

    @Override
    public TextFieldTheme getTextFieldTheme() {
        if (this.textField == null) {
            this.textField = getWidgetTheme(IThemeApi.TEXT_FIELD);
        }
        return this.textField;
    }

    @Override
    public SelectableTheme getToggleButtonTheme() {
        if (this.toggleButtonTheme == null) {
            this.toggleButtonTheme = getWidgetTheme(IThemeApi.TOGGLE_BUTTON);
        }
        return this.toggleButtonTheme;
    }
}
