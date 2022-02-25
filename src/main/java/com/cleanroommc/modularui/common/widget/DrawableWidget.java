package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.common.drawable.IDrawable;
import com.cleanroommc.modularui.common.drawable.Text;
import com.cleanroommc.modularui.common.drawable.TextSpan;
import com.cleanroommc.modularui.common.drawable.UITexture;
import com.google.gson.JsonObject;

public class DrawableWidget extends Widget implements IWidgetDrawable {

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
            ModularUIMod.LOGGER.warn("Please use TextWidget for Text");
        }
        this.drawable = drawable;
        return this;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        if (drawable != null) {
            drawable.draw(Pos2d.zero(), getSize(), partialTicks);
        }
    }
}
