package com.cleanroommc.modularui.api.drawable;

/**
 * Draws a {@link IDrawable} to a offset pos with a offset size
 */
public class OffsetDrawable implements IDrawable {

    private final IDrawable drawable;
    private final float offsetX, offsetY;
    private final float widthOffset, heightOffset;

    public OffsetDrawable(IDrawable drawable, float offsetX, float offsetY, float widthOffset, float heightOffset) {
        this.drawable = drawable;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.widthOffset = widthOffset;
        this.heightOffset = heightOffset;
    }

    public OffsetDrawable(IDrawable drawable, float offsetX, float offsetY) {
        this(drawable, offsetX, offsetY, 0, 0);
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        drawable.draw(x + offsetX, y + offsetY, width + widthOffset, height + heightOffset, partialTicks);
    }
}
