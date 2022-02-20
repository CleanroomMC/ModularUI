package io.github.cleanroommc.modularui.widget;

import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.api.math.Size;
import io.github.cleanroommc.modularui.drawable.IDrawable;

public class DrawableWidget extends Widget implements IWidgetDrawable {

    private IDrawable drawable;

    public DrawableWidget setDrawable(IDrawable drawable) {
        this.drawable = drawable;
        if (drawable != null) {
            Size size = drawable.estimateSize();
            if (size != null) {
                setSize(size);
            }
        }
        return this;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        if (drawable != null) {
            drawable.draw(Pos2d.zero(), getSize(), partialTicks);
        }
    }
}
