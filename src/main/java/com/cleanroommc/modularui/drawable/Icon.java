package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IDrawable;
import com.cleanroommc.modularui.api.IIcon;
import com.cleanroommc.modularui.utils.Alignment;

/**
 * A {@link IDrawable} wrapper with a fixed size and an alignment.
 */
public class Icon implements IIcon {

    private final IDrawable drawable;
    private int width = 18, height = 18;
    private Alignment alignment = Alignment.Center;

    public Icon(IDrawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public Icon width(int width) {
        this.width = width;
        return this;
    }

    @Override
    public int getHeight() {
        return height + 1;
    }

    public Icon height(int height) {
        this.height = height;
        return this;
    }

    public Icon size(int width, int height) {
        return width(width).height(height);
    }

    public Icon size(int size) {
        return width(size).height(size);
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Icon alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public void draw(int x, int y, int width, int height) {
        x += width * alignment.x - this.width * alignment.x;
        y += height * alignment.y - this.height * alignment.y;
        drawable.draw(x, y, this.width, this.height);
    }
}
