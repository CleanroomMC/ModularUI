package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;

import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class RichText implements IDrawable {

    private static final TextRenderer renderer = new TextRenderer();

    private final List<Object> elements = new ArrayList<>();
    private Alignment alignment = Alignment.CenterLeft;
    private float scale = 1f;
    private Integer color = null;
    private Boolean shadow = null;

    private List<ITextLine> cachedText;

    public RichText add(String s) {
        this.elements.add(s);
        return this;
    }

    public RichText add(IDrawable drawable) {
        this.elements.add(drawable);
        return this;
    }

    public RichText addLine(ITextLine line) {
        this.elements.add(line);
        return this;
    }

    public RichText newLine() {
        return add(IKey.LINE_FEED);
    }

    public RichText space() {
        return add(IKey.SPACE);
    }

    public RichText spaceLine(int pixelSpace) {
        return addLine(new Spacer(pixelSpace));
    }

    public RichText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public RichText color(int color) {
        this.color = color;
        return this;
    }

    public RichText scale(float scale) {
        this.scale = scale;
        return this;
    }

    public RichText shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        renderer.setSimulate(false);
        renderer.setPos(x, y);
        renderer.setScale(this.scale);
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.getTextShadow());
        renderer.setColor(this.color != null ? this.color : widgetTheme.getTextColor());
        renderer.setAlignment(this.alignment, width, height);
        this.cachedText = renderer.compileAndDraw(context, this.elements);

        /*int mx = context.unTransformX(context.getAbsMouseX(), context.getAbsMouseY());
        int my = context.unTransformY(context.getAbsMouseX(), context.getAbsMouseY());
        Object hovering = getHoveringElement(TextRenderer.getFontRenderer(), mx, my);
        if (hovering != null) ModularUI.LOGGER.info(hovering);*/
    }

    public Object getHoveringElement(FontRenderer fr, int x, int y) {
        if (this.cachedText == null) return null;

        for (ITextLine line : this.cachedText) {
            Object o = line.getHoveringElement(fr, x, y);
            if (o == null) continue;
            if (o == Boolean.FALSE) return null;
            return o;
        }
        return null;
    }
}
