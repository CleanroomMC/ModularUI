package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.FloatSupplier;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Box;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TextWidget<W extends TextWidget<W>> extends Widget<W> {

    private final IKey key;
    @NotNull
    private Supplier<@NotNull Alignment> alignment = () -> Alignment.Center;
    @Nullable
    private IntSupplier color = null;
    @Nullable
    private BooleanSupplier shadow = null;
    @Nullable
    private FloatSupplier scale = null;
    private int maxWidth = -1;

    private String lastText = null;
    private String textForDefaultSize = null;

    public TextWidget(IKey key) {
        this.key = key;
    }

    public TextWidget(String key) {
        this(IKey.str(key));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        TextRenderer renderer = TextRenderer.SHARED;
        String text = checkString();
        WidgetTheme theme = getActiveWidgetTheme(widgetTheme, isHovering());
        renderer.setColor(this.color != null ? this.color.getAsInt() : theme.getTextColor());
        renderer.setAlignment(getAlignment(), getArea().paddedWidth() + getScale(), getArea().paddedHeight());
        renderer.setShadow(this.shadow != null ? this.shadow.getAsBoolean() : theme.getTextShadow());
        renderer.setPos(getArea().getPadding().getLeft(), getArea().getPadding().getTop());
        renderer.setScale(getScale());
        renderer.setSimulate(false);
        renderer.draw(text);
    }

    protected String checkString() {
        String text = this.key.getFormatted();
        if (!Objects.equals(this.lastText, text)) {
            onTextChanged(text);
            this.lastText = text;
        }
        return text;
    }

    protected void onTextChanged(String newText) {
        // scheduling it would resize it on next frame, but we need it now
        WidgetTree.resizeInternal(this, false);
    }

    private TextRenderer simulate(float maxWidth) {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.getLeft(), padding.getTop());
        renderer.setScale(getScale());
        renderer.setSimulate(true);
        renderer.draw(getTextForDefaultSize());
        renderer.setSimulate(false);
        return renderer;
    }

    @Override
    public int getDefaultHeight() {
        float maxWidth;
        if (resizer().isWidthCalculated()) {
            maxWidth = getArea().width + getScale();
        } else if (this.maxWidth > 0) {
            maxWidth = Math.max(this.maxWidth, 5);
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width + getScale();
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetHeight(renderer.getLastActualHeight());
    }

    @Override
    public int getDefaultWidth() {
        float maxWidth;
        if (this.maxWidth > 0) {
            maxWidth = Math.max(this.maxWidth, 5);
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width;
        } else {
            maxWidth = getScreen().getScreenArea().width;
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

    @Override
    public boolean canHoverThrough() {
        return true;
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

    public @NotNull Alignment getAlignment() {
        return this.alignment.get();
    }

    public @Nullable FloatSupplier getScaleSupplier() {
        return this.scale;
    }

    public float getScale() {
        return this.scale == null ? 1.0f : this.scale.getAsFloat();
    }

    public @Nullable IntSupplier getColor() {
        return this.color;
    }

    public @Nullable Boolean isShadow() {
        return this.shadow == null ? null : this.shadow.getAsBoolean();
    }

    public W alignment(@Nullable Alignment alignment) {
        return alignment(alignment == null ? null : () -> alignment);
    }

    public W alignment(@Nullable Supplier<@NotNull Alignment> alignment) {
        this.alignment = alignment == null ? () -> Alignment.Center : alignment;
        return getThis();
    }

    public W color(int color) {
        return color(() -> color);
    }

    public W color(@Nullable IntSupplier color) {
        this.color = color;
        return getThis();
    }

    public W scale(float scale) {
        return scale(() -> scale);
    }

    public W scale(@Nullable FloatSupplier scale) {
        this.scale = scale;
        return getThis();
    }

    public W shadow(@Nullable Boolean shadow) {
        if (shadow == null) {
            return shadow((BooleanSupplier) null);
        } else {
            boolean hasShadow = shadow;
            return shadow(() -> hasShadow);
        }
    }

    public W shadow(@Nullable BooleanSupplier shadow) {
        this.shadow = shadow;
        return getThis();
    }

    public W style(TextFormatting formatting) {
        this.key.style(formatting);
        return getThis();
    }

    public W maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return getThis();
    }
}
