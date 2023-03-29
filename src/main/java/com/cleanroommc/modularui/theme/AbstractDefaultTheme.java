package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.ITheme;

public abstract class AbstractDefaultTheme implements ITheme {

    private WidgetTheme panel;
    private WidgetTheme button;
    private WidgetSlotTheme itemSlot;
    private WidgetSlotTheme fluidSlot;
    private WidgetTextFieldTheme textField;

    @Override
    public ITheme getParentTheme() {
        return null;
    }

    @Override
    public WidgetTheme getPanelTheme() {
        if (this.panel == null) {
            this.panel = getWidgetTheme(Theme.PANEL);
        }
        return panel;
    }

    @Override
    public WidgetTheme getButtonTheme() {
        if (this.button == null) {
            this.button = getWidgetTheme(Theme.BUTTON);
        }
        return button;
    }

    @Override
    public WidgetSlotTheme getItemSlotTheme() {
        if (this.itemSlot == null) {
            this.itemSlot = (WidgetSlotTheme) getWidgetTheme(Theme.ITEM_SLOT);
        }
        return itemSlot;
    }

    @Override
    public WidgetSlotTheme getFluidSlotTheme() {
        if (this.fluidSlot == null) {
            this.fluidSlot = (WidgetSlotTheme) getWidgetTheme(Theme.FLUID_SLOT);
        }
        return fluidSlot;
    }

    @Override
    public WidgetTextFieldTheme getTextFieldTheme() {
        if (this.textField == null) {
            this.textField = (WidgetTextFieldTheme) getWidgetTheme(Theme.TEXT_FIELD);
        }
        return textField;
    }
}
