package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;

import org.jetbrains.annotations.Nullable;

/**
 * A drawable with a name in a row. The layout behaves as if
 * <p>
 * {@code Flow.row().mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)}.
 * </p>
 */
public class NamedDrawableRow implements IDrawable {

    private IKey name;
    private IIcon drawable;

    public NamedDrawableRow() {
        this(null, null);
    }

    public NamedDrawableRow(@Nullable IKey name, @Nullable IIcon drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.name != null) {
            this.name.drawAligned(context, x, y, width, height, widgetTheme, Alignment.CenterLeft);
        }
        if (this.drawable != null) {
            int wd = this.drawable.getWidth() + this.drawable.getMargin().horizontal();
            int xd = x + width - wd;
            this.drawable.draw(context, xd, y, wd, height, widgetTheme);

        }
    }

    @Override
    public int getDefaultWidth() {
        int w = 0;
        if (this.name != null) w += this.name.getDefaultWidth();
        if (this.drawable != null) w += this.drawable.getWidth();
        return w;
    }

    @Override
    public int getDefaultHeight() {
        int h = 0;
        if (this.name != null) h = Math.max(h, this.name.getDefaultHeight());
        if (this.drawable != null) h = Math.max(h, this.drawable.getHeight());
        return h;
    }

    public @Nullable IKey getNameKey() {
        return name;
    }

    public @Nullable IIcon getDrawable() {
        return drawable;
    }

    public NamedDrawableRow name(@Nullable IKey key) {
        this.name = key;
        return this;
    }

    public NamedDrawableRow drawable(@Nullable IIcon icon) {
        this.drawable = icon;
        return this;
    }
}
