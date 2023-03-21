package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewportTransformation;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.GlStateManager;

public class ViewportScale implements IViewportTransformation {

    private final float x, y;

    public ViewportScale(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int transformX(int x, Area area, boolean toLocal) {
        return (int) (toLocal ? x * this.x : x / this.x);
    }

    @Override
    public int transformY(int y, Area area, boolean toLocal) {
        return (int) (toLocal ? y * this.y : y / this.y);
    }

    @Override
    public void applyOpenGlTransformation() {
        GlStateManager.scale(this.x, this.y, 1);
    }

    @Override
    public void unapplyOpenGlTransformation() {
        GlStateManager.scale(1 / this.x, 1 / this.y, 1);
    }
}
