package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.theme.Theme;

public interface ITheme {

    static ITheme getDefault() {
        return Theme.DEFAULT;
    }

    String getId();

    Theme getParentTheme();

    IDrawable getPanelBackground();

    IDrawable getButtonBackground();

    IDrawable getDisabledButtonBackground();

    int getTextColor();

    int getButtonTextColor();

    boolean isTextShadow();

    boolean isButtonTextShadow();

    int getPanelColor();

    int getButtonColor();
}
