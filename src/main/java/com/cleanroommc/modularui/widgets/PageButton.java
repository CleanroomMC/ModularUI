package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DrawableStack;
import com.cleanroommc.modularui.drawable.TabTexture;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeSelectable;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;

public class PageButton extends Widget<PageButton> implements Interactable {

    private final int index;
    private final PagedWidget.Controller controller;
    private IDrawable inactiveTexture = null;
    private boolean invert = false;

    public PageButton(int index, PagedWidget.Controller controller) {
        this.index = index;
        this.controller = controller;
        disableHoverBackground();
    }

    @Override
    public WidgetTheme getWidgetThemeInternal(ITheme theme) {
        WidgetThemeSelectable widgetTheme = theme.getToggleButtonTheme();
        return isActive() ^ invertSelected() ? widgetTheme : widgetTheme.getSelected();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isActive()) {
            this.controller.setPage(this.index);
            Interactable.playButtonClickSound();
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
            this.inactiveTexture = new DrawableStack(background);
        }
        return this;
    }

    public PageButton tab(TabTexture texture, int location) {
        return background(invertSelected(), texture.get(location, invertSelected()))
                .background(!invertSelected(), texture.get(location, !invertSelected()))
                .disableHoverBackground()
                .size(texture.getWidth(), texture.getHeight());
    }

    public PageButton invertSelected(boolean invert) {
        this.invert = invert;
        return getThis();
    }

    public boolean invertSelected() {
        return this.invert;
    }
}
