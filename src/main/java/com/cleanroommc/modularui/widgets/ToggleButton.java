package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetToggleButtonTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;

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
        return theme.getToggleButtonTheme();
    }

    @Override
    public void applyTheme(ITheme theme) {
        super.applyTheme(theme);
        if (this.selectedBackground == null) {
            this.selectedBackground = theme.getToggleButtonTheme().getSelectedBackground();
        }
        if (this.selectedHoverBackground == null) {
            this.selectedHoverBackground = theme.getToggleButtonTheme().getSelectedHoverBackground();
        }
    }

    @Override
    public IDrawable getCurrentBackground() {
        if (isValueSelected()) {
            return this.selectedHoverBackground != null && isHovering() ? this.selectedHoverBackground : this.selectedBackground;
        }
        return super.getCurrentBackground();
    }

    @Override
    public void drawBackground(GuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentBackground();
        if (bg != null) {
            if (isValueSelected()) {
                Color.setGlColor(((WidgetToggleButtonTheme) widgetTheme).getSelectedColor());
            } else if (bg.canApplyTheme()) {
                bg.applyThemeColor(context.getTheme(), widgetTheme);
            } else {
                Color.setGlColor(Color.WHITE.main);
            }
            bg.drawAtZero(context, getArea());
        }
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
