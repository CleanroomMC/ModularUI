package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class TextWidget extends Widget<TextWidget> {

    private final IKey key;
    private Alignment alignment = Alignment.CenterLeft;
    private int color = 0x404040;
    private boolean shadow = false;
    private float scale = 1f;

    public TextWidget(IKey key) {
        this.key = key;
    }

    @Override
    public void draw(GuiContext context) {
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setColor(this.color);
        renderer.setAlignment(this.alignment, getArea().w() + 1, getArea().h());
        renderer.setShadow(this.shadow);
        renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
        renderer.setScale(this.scale);
        renderer.setSimulate(false);
        renderer.draw(this.key.get());
    }

    @Override
    public int getDefaultHeight() {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, Integer.MAX_VALUE);
        renderer.setPos(padding.left, padding.top);
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(this.key.get());
        return (int) (renderer.getLastHeight() + padding.vertical() + 0.5f);
    }

    @Override
    public int getDefaultWidth() {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, Integer.MAX_VALUE);
        renderer.setPos(padding.left, padding.top);
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(this.key.get());
        return (int) (renderer.getLastWidth() + padding.horizontal() + 0.5f);
    }

    public TextWidget alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public TextWidget color(int color) {
        this.color = color;
        return this;
    }

    public TextWidget scale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextWidget shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }
}
