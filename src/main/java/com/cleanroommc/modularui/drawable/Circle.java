package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Contract;

public class Circle implements IDrawable {

    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    @Contract("_ -> this")
    public Circle setColorInner(int colorInner) {
        this.colorInner = colorInner;
        return this;
    }

    public Circle setColorOuter(int colorOuter) {
        this.colorOuter = colorOuter;
        return this;
    }

    public Circle setColor(int inner, int outer) {
        this.colorInner = inner;
        this.colorOuter = outer;
        return this;
    }

    public Circle setSegments(int segments) {
        this.segments = segments;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height) {
        GuiDraw.drawEllipse(x0, y0, width, height, this.colorInner, this.colorOuter, this.segments);
    }

    @Override
    public void loadFromJson(JsonObject json) {
        this.colorInner = JsonHelper.getColor(json, Color.WHITE.main, "colorInner", "color");
        this.colorOuter = JsonHelper.getColor(json, Color.WHITE.main, "colorOuter", "color");
        this.segments = JsonHelper.getInt(json, 40, "segments");
    }
}
