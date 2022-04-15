package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.widget.Widget;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class DrawableWidget extends Widget {

    @Nullable
    private IDrawable drawable = IDrawable.EMPTY;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        if (type.equals("image")) {
            setDrawable(UITexture.ofJson(json));
        }
    }

    @Override
    public void onScreenUpdate() {
        if (drawable != null) {
            drawable.tick();
        }
    }

    @Override
    public void draw(float partialTicks) {
        if (drawable != null) {
            drawable.draw(Pos2d.ZERO, getSize(), partialTicks);
        }
    }

    @Nullable
    public IDrawable getDrawable() {
        return drawable;
    }

    public DrawableWidget setDrawable(IDrawable drawable) {
        if (drawable instanceof Text || drawable instanceof TextSpan) {
            ModularUI.LOGGER.warn("Please use TextWidget for Text");
        }
        this.drawable = drawable;
        return this;
    }
}
