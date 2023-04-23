package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Box;

/**
 * A {@link IDrawable} wrapper with a fixed size and an alignment.
 */
public class Icon implements IIcon {

    private final IDrawable drawable;
    private int width = 18, height = 18;
    private Alignment alignment = Alignment.Center;
    private final Box margin = new Box();

    public Icon(IDrawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Box getMargin() {
        return margin;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        x += this.margin.left;
        y += this.margin.top;
        width -= this.margin.horizontal();
        height -= this.margin.vertical();
        if (this.width > 0) {
            x += width * alignment.x - this.width * alignment.x;
            width = this.width;
        }
        if (this.height > 0) {
            y += height * alignment.y - this.height * alignment.y;
            height = this.height;
        }
        drawable.draw(context, x, y, width, height);
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Icon width(int width) {
        this.width = width;
        return this;
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

    public Icon alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Icon margin(int left, int right, int top, int bottom) {
        this.margin.all(left, right, top, bottom);
        return this;
    }

    public Icon margin(int horizontal, int vertical) {
        this.margin.all(horizontal, vertical);
        return this;
    }

    public Icon margin(int all) {
        this.margin.all(all);
        return this;
    }

    public Icon marginLeft(int val) {
        this.margin.left(val);
        return this;
    }

    public Icon marginRight(int val) {
        this.margin.right(val);
        return this;
    }

    public Icon marginTop(int val) {
        this.margin.top(val);
        return this;
    }

    public Icon marginBottom(int val) {
        this.margin.bottom(val);
        return this;
    }
}
