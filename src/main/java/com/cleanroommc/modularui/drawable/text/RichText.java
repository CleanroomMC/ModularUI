package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.*;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;

import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class RichText implements IDrawable, IRichTextBuilder<RichText> {

    private static final TextRenderer renderer = new TextRenderer();

    private final List<Object> elements = new ArrayList<>();
    private Alignment alignment = Alignment.CenterLeft;
    private float scale = 1f;
    private Integer color = null;
    private Boolean shadow = null;

    private List<ITextLine> cachedText;

    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    public List<String> getStringRepresentation() {
        List<String> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (Object o : this.elements) {
            if (o == IKey.LINE_FEED) {
                list.add(builder.toString());
                builder.delete(0, builder.length());
                continue;
            }
            String s = null;
            if (o instanceof IKey key) {
                s = key.get();
            } else if (o instanceof String s1) {
                s = s1;
            } else if (o instanceof TextIcon ti) {
                s = ti.getText();
            }
            if (s != null) {
                for (String part : s.split("\n")) {
                    builder.append(part);
                    list.add(builder.toString());
                    builder.delete(0, builder.length());
                }
            }
        }
        if (!list.isEmpty() && list.get(list.size() - 1).isEmpty()) {
            list.remove(list.size() - 1);
        }
        return list;
    }

    public int getMinWidth() {
        int minWidth = 12;
        for (Object o : this.elements) {
            if (o instanceof IIcon icon) {
                minWidth = Math.max(minWidth, icon.getWidth());
            }
        }
        return minWidth;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Boolean getShadow() {
        return shadow;
    }

    public Integer getColor() {
        return color;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public RichText getThis() {
        return this;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return this;
    }

    public RichText add(String s) {
        this.elements.add(s);
        return this;
    }

    @Override
    public RichText add(IDrawable drawable) {
        Object o = drawable;
        if (!(o instanceof IKey) && !(o instanceof IIcon)) o = drawable.asIcon();
        this.elements.add(o);
        return this;
    }

    @Override
    public RichText addLine(ITextLine line) {
        this.elements.add(line);
        return this;
    }

    @Override
    public RichText clearText() {
        this.elements.clear();
        return this;
    }

    @Override
    public RichText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public RichText textColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public RichText scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public RichText textShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public RichText insertTitleMargin(int margin) {
        List<Object> objects = this.elements;
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (o == IKey.LINE_FEED) {
                if (i == objects.size() - 1) return this;
                if (objects.get(i + 1) instanceof Spacer spacer) {
                    if (spacer.getSpace() == margin) return this;
                    objects.set(i + 1, Spacer.of(margin));
                } else {
                    objects.add(i + 1, Spacer.of(margin));
                }
                return this;
            }
        }
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        draw(context, x, y, width, height, widgetTheme.getTextColor(), widgetTheme.getTextShadow());
    }

    public void draw(GuiContext context, int x, int y, int width, int height, int color, boolean shadow) {
        draw(renderer, context, x, y, width, height, color, shadow);
    }

    public void draw(TextRenderer renderer, GuiContext context, int x, int y, int width, int height, int color, boolean shadow) {
        renderer.setSimulate(false);
        setupRenderer(renderer, x, y, width, height, color, shadow);
        this.cachedText = renderer.compileAndDraw(context, this.elements);
    }

    public int getLastHeight() {
        return (int) renderer.getLastHeight();
    }

    public int getLastWidth() {
        return (int) renderer.getLastWidth();
    }

    public void setupRenderer(TextRenderer renderer, int x, int y, float width, float height, int color, boolean shadow) {
        renderer.setPos(x, y);
        renderer.setScale(this.scale);
        renderer.setColor(this.color != null ? this.color : color);
        renderer.setShadow(this.shadow != null ? this.shadow : shadow);
        renderer.setAlignment(this.alignment, width, height);
    }

    public List<ITextLine> compileAndDraw(TextRenderer renderer, GuiContext context, boolean simulate) {
        renderer.setSimulate(simulate);
        this.cachedText = renderer.compileAndDraw(context, this.elements);
        renderer.setSimulate(false);
        return this.cachedText;
    }

    public Object getHoveringElement(GuiContext context) {
        return getHoveringElement(context.getFontRenderer(), context.getMouseX(), context.getMouseY());
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
