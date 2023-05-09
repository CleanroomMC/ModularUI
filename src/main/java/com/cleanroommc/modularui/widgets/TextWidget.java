package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.drawable.keys.StringKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.Theme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Box;
import com.cleanroommc.modularui.widget.sizer.Flex;

public class TextWidget extends Widget<TextWidget> {

    private final IKey key;
    private Alignment alignment = Alignment.CenterLeft;
    private int color = 0x404040;
    private boolean shadow = false;
    private float scale = 1f;

    public boolean colorChanged = false, shadowChanged = false;
    private String widgetTheme = Theme.FALLBACK;

    public TextWidget(IKey key) {
        this.key = key;
    }

    public TextWidget(String key) {
         this(new StringKey(key));
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
    public void applyTheme(ITheme theme) {
        if (!colorChanged) {
            this.color = getWidgetTheme(theme).getTextColor();
        }
        if (!shadowChanged) {
            this.shadow = getWidgetTheme(theme).getTextShadow();
        }
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getWidgetTheme(this.widgetTheme);
    }

    @Override
    public int getDefaultHeight() {
        Flex parentFlex = getParent().getFlex();
        float maxWidth = parentFlex != null && !parentFlex.xAxisDependsOnChildren() ? getParent().getArea().width : Float.MAX_VALUE;
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left, padding.top);
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(this.key.get());
        return (int) (renderer.getLastHeight() + padding.vertical() + 0.5f);
    }

    @Override
    public int getDefaultWidth() {
        Flex parentFlex = getParent().getFlex();
        float maxWidth = parentFlex != null && !parentFlex.xAxisDependsOnChildren() ? getParent().getArea().width : Float.MAX_VALUE;
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left, padding.top);
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(this.key.get());
        return (int) (renderer.getLastWidth() + padding.horizontal() + 0.5f);
    }

    public IKey getKey() {
        return key;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public float getScale() {
        return scale;
    }

    public int getColor() {
        return color;
    }

    public boolean isShadow() {
        return shadow;
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

    public TextWidget widgetTheme(String widgetTheme) {
        this.widgetTheme = widgetTheme;
        return this;
    }
}
