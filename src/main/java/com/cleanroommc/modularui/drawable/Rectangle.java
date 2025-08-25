package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.animation.IAnimatable;
import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.JsonHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.IntConsumer;

public class Rectangle implements IDrawable, IJsonSerializable, IAnimatable<Rectangle> {

    public static final double PI_2 = Math.PI / 2;

    private int cornerRadius, colorTL, colorTR, colorBL, colorBR, cornerSegments;
    private boolean canApplyTheme = false;

    public Rectangle() {
        setColor(0xFFFFFFFF);
        this.cornerRadius = 0;
        this.cornerSegments = 6;
    }

    public int getColor() {
        return this.colorTL;
    }

    public Rectangle setCornerRadius(int cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
        return this;
    }

    public Rectangle setColor(int colorTL, int colorTR, int colorBL, int colorBR) {
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBL = colorBL;
        this.colorBR = colorBR;
        return this;
    }

    public Rectangle setVerticalGradient(int colorTop, int colorBottom) {
        return setColor(colorTop, colorTop, colorBottom, colorBottom);
    }

    public Rectangle setHorizontalGradient(int colorLeft, int colorRight) {
        return setColor(colorLeft, colorRight, colorLeft, colorRight);
    }

    public Rectangle setColor(int color) {
        return setColor(color, color, color, color);
    }

    public Rectangle setCornerSegments(int cornerSegments) {
        this.cornerSegments = cornerSegments;
        return this;
    }

    public Rectangle setCanApplyTheme(boolean canApplyTheme) {
        this.canApplyTheme = canApplyTheme;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        if (this.cornerRadius <= 0) {
            GuiDraw.drawRect(x0, y0, width, height, this.colorTL, this.colorTR, this.colorBL, this.colorBR);
            return;
        }
        GuiDraw.drawRoundedRect(x0, y0, width, height, this.colorTL, this.colorTR, this.colorBL, this.colorBR, this.cornerRadius, this.cornerSegments);
    }

    @Override
    public boolean canApplyTheme() {
        return this.canApplyTheme;
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has("color")) {
            setColor(Color.ofJson(json.get("color")));
        }
        if (json.has("colorTop")) {
            int c = Color.ofJson(json.get("colorTop"));
            this.colorTL = c;
            this.colorTR = c;
        }
        if (json.has("colorBottom")) {
            int c = Color.ofJson(json.get("colorBottom"));
            this.colorBL = c;
            this.colorBR = c;
        }
        if (json.has("colorLeft")) {
            int c = Color.ofJson(json.get("colorLeft"));
            this.colorTL = c;
            this.colorBL = c;
        }
        if (json.has("colorRight")) {
            int c = Color.ofJson(json.get("colorRight"));
            this.colorTR = c;
            this.colorBR = c;
        }
        setColor(json, val -> this.colorTL = val, "colorTopLeft", "colorTL");
        setColor(json, val -> this.colorTR = val, "colorTopRight", "colorTR");
        setColor(json, val -> this.colorBL = val, "colorBottomLeft", "colorBL");
        setColor(json, val -> this.colorBR = val, "colorBottomRight", "colorBR");
        this.cornerRadius = JsonHelper.getInt(json, 0, "cornerRadius");
        this.cornerSegments = JsonHelper.getInt(json, 10, "cornerSegments");
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("colorTL", this.colorTL);
        json.addProperty("colorTR", this.colorTR);
        json.addProperty("colorBL", this.colorBL);
        json.addProperty("colorBR", this.colorBR);
        json.addProperty("cornerRadius", this.cornerRadius);
        json.addProperty("cornerSegments", this.cornerSegments);
        return true;
    }

    private void setColor(JsonObject json, IntConsumer color, String... keys) {
        JsonElement element = JsonHelper.getJsonElement(json, keys);
        if (element != null) {
            color.accept(Color.ofJson(element));
        }
    }

    @Override
    public Rectangle interpolate(Rectangle start, Rectangle end, float t) {
        this.cornerRadius = Interpolations.lerp(start.cornerRadius, end.cornerRadius, t);
        this.cornerSegments = Interpolations.lerp(start.cornerSegments, end.cornerSegments, t);
        this.colorTL = Color.interpolate(start.colorTL, end.colorTL, t);
        this.colorTR = Color.interpolate(start.colorTR, end.colorTR, t);
        this.colorBL = Color.interpolate(start.colorBL, end.colorBL, t);
        this.colorBR = Color.interpolate(start.colorBR, end.colorBR, t);
        return this;
    }

    @Override
    public Rectangle copyOrImmutable() {
        return new Rectangle()
                .setColor(this.colorTL, this.colorTR, this.colorBL, this.colorBR)
                .setCornerRadius(this.cornerRadius)
                .setCornerSegments(this.cornerSegments)
                .setCanApplyTheme(this.canApplyTheme);
    }
}
