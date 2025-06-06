package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Box;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class TextWidget extends Widget<TextWidget> {

    private final IKey key;
    private Alignment alignment = Alignment.CenterLeft;
    private IntSupplier color = null;
    private Boolean shadow = null;
    private float scale = 1f;

    private String lastText = null;
    private String textForDefaultSize = null;

    public TextWidget(IKey key) {
        this.key = key;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        TextRenderer renderer = TextRenderer.SHARED;
        String text = this.key.getFormatted();
        if (this.lastText != null && !this.lastText.equals(text)) {
            // scheduling it would resize it on next frame, but we need it now
            WidgetTree.resizeInternal(this, false);
        }
        this.lastText = text;
        renderer.setColor(this.color != null ? this.color.getAsInt() : widgetTheme.getTextColor());
        renderer.setAlignment(this.alignment, getArea().w() + this.scale, getArea().h());
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.getTextShadow());
        renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
        renderer.setScale(this.scale);
        renderer.setSimulate(false);
        renderer.draw(this.key.getFormatted());
    }

    private TextRenderer simulate(float maxWidth) {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left, padding.top);
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        renderer.draw(getTextForDefaultSize());
        renderer.setSimulate(false);
        return renderer;
    }

    @Override
    public int getDefaultHeight() {
        float maxWidth;
        if (resizer().isWidthCalculated()) {
            maxWidth = getArea().width + this.scale;
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width + this.scale;
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetHeight(renderer.getLastActualHeight());
    }

    @Override
    public int getDefaultWidth() {
        float maxWidth = getScreen().getScreenArea().width;
        if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetWidth(renderer.getLastActualWidth());
    }

    protected int getWidgetWidth(float actualTextWidth) {
        Box padding = getArea().getPadding();
        return Math.max(1, (int) Math.ceil(actualTextWidth + padding.horizontal()));
    }

    protected int getWidgetHeight(float actualTextHeight) {
        Box padding = getArea().getPadding();
        return Math.max(1, (int) Math.ceil(actualTextHeight + padding.vertical()));
    }

    /**
     * Makes sure the used text for {@link #getDefaultWidth()} and {@link #getDefaultHeight()} is always the same.
     * Also sets the last rendered text.
     */
    protected String getTextForDefaultSize() {
        if (this.textForDefaultSize == null) {
            this.textForDefaultSize = this.key.getFormatted();
            this.lastText = this.textForDefaultSize;
        }
        return this.textForDefaultSize;
    }

    @Override
    public void postResize() {
        this.textForDefaultSize = null;
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

    public @Nullable IntSupplier getColor() {
        return this.color;
    }

    public @Nullable Boolean isShadow() {
        return this.shadow;
    }

    public TextWidget alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public TextWidget color(int color) {
        return color(() -> color);
    }

    public TextWidget color(@Nullable IntSupplier color) {
        this.color = color;
        return this;
    }

    public TextWidget scale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextWidget shadow(@Nullable Boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public TextWidget style(TextFormatting formatting) {
        this.key.style(formatting);
        return this;
    }
}
