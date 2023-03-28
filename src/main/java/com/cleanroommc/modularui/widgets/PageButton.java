package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DrawableArray;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public class PageButton extends Widget<PageButton> implements Interactable {

    private final int index;
    private final PagedWidget.Controller controller;
    private IDrawable inactiveTexture = null;

    public PageButton(int index, PagedWidget.Controller controller) {
        this.index = index;
        this.controller = controller;
    }

    @Override
    public void applyTheme(ITheme theme) {
        super.applyTheme(theme);
        /*applyThemeBackground(theme.getButtonBackground());
        if (isUseThemeBackground()) {
            this.inactiveTexture = ArrayUtils.addAll(new IDrawable[]{theme.getDisabledButtonBackground()}, this.inactiveTexture);
        }*/
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getButtonTheme();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isActive()) {
            this.controller.setPage(this.index);
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public IDrawable getBackground() {
        return isActive() || this.inactiveTexture == null ? super.getBackground() : this.inactiveTexture;
    }

    public boolean isActive() {
        return this.controller.getActivePageIndex() == this.index;
    }

    public PageButton background(boolean active, IDrawable... background) {
        if (active) {
            return background(background);
        }
        if (background.length == 0) {
            this.inactiveTexture = null;
        } else if (background.length == 1) {
            this.inactiveTexture = background[0];
        } else {
            this.inactiveTexture = new DrawableArray(background);
        }
        return this;
    }
}
