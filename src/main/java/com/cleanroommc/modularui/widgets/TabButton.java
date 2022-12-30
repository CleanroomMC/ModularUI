package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class TabButton extends Widget<TabButton> implements Interactable {

    private TabContainer tabContainer;
    private final int index;
    private IDrawable[] activeTexture = null;
    private int location = 0;

    public TabButton(int index) {
        this.index = index;
    }

    @ApiStatus.Internal
    public void setTabContainer(TabContainer tabContainer) {
        this.tabContainer = tabContainer;
    }

    public void updateDefaultTexture() {
        if (getBackground().length == 0 && this.activeTexture == null) {
            switch (this.tabContainer.getButtonBarSide()) {
                case TOP:
                    background(GuiTextures.TAB_TOP.get(this.location, false));
                    activeBackground(GuiTextures.TAB_TOP.get(this.location, true));
                    break;
                case BOTTOM:
                    background(GuiTextures.TAB_BOTTOM.get(this.location, false));
                    activeBackground(GuiTextures.TAB_BOTTOM.get(this.location, true));
                    break;
                case LEFT:
                    background(GuiTextures.TAB_LEFT.get(this.location, false));
                    activeBackground(GuiTextures.TAB_LEFT.get(this.location, true));
                    break;
                case RIGHT:
                    background(GuiTextures.TAB_RIGHT.get(this.location, false));
                    activeBackground(GuiTextures.TAB_RIGHT.get(this.location, true));
                    break;
            }
        }
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (isActive() && this.activeTexture != null) {
            for (IDrawable drawable : this.activeTexture) {
                drawable.draw(0, 0, getArea().width, getArea().height);
            }
            return;
        }
        super.drawBackground(partialTicks);
    }

    @Override
    public @NotNull Result onMouseTapped(int mouseButton) {
        if (this.tabContainer.getCurrentPageIndex() != this.index) {
            this.tabContainer.setPage(this.index);
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public int getDefaultHeight() {
        return this.tabContainer.getButtonBarSide() == TabContainer.Side.TOP || this.tabContainer.getButtonBarSide() == TabContainer.Side.BOTTOM ? 32 : 28;
    }

    @Override
    public int getDefaultWidth() {
        return this.tabContainer.getButtonBarSide() == TabContainer.Side.TOP || this.tabContainer.getButtonBarSide() == TabContainer.Side.BOTTOM ? 28 : 32;
    }

    public boolean isActive() {
        return this.tabContainer.getCurrentPageIndex() == this.index;
    }

    public TabButton activeBackground(IDrawable... activeTexture) {
        this.activeTexture = activeTexture;
        return this;
    }

    public TabButton background(boolean active, IDrawable... background) {
        return active ? activeBackground(background) : background(background);
    }

    public TabButton start() {
        this.location = -1;
        return this;
    }

    public TabButton middle() {
        this.location = 0;
        return this;
    }

    public TabButton end() {
        this.location = 1;
        return this;
    }
}
