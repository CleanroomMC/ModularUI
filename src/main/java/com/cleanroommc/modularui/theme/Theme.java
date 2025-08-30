package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class Theme implements ITheme {

    private final Map<WidgetThemeKey<?>, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();

    private final String id;
    private final ITheme parentTheme;
    private final WidgetTheme fallback;
    private final WidgetTheme panelTheme;
    private final WidgetTheme buttonTheme;
    private final SlotTheme itemSlotTheme;
    private final SlotTheme fluidSlotTheme;
    private final TextFieldTheme textFieldTheme;
    private final SelectableTheme toggleButtonTheme;

    Theme(String id, ITheme parent, Map<WidgetThemeKey<?>, WidgetTheme> widgetThemes) {
        this.id = id;
        this.parentTheme = parent;
        this.widgetThemes.putAll(widgetThemes);
        if (parent instanceof Theme theme) {
            for (Map.Entry<WidgetThemeKey<?>, WidgetTheme> entry : theme.widgetThemes.entrySet()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (parent == IThemeApi.get().getDefaultTheme()) {
            if (!this.widgetThemes.containsKey(IThemeApi.FALLBACK)) {
                this.widgetThemes.put(IThemeApi.FALLBACK, ThemeManager.defaultFallbackWidgetTheme);
            }
            for (Map.Entry<WidgetThemeKey<?>, WidgetTheme> entry : ThemeAPI.INSTANCE.defaultWidgetThemes.entrySet()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.panelTheme = this.widgetThemes.get(IThemeApi.PANEL);
        this.fallback = this.widgetThemes.get(IThemeApi.FALLBACK);
        this.buttonTheme = this.widgetThemes.get(IThemeApi.BUTTON);
        this.itemSlotTheme = (SlotTheme) this.widgetThemes.get(IThemeApi.ITEM_SLOT);
        this.fluidSlotTheme = (SlotTheme) this.widgetThemes.get(IThemeApi.FLUID_SLOT);
        this.textFieldTheme = (TextFieldTheme) this.widgetThemes.get(IThemeApi.TEXT_FIELD);
        this.toggleButtonTheme = (SelectableTheme) this.widgetThemes.get(IThemeApi.TOGGLE_BUTTON);
    }

    public String getId() {
        return this.id;
    }

    public ITheme getParentTheme() {
        return this.parentTheme;
    }

    public WidgetTheme getFallback() {
        return this.fallback;
    }

    public WidgetTheme getPanelTheme() {
        return this.panelTheme;
    }

    public WidgetTheme getButtonTheme() {
        return this.buttonTheme;
    }

    @Override
    public SlotTheme getItemSlotTheme() {
        return this.itemSlotTheme;
    }

    @Override
    public SlotTheme getFluidSlotTheme() {
        return this.fluidSlotTheme;
    }

    public TextFieldTheme getTextFieldTheme() {
        return this.textFieldTheme;
    }

    @Override
    public SelectableTheme getToggleButtonTheme() {
        return this.toggleButtonTheme;
    }

    public <T extends WidgetTheme> T getWidgetTheme(WidgetThemeKey<T> key) {
        WidgetTheme widgetTheme = this.widgetThemes.get(key);
        if (widgetTheme == null && key.isSubWidgetTheme()) {
            widgetTheme = this.widgetThemes.get(key.getParent());
        }
        if (key.isCompatible(widgetTheme)) {
            return key.cast(widgetTheme);
        }
        throw new IllegalStateException();
    }
}
