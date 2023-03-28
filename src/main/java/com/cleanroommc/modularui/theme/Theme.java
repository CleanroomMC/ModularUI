package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.ITheme;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class Theme implements ITheme {

    public static final ITheme DEFAULT_DEFAULT = new ThemeHandler.DefaultTheme();

    /**
     * If you have a custom widget type and want a custom theme for the type you can register a parser here.
     *
     * @param id           the id of the widget theme. This how json finds this parser.
     * @param defaultTheme this will be used for {@link #DEFAULT_DEFAULT}.
     * @param function     a function that creates an instance of the widget theme with the parent theme and a json.
     */
    public static void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser function) {
        ThemeHandler.registerWidgetTheme(id, defaultTheme, function);
    }

    public static final String FALLBACK = "default";
    public static final String PANEL = "panel";
    public static final String BUTTON = "button";
    public static final String ITEM_SLOT = "itemSlot";
    public static final String FLUID_SLOT = "fluidSlot";
    public static final String TEXT_FIELD = "textField";

    private final Map<String, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();

    private final String id;
    private final ITheme parentTheme;
    private final WidgetTheme fallback;
    private final WidgetTheme panelTheme;
    private final WidgetTheme buttonTheme;
    private final WidgetSlotTheme itemSlotTheme;
    private final WidgetSlotTheme fluidSlotTheme;
    private final WidgetTextFieldTheme textFieldTheme;

    Theme(String id, ITheme parent, Map<String, WidgetTheme> widgetThemes) {
        this.id = id;
        this.parentTheme = parent;
        this.widgetThemes.putAll(widgetThemes);
        if (parent instanceof Theme) {
            for (Map.Entry<String, WidgetTheme> entry : ((Theme) parent).widgetThemes.entrySet()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (parent == DEFAULT_DEFAULT) {
            if (!this.widgetThemes.containsKey(FALLBACK)) {
                this.widgetThemes.put(FALLBACK, ThemeHandler.defaultdefaultWidgetTheme);
            }
            for (Map.Entry<String, WidgetTheme> entry : ThemeHandler.defaultWidgetThemes.entrySet()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.panelTheme = this.widgetThemes.get(PANEL);
        this.fallback = this.widgetThemes.get(FALLBACK);
        this.buttonTheme = this.widgetThemes.get(BUTTON);
        this.itemSlotTheme = (WidgetSlotTheme) this.widgetThemes.get(ITEM_SLOT);
        this.fluidSlotTheme = (WidgetSlotTheme) this.widgetThemes.get(FLUID_SLOT);
        this.textFieldTheme = (WidgetTextFieldTheme) this.widgetThemes.get(TEXT_FIELD);
    }

    public String getId() {
        return id;
    }

    public ITheme getParentTheme() {
        return parentTheme;
    }

    public WidgetTheme getFallback() {
        return fallback;
    }

    public WidgetTheme getPanelTheme() {
        return panelTheme;
    }

    public WidgetTheme getButtonTheme() {
        return buttonTheme;
    }

    @Override
    public WidgetSlotTheme getItemSlotTheme() {
        return itemSlotTheme;
    }

    @Override
    public WidgetSlotTheme getFluidSlotTheme() {
        return fluidSlotTheme;
    }

    public WidgetTextFieldTheme getTextFieldTheme() {
        return textFieldTheme;
    }

    public WidgetTheme getWidgetTheme(String id) {
        if (this.widgetThemes.containsKey(id)) {
            return this.widgetThemes.get(id);
        }
        return getFallback();
    }
}
