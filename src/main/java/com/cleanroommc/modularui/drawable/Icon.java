package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widget.sizer.Box;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonObject;

/**
 * A {@link IDrawable} wrapper with a fixed size and an alignment.
 */
public class Icon implements IIcon {

    private final IDrawable drawable;
    private int width = 0, height = 0;
    private Alignment alignment = Alignment.Center;
    private final Box margin = new Box();
    private int color = 0;

    public Icon(IDrawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public Box getMargin() {
        return this.margin;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        x += this.margin.left;
        y += this.margin.top;
        width -= this.margin.horizontal();
        height -= this.margin.vertical();
        if (this.width > 0) {
            x += (int) (width * this.alignment.x - this.width * this.alignment.x);
            width = this.width;
        }
        if (this.height > 0) {
            y += (int) (height * this.alignment.y - this.height * this.alignment.y);
            height = this.height;
        }
        if (this.color != 0 && this.color != widgetTheme.getColor()) {
            widgetTheme = widgetTheme.withColor(this.color);
        }
        this.drawable.draw(context, x, y, width, height, widgetTheme);
    }

    public Alignment getAlignment() {
        return this.alignment;
    }

    public Icon width(int width) {
        this.width = Math.max(0, width);
        return this;
    }

    public Icon height(int height) {
        this.height = Math.max(0, height);
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

    public Icon color(int color) {
        this.color = color;
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

    @Override
    public void loadFromJson(JsonObject json) {
        this.width = (json.has("autoWidth") || json.has("autoSize")) &&
                JsonHelper.getBoolean(json, true, "autoWidth", "autoSize") ? 0 :
                JsonHelper.getInt(json, 0, "width", "w", "size");
        this.height = (json.has("autoHeight") || json.has("autoSize")) &&
                JsonHelper.getBoolean(json, true, "autoHeight", "autoSize") ? 0 :
                JsonHelper.getInt(json, 0, "height", "h", "size");
        this.alignment = JsonHelper.deserialize(json, Alignment.class, Alignment.Center, "alignment", "align");
        this.margin.all(JsonHelper.getInt(json, 0, "margin"));
        if (json.has("marginHorizontal")) {
            this.margin.left = json.get("marginHorizontal").getAsInt();
            this.margin.right = this.margin.left;
        }
        if (json.has("marginVertical")) {
            this.margin.top = json.get("marginVertical").getAsInt();
            this.margin.bottom = this.margin.top;
        }
        this.margin.top = JsonHelper.getInt(json, this.margin.top, "marginTop");
        this.margin.bottom = JsonHelper.getInt(json, this.margin.bottom, "marginBottom");
        this.margin.left = JsonHelper.getInt(json, this.margin.left, "marginLeft");
        this.margin.right = JsonHelper.getInt(json, this.margin.right, "marginRight");
    }

    public static Icon ofJson(JsonObject json) {
        return JsonHelper.deserialize(json, IDrawable.class, IDrawable.EMPTY, "drawable", "icon").asIcon();
    }
}
