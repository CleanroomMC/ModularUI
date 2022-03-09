package com.cleanroommc.modularui.api.drawable;

/**
 * Draws a {@link IDrawable} with a fixed size to a offset position
 */
public class SizedDrawable implements IDrawable {

    private final IDrawable drawable;
    private final float fixedWidth, fixedHeight;
    private final float offsetX, offsetY;

    public SizedDrawable(IDrawable drawable, float fixedWidth, float fixedHeight, float offsetX, float offsetY) {
        this.drawable = drawable;
        this.fixedWidth = fixedWidth;
        this.fixedHeight = fixedHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public SizedDrawable(IDrawable drawable, float fixedWidth, float fixedHeight) {
        this(drawable, fixedWidth, fixedHeight, 0, 0);
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        drawable.draw(x + offsetX, y + offsetY, fixedWidth, fixedHeight, partialTicks);
    }
}
