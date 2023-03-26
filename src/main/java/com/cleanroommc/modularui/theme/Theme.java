package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Color;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

public class Theme implements ITheme {

    public static final Theme DEFAULT;

    public static ITheme get(String id) {
        return ThemeHandler.get(id);
    }

    private final String id;
    private Theme parentTheme;
    private final IDrawable panelBackground;
    private final IDrawable buttonBackground;
    private final IDrawable disabledButtonBackground;
    private final Integer textColor;
    private final Integer buttonTextColor;
    private final Boolean textShadow;
    private final Boolean buttonTextShadow;
    private final Integer panelColor;
    private final Integer buttonColor;

    Theme(String id, IDrawable panelBackground, IDrawable buttonBackground,
          IDrawable disabledButtonBackground, Integer textColor, Integer buttonTextColor,
          Boolean textShadow, Boolean buttonTextShadow, Integer panelColor, Integer buttonColor) {
        this.id = id;
        this.panelBackground = panelBackground;
        this.buttonBackground = buttonBackground;
        this.disabledButtonBackground = disabledButtonBackground;
        this.textColor = textColor;
        this.buttonTextColor = buttonTextColor;
        this.textShadow = textShadow;
        this.buttonTextShadow = buttonTextShadow;
        this.panelColor = panelColor;
        this.buttonColor = buttonColor;
    }

    boolean lateInitThemeParent(Theme parentTheme) {
        Set<Theme> parents = new ObjectOpenHashSet<>();
        parents.add(this);
        Theme parent = parentTheme;
        while (parent != null) {
            if (parents.contains(parent)) {
                return false;
            }
            parents.add(parent);
            parent = parent.getParentTheme();
        }
        this.parentTheme = parentTheme;
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Theme getParentTheme() {
        return parentTheme;
    }

    @Override
    public IDrawable getPanelBackground() {
        if (this.panelBackground == null) {
            return getParentTheme().getPanelBackground();
        }
        return panelBackground;
    }

    @Override
    public IDrawable getButtonBackground() {
        if (this.buttonBackground == null) {
            return getParentTheme().getButtonBackground();
        }
        return buttonBackground;
    }

    @Override
    public IDrawable getDisabledButtonBackground() {
        if (this.disabledButtonBackground == null) {
            return getParentTheme().getDisabledButtonBackground();
        }
        return disabledButtonBackground;
    }

    @Override
    public int getTextColor() {
        if (this.textColor == null) {
            return getParentTheme().getTextColor();
        }
        return textColor;
    }

    @Override
    public int getButtonTextColor() {
        if (this.buttonTextColor == null) {
            return getParentTheme().getButtonTextColor();
        }
        return buttonTextColor;
    }

    @Override
    public boolean isTextShadow() {
        if (this.textShadow == null) {
            return getParentTheme().isTextShadow();
        }
        return textShadow;
    }

    @Override
    public boolean isButtonTextShadow() {
        if (this.buttonTextShadow == null) {
            return getParentTheme().isButtonTextShadow();
        }
        return buttonTextShadow;
    }

    @Override
    public int getPanelColor() {
        if (this.panelColor == null) {
            return getParentTheme().getPanelColor();
        }
        return panelColor;
    }

    @Override
    public int getButtonColor() {
        if (this.buttonColor == null) {
            return getParentTheme().getButtonColor();
        }
        return buttonColor;
    }

    static {
        DEFAULT = new Theme("DEFAULT", GuiTextures.BACKGROUND, GuiTextures.BUTTON, GuiTextures.SLOT_DARK, 0xFF404040, Color.WHITE.normal, false, true, 0, 0);
    }
}
