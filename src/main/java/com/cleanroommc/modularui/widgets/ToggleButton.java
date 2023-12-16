package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetToggleButtonTheme;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends Widget<ToggleButton> implements Interactable {

    private IBoolValue<?> boolValue;
    private IDrawable selectedBackground;
    private IDrawable selectedHoverBackground;

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) {
            this.boolValue.setBoolValue(!this.boolValue.getBoolValue());
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        WidgetToggleButtonTheme widgetTheme = theme.getToggleButtonTheme();
        return isValueSelected() ? widgetTheme.getSelected() : widgetTheme;
    }

    @Override
    public @Nullable IDrawable getBackground() {
        if (isValueSelected()) {
            return this.selectedBackground;
        }
        return super.getBackground();
    }

    @Override
    public @Nullable IDrawable getHoverBackground() {
        if (isValueSelected()) {
            return this.selectedHoverBackground;
        }
        return super.getHoverBackground();
    }

    public boolean isValueSelected() {
        return this.boolValue.getBoolValue();
    }

    public ToggleButton value(IBoolValue<?> boolValue) {
        this.boolValue = boolValue;
        setValue(boolValue);
        return this;
    }

    public ToggleButton selectedBackground(IDrawable selectedBackground) {
        this.selectedBackground = selectedBackground;
        return this;
    }

    public ToggleButton selectedHoverBackground(IDrawable selectedHoverBackground) {
        this.selectedHoverBackground = selectedHoverBackground;
        return this;
    }
}
