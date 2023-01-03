package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.TextWidget;

public class StyledText implements IKey, IDrawable {

    private final IKey key;
    private Alignment alignment = Alignment.TopLeft;
    private int color = 0x404040;
    private boolean shadow = false;
    private float scale = 1f;

    public StyledText(IKey key) {
        this.key = key;
    }

    @Override
    public String get() {
        return this.key.get();
    }

    @Override
    public void set(String string) {
    }

    @Override
    public void draw(int x, int y, int width, int height) {
        renderer.setAlignment(this.alignment, width, height);
        renderer.setColor(this.color);
        renderer.setScale(this.scale);
        renderer.setPos(x, y);
        renderer.setShadow(this.shadow);
        renderer.draw(get());
    }

    @Override
    public StyledText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public StyledText color(int color) {
        this.color = color;
        return this;
    }

    @Override
    public StyledText scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public StyledText shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    public TextWidget asWidget() {
        return new TextWidget(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }
}
