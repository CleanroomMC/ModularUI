package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewportTransformation;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.GlStateManager;

public class ViewportTranslation implements IViewportTransformation {

    private final int x, y;

    public ViewportTranslation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int transformX(int x, Area area, boolean toLocal) {
        return toLocal ? x + this.x : x - this.x;
    }

    @Override
    public int transformY(int y, Area area, boolean toLocal) {
        return toLocal ? y + this.y : y - this.y;
    }

    @Override
    public void applyOpenGlTransformation() {
        GlStateManager.translate(-this.x, -this.y, 0);
    }

    @Override
    public void unapplyOpenGlTransformation() {
        GlStateManager.translate(this.x, this.y, 0);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
