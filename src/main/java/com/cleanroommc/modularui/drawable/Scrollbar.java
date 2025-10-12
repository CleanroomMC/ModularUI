package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;

public class Scrollbar implements IDrawable, IJsonSerializable {

    public static final Scrollbar DEFAULT = new Scrollbar(false);
    public static final Scrollbar VANILLA = new Scrollbar(true);

    public static Scrollbar ofJson(JsonObject json) {
        if (JsonHelper.getBoolean(json, false, "striped", "vanilla")) {
            return VANILLA;
        }
        return DEFAULT;
    }

    private final boolean striped;

    public Scrollbar(boolean striped) {
        this.striped = striped;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        //applyColor(widgetTheme.getColor());
        GuiDraw.drawRect(x, y, width, height, Color.average(0xffeeeeee, widgetTheme.getColor()));
        GuiDraw.drawRect(x + 1, y + 1, width - 1, height - 1, Color.mix(0xff666666, widgetTheme.getColor()));
        GuiDraw.drawRect(x + 1, y + 1, width - 2, height - 2, Color.mix(0xffaaaaaa, widgetTheme.getColor()));

        if (isStriped()) {
            if (height <= 5 && width <= 5) return;
            int color = widgetTheme.getTextColor();
            if (height >= width) {
                int start = y + 2;
                int end = height + start - 4;
                for (int cy = start; cy < end; cy += 2) {
                    GuiDraw.drawRect(x + 2, cy, width - 4, 1, color);
                }
            } else {
                int start = x + 2;
                int end = width + start - 4;
                for (int cx = start; cx <= end; cx += 2) {
                    GuiDraw.drawRect(cx, y + 2, 1, height - 4, color);
                }
            }
        }
    }

    @Override
    public boolean canApplyTheme() {
        return true;
    }

    public boolean isStriped() {
        return striped;
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("striped", this.striped);
        return true;
    }
}
