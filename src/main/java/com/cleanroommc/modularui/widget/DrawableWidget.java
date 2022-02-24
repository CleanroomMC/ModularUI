package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Text;
import com.cleanroommc.modularui.drawable.TextSpan;

public class DrawableWidget extends Widget implements IWidgetDrawable {

    private IDrawable drawable;

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
