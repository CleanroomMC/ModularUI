package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;

public abstract class AbstractTheme implements ITheme {

    private final String id;
    private final ITheme parentTheme;

    private WidgetThemeEntry<WidgetTheme> fallback;
    private WidgetThemeEntry<WidgetTheme> panel;
    private WidgetThemeEntry<WidgetTheme> button;
    private WidgetThemeEntry<SlotTheme> itemSlot;
    private WidgetThemeEntry<SlotTheme> fluidSlot;
    private WidgetThemeEntry<TextFieldTheme> textField;
    private WidgetThemeEntry<SelectableTheme> toggleButtonTheme;

    protected AbstractTheme(String id, ITheme parentTheme) {
        this.id = id;
        this.parentTheme = parentTheme;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ITheme getParentTheme() {
        return parentTheme;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getFallback() {
        if (this.fallback == null) {
            this.fallback = getWidgetTheme(IThemeApi.FALLBACK);
        }
        return this.fallback;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getPanelTheme() {
        if (this.panel == null) {
            this.panel = getWidgetTheme(IThemeApi.PANEL);
        }
        return this.panel;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getButtonTheme() {
        if (this.button == null) {
            this.button = getWidgetTheme(IThemeApi.BUTTON);
        }
        return this.button;
    }

    @Override
    public WidgetThemeEntry<SlotTheme> getItemSlotTheme() {
        if (this.itemSlot == null) {
            this.itemSlot = getWidgetTheme(IThemeApi.ITEM_SLOT);
        }
        return this.itemSlot;
    }

    @Override
    public WidgetThemeEntry<SlotTheme> getFluidSlotTheme() {
        if (this.fluidSlot == null) {
            this.fluidSlot = getWidgetTheme(IThemeApi.FLUID_SLOT);
        }
        return this.fluidSlot;
    }

    @Override
    public WidgetThemeEntry<TextFieldTheme> getTextFieldTheme() {
        if (this.textField == null) {
            this.textField = getWidgetTheme(IThemeApi.TEXT_FIELD);
        }
        return this.textField;
    }

    @Override
    public WidgetThemeEntry<SelectableTheme> getToggleButtonTheme() {
        if (this.toggleButtonTheme == null) {
            this.toggleButtonTheme = getWidgetTheme(IThemeApi.TOGGLE_BUTTON);
        }
        return this.toggleButtonTheme;
    }
}
