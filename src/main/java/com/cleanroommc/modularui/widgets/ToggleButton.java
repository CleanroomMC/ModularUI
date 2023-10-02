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
    private IDrawable disabledBackground;
    private IDrawable disabledHoverBackground;

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) {
            this.boolValue.setBoolValue(!this.boolValue.getBoolValue());
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
        if (this.disabledBackground == null) {
            this.disabledBackground = theme.getToggleButtonTheme().getDisabledBackground();
        }
        if (this.disabledHoverBackground == null) {
            this.disabledHoverBackground = theme.getToggleButtonTheme().getDisabledHoverBackground();
        }
    }

    @Override
    public IDrawable getCurrentBackground() {
        if (isValueEnabled()) {
            return super.getCurrentBackground();
        }
        return this.disabledHoverBackground != null && isHovering() ? this.disabledHoverBackground : this.disabledBackground;
    }

    @Override
    public void drawBackground(GuiContext context, WidgetTheme widgetTheme) {
        IDrawable bg = getCurrentBackground();
        if (bg != null) {
            if (isValueEnabled()) {
                bg.applyThemeColor(context.getTheme(), widgetTheme);
            } else if (bg.canApplyTheme()) {
                Color.setGlColor(((WidgetToggleButtonTheme) widgetTheme).getDisabledColor());
            } else {
                Color.setGlColor(Color.WHITE.normal);
            }
            bg.drawAtZero(context, getArea());
        }
    }

    public boolean isValueEnabled() {
        return this.boolValue.getBoolValue();
    }

    public ToggleButton value(IBoolValue<?> boolValue) {
        this.boolValue = boolValue;
        setValue(boolValue);
        return this;
    }

    public ToggleButton disabledBackground(IDrawable disabledBackground) {
        this.disabledBackground = disabledBackground;
        return this;
    }

    public ToggleButton disabledHoverBackground(IDrawable disabledHoverBackground) {
        this.disabledHoverBackground = disabledHoverBackground;
        return this;
    }
}
