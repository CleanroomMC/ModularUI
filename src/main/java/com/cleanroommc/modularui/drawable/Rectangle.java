package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.animation.IAnimatable;
import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.IntConsumer;

public class Rectangle implements IDrawable, IJsonSerializable, IAnimatable<Rectangle> {

    private int cornerRadius, colorTL, colorTR, colorBL, colorBR, cornerSegments;
    private float borderThickness;
    private boolean canApplyTheme = false;

    public Rectangle() {
        color(0xFFFFFFFF);
        this.cornerRadius = 0;
        this.cornerSegments = 6;
    }

    public int getColor() {
        return this.colorTL;
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setCornerRadius(int cornerRadius) {
        return cornerRadius(cornerRadius);
    }

    public Rectangle cornerRadius(int cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
        if (this.borderThickness > 0 && this.cornerRadius > 0) {
            ModularUI.LOGGER.error("Hollow rectangles currently can't have a corner radius.");
        }
        return this;
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setColor(int colorTL, int colorTR, int colorBL, int colorBR) {
        return color(colorTL, colorTR, colorBL, colorBR);
    }

    public Rectangle color(int colorTL, int colorTR, int colorBL, int colorBR) {
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBL = colorBL;
        this.colorBR = colorBR;
        return this;
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setVerticalGradient(int colorTop, int colorBottom) {
        return verticalGradient(colorTop, colorBottom);
    }

    public Rectangle verticalGradient(int colorTop, int colorBottom) {
        return color(colorTop, colorTop, colorBottom, colorBottom);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setHorizontalGradient(int colorLeft, int colorRight) {
        return horizontalGradient(colorLeft, colorRight);
    }

    public Rectangle horizontalGradient(int colorLeft, int colorRight) {
        return color(colorLeft, colorRight, colorLeft, colorRight);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setColor(int color) {
        return color(color);
    }

    public Rectangle color(int color) {
        return color(color, color, color, color);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setCornerSegments(int cornerSegments) {
        return cornerSegments(cornerSegments);
    }

    public Rectangle cornerSegments(int cornerSegments) {
        this.cornerSegments = cornerSegments;
        return this;
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public Rectangle setCanApplyTheme(boolean canApplyTheme) {
        return canApplyTheme(canApplyTheme);
    }

    public Rectangle canApplyTheme(boolean canApplyTheme) {
        this.canApplyTheme = canApplyTheme;
        return this;
    }

    public Rectangle solid() {
        this.borderThickness = 0;
        return this;
    }

    public Rectangle hollow(float borderThickness) {
        this.borderThickness = borderThickness;
        if (borderThickness > 0 && this.cornerRadius > 0) {
            ModularUI.LOGGER.error("Hollow rectangles currently can't have a corner radius.");
        }
        return this;
    }

    public Rectangle hollow() {
        return hollow(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        if (this.borderThickness <= 0) {
            if (this.cornerRadius <= 0) {
                GuiDraw.drawRect(x0, y0, width, height, this.colorTL, this.colorTR, this.colorBL, this.colorBR);
                return;
            }
            GuiDraw.drawRoundedRect(x0, y0, width, height, this.colorTL, this.colorTR, this.colorBL, this.colorBR, this.cornerRadius, this.cornerSegments);
        } else {
            float d = this.borderThickness;
            float x1 = x0 + width, y1 = y0 + height;
            Platform.setupDrawColor();
            Platform.setupDrawGradient();
            Platform.startDrawing(Platform.DrawMode.TRIANGLE_STRIP, Platform.VertexFormat.POS_COLOR, buffer -> {
                v(buffer, x0, y0, this.colorTL);
                v(buffer, x1 - d, y0 + d, this.colorTR);
                v(buffer, x1, y0, this.colorTR);
                v(buffer, x1 - d, y1 - d, this.colorBR);
                v(buffer, x1, y1, this.colorBR);
                v(buffer, x0 + d, y1 - d, this.colorBL);
                v(buffer, x0, y1, this.colorBL);
                v(buffer, x0 + d, y0 + d, this.colorTL);
                v(buffer, x0, y0, this.colorTL);
                v(buffer, x1 - d, y0 + d, this.colorTR);
            });
            Platform.endDrawGradient();
        }
    }

    private static void v(BufferBuilder buffer, float x, float y, int c) {
        buffer.pos(x, y, 0).color(Color.getRed(c), Color.getGreen(c), Color.getBlue(c), Color.getAlpha(c)).endVertex();
    }

    @Override
    public boolean canApplyTheme() {
        return this.canApplyTheme;
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if (json.has("color")) {
            color(Color.ofJson(json.get("color")));
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
        if (JsonHelper.getBoolean(json, false, "solid")) {
            this.borderThickness = 0;
        } else if (JsonHelper.getBoolean(json, false, "hollow")) {
            this.borderThickness = 1;
        } else {
            this.borderThickness = JsonHelper.getFloat(json, 0, "borderThickness");
        }
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("colorTL", this.colorTL);
        json.addProperty("colorTR", this.colorTR);
        json.addProperty("colorBL", this.colorBL);
        json.addProperty("colorBR", this.colorBR);
        json.addProperty("cornerRadius", this.cornerRadius);
        json.addProperty("cornerSegments", this.cornerSegments);
        json.addProperty("borderThickness", this.borderThickness);
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
        this.colorTL = Color.lerp(start.colorTL, end.colorTL, t);
        this.colorTR = Color.lerp(start.colorTR, end.colorTR, t);
        this.colorBL = Color.lerp(start.colorBL, end.colorBL, t);
        this.colorBR = Color.lerp(start.colorBR, end.colorBR, t);
        return this;
    }

    @Override
    public Rectangle copyOrImmutable() {
        return new Rectangle()
                .color(this.colorTL, this.colorTR, this.colorBL, this.colorBR)
                .cornerRadius(this.cornerRadius)
                .cornerSegments(this.cornerSegments)
                .canApplyTheme(this.canApplyTheme);
    }
}
