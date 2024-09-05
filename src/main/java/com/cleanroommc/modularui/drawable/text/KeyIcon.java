package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Box;

import net.minecraft.client.gui.FontRenderer;

/**
 * An icon which represents a {@link IKey} object.
 * Note: This class assumes the string will be a single line!
 */
public class KeyIcon implements IIcon {

    private final IKey key;
    private FontRenderer overrideFontRenderer;
    private final Box margin = new Box();

    public KeyIcon(IKey key) {
        this.key = key;
    }

    public FontRenderer getFontRenderer() {
        return this.overrideFontRenderer != null ? this.overrideFontRenderer : MCHelper.getFontRenderer();
    }

    @Override
    public int getWidth() {
        return getFontRenderer().getStringWidth(key.get()) + this.margin.horizontal();
    }

    @Override
    public int getHeight() {
        return getFontRenderer().FONT_HEIGHT + this.margin.vertical();
    }

    @Override
    public Box getMargin() {
        return null;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        int w = getWidth(), h = getHeight();
        x += (int) (width / 2f - w / 2f);
        y += (int) (height / 2f - h / 2f);
        this.key.draw(context, x, y, width, height, widgetTheme);
    }

    public KeyIcon margin(int left, int right, int top, int bottom) {
        this.margin.all(left, right, top, bottom);
        return this;
    }

    public KeyIcon margin(int horizontal, int vertical) {
        this.margin.all(horizontal, vertical);
        return this;
    }

    public KeyIcon margin(int all) {
        this.margin.all(all);
        return this;
    }

    public KeyIcon marginLeft(int val) {
        this.margin.left(val);
        return this;
    }

    public KeyIcon marginRight(int val) {
        this.margin.right(val);
        return this;
    }

    public KeyIcon marginTop(int val) {
        this.margin.top(val);
        return this;
    }

    public KeyIcon marginBottom(int val) {
        this.margin.bottom(val);
        return this;
    }

    public KeyIcon fontRenderer(FontRenderer fr) {
        this.overrideFontRenderer = fr;
        return this;
    }

    @Override
    public String toString() {
        return "KeyIcon(" + this.key.get() + ")";
    }
}
