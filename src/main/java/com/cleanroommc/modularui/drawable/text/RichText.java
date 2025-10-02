package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.TooltipLines;

import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RichText implements IDrawable, IRichTextBuilder<RichText> {

    private static final TextRenderer renderer = new TextRenderer();

    private final List<Object> elements = new ArrayList<>();
    private TooltipLines stringList;
    private Alignment alignment = Alignment.CenterLeft;
    private float scale = 1f;
    private Integer color = null;
    private Boolean shadow = null;

    private int cursor = 0;
    private boolean cursorLocked = false;
    private List<ITextLine> cachedText;

    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    public List<String> getAsStrings() {
        if (this.stringList == null) {
            this.stringList = new TooltipLines(this.elements);
        }
        return this.stringList;
    }

    private void clearStrings() {
        if (this.stringList != null) {
            this.stringList.clearCache();
        }
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

    private void addElement(Object o) {
        this.elements.add(this.cursor, o);
        if (!this.cursorLocked) {
            this.cursor++;
        }
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
        addElement(s);
        clearStrings();
        return this;
    }

    @Override
    public RichText add(IDrawable drawable) {
        Object o = drawable;
        if (!(o instanceof IKey) && !(o instanceof IIcon)) o = drawable.asIcon();
        addElement(o);
        clearStrings();
        return this;
    }

    @Override
    public RichText addLine(ITextLine line) {
        addElement(line);
        clearStrings();
        return this;
    }

    @Override
    public RichText clearText() {
        this.elements.clear();
        this.cursor = 0;
        clearStrings();
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

    @Override
    public RichText moveCursorAfterElement(Pattern regex) {
        int i = findNextText(this.cursor, true, s -> regex.matcher(s).find());
        if (i < 0) i = this.elements.size();
        this.cursor = i;
        return this;
    }

    @Override
    public RichText replace(Pattern regex, UnaryOperator<IKey> function) {
        int i = findNextText(this.cursor, true, s -> regex.matcher(s).find());
        if (i >= 0) {
            this.cursor = i;
            Object o = this.elements.get(i);
            IKey key = o instanceof IKey key1 ? key1 : IKey.str((String) o);
            key = function.apply(key);
            if (key == null) {
                this.elements.remove(i);
                this.cursor--;
            } else {
                this.elements.set(i, key);
            }
        }
        return this;
    }

    @Override
    public RichText moveCursorToStart() {
        this.cursor = 0;
        return this;
    }

    @Override
    public RichText moveCursorToEnd() {
        this.cursor = this.elements.size() - 1;
        return this;
    }

    @Override
    public RichText moveCursorForward(int by) {
        this.cursor = Math.min(this.cursor + by, this.elements.size() - 1);
        return this;
    }

    @Override
    public RichText moveCursorBackward(int by) {
        this.cursor = Math.max(0, this.cursor - by);
        return this;
    }

    @Override
    public RichText lockCursor() {
        this.cursorLocked = true;
        return this;
    }

    @Override
    public RichText unlockCursor() {
        this.cursorLocked = false;
        return this;
    }

    @Override
    public RichText moveCursorToNextLine() {
        if (this.cursor < this.elements.size() - 1) {
            this.cursor = findNextLine(this.cursor) + 1;
        }
        return this;
    }

    private int findNextLine(int current) {
        for (int i = current; i < this.elements.size(); i++) {
            Object o = this.elements.get(i);
            if (o == IKey.LINE_FEED) return i;
            if (o instanceof IKey key && key.get().trim().endsWith("\n")) return i;
            if (o instanceof String string && string.trim().endsWith("\n")) return i;
            if (o instanceof ITextLine) return i;
        }
        return this.elements.size() - 1;
    }

    private int findNextText(int current, boolean wrapAround, Predicate<String> test) {
        int i = current;
        int lim = this.elements.size();
        while (i < lim) {
            Object o = this.elements.get(i);
            if (o instanceof IKey key && test.test(key.get())) return i;
            if (o instanceof String string && test.test(string)) return i;
            if (++i == lim && wrapAround) {
                i = 0;
                lim = current;
                wrapAround = false;
            }
        }
        return -1;
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
                clearStrings();
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
        return (int) renderer.getLastTrimmedHeight();
    }

    public int getLastWidth() {
        return (int) renderer.getLastTrimmedWidth();
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

    /**
     * Returns the currently hovered element of this rich text or {@code null} if none is hovered.
     * Note that this method assumes, that the {@link com.cleanroommc.modularui.api.layout.IViewportStack IViewportStack}
     * is transformed to 0,0 of this {@link IDrawable}.
     *
     * @param context the viewport stack with transformation to this widget
     * @return hovered element or null
     */
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

    public RichText copy() {
        RichText copy = new RichText();
        copy.elements.addAll(this.elements);
        copy.cursor = this.cursor;
        copy.alignment = this.alignment;
        copy.scale = this.scale;
        copy.color = this.color;
        copy.shadow = this.shadow;
        return copy;
    }
}
