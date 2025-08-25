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

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;

public class Circle implements IDrawable, IJsonSerializable, IAnimatable<Circle> {

    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    public Circle setColorInner(int colorInner) {
        return colorInner(colorInner);
    }

    public Circle setColorOuter(int colorOuter) {
        return colorOuter(colorOuter);
    }

    public Circle setColor(int inner, int outer) {
        return color(inner, outer);
    }

    public Circle setSegments(int segments) {
        return segments(segments);
    }

    @Contract("_ -> this")
    public Circle colorInner(int colorInner) {
        this.colorInner = colorInner;
        return this;
    }

    public Circle colorOuter(int colorOuter) {
        this.colorOuter = colorOuter;
        return this;
    }

    public Circle color(int inner, int outer) {
        this.colorInner = inner;
        this.colorOuter = outer;
        return this;
    }

    public Circle color(int color) {
        return color(color, color);
    }

    public Circle segments(int segments) {
        this.segments = segments;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        GuiDraw.drawEllipse(x0, y0, width, height, this.colorInner, this.colorOuter, this.segments);
    }

    @Override
    public void loadFromJson(JsonObject json) {
        this.colorInner = JsonHelper.getColor(json, Color.WHITE.main, "colorInner", "color");
        this.colorOuter = JsonHelper.getColor(json, Color.WHITE.main, "colorOuter", "color");
        this.segments = JsonHelper.getInt(json, 40, "segments");
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("colorInner", this.colorInner);
        json.addProperty("colorOuter", this.colorOuter);
        json.addProperty("segments", this.segments);
        return true;
    }

    @Override
    public Circle interpolate(Circle start, Circle end, float t) {
        this.colorInner = Color.interpolate(start.colorInner, end.colorInner, t);
        this.colorOuter = Color.interpolate(start.colorOuter, end.colorOuter, t);
        this.segments = Interpolations.lerp(start.segments, end.segments, t);
        return this;
    }

    @Override
    public Circle copyOrImmutable() {
        return new Circle()
                .setColor(this.colorInner, this.colorOuter)
                .setSegments(this.segments);
    }
}
