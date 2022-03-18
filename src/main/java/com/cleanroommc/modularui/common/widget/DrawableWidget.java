package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.google.gson.JsonObject;

public class DrawableWidget extends Widget {

    private IDrawable drawable;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        if (type.equals("image")) {
            setDrawable(UITexture.ofJson(json));
        }
    }

    public DrawableWidget setDrawable(IDrawable drawable) {
        if (drawable instanceof Text || drawable instanceof TextSpan) {
            ModularUI.LOGGER.warn("Please use TextWidget for Text");
        }
        this.drawable = drawable;
        return this;
    }

    @Override
    public void onScreenUpdate() {
        drawable.tick();
    }

    @Override
    public void drawInBackground(float partialTicks) {
        if (drawable != null) {
            drawable.draw(Pos2d.ZERO, getSize(), partialTicks);
        }
    }
}
