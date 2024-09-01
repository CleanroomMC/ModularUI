package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.drawable.keys.StringKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class TextWidget extends Widget<TextWidget> {

    private final IKey key;
    private Alignment alignment = Alignment.CenterLeft;
    private int color = 0x404040;
    private boolean shadow = false;
    private float scale = 1f;

    public boolean colorChanged = false, shadowChanged = false;

    public TextWidget(IKey key) {
        this.key = key;
    }

    public TextWidget(String key) {
         this(new StringKey(key));
    } 

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setColor(this.colorChanged ? this.color : widgetTheme.getTextColor());
        renderer.setAlignment(this.alignment, getArea().w() + this.scale, getArea().h());
        renderer.setShadow(this.shadowChanged ? this.shadow : widgetTheme.getTextShadow());
        renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
        renderer.setScale(this.scale);
        renderer.setSimulate(false);
        renderer.draw(this.key.get());
    }

    private TextRenderer simulate(float maxWidth) {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left, padding.top);
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(this.key.get());
        return renderer;
    }

    @Override
    public int getDefaultHeight() {
        float maxWidth;
        if (resizer() != null && resizer().isWidthCalculated()) {
            maxWidth = getArea().width + this.scale;
        } else if (getParent().resizer() != null && getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width + this.scale;
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        Box padding = getArea().getPadding();
        return (int) (renderer.getLastHeight() + padding.vertical() + 0.5f);
    }

    @Override
    public int getDefaultWidth() {
        float maxWidth = getScreen().getScreenArea().width;
        if (getParent().resizer() != null && getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        Box padding = getArea().getPadding();
        return (int) (renderer.getLastWidth() + padding.horizontal() + 0.5f);
    }

    public IKey getKey() {
        return this.key;
    }

    public Alignment getAlignment() {
        return this.alignment;
    }

    public float getScale() {
        return this.scale;
    }

    public int getColor() {
        return this.color;
    }

    public boolean isShadow() {
        return this.shadow;
    }

    public TextWidget alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public TextWidget color(int color) {
        this.colorChanged = true;
        this.color = color;
        return this;
    }

    public TextWidget scale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextWidget shadow(boolean shadow) {
        this.shadowChanged = true;
        this.shadow = shadow;
        return this;
    }
}
