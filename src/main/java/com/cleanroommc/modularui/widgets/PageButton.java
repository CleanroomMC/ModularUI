package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public class PageButton extends Widget<PageButton> implements Interactable {

    private final int index;
    private final PagedWidget.Controller controller;
    private IDrawable[] inactiveTexture = EMPTY_BACKGROUND;

    public PageButton(int index, PagedWidget.Controller controller) {
        this.index = index;
        this.controller = controller;
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
    public IDrawable[] getBackground() {
        return isActive() || this.inactiveTexture.length == 0 ? super.getBackground() : this.inactiveTexture;
    }

    public boolean isActive() {
        return this.controller.getActivePageIndex() == this.index;
    }

    public PageButton background(boolean active, IDrawable... background) {
        if (active) {
            return background(background);
        }
        this.inactiveTexture = background;
        return this;
    }
}
