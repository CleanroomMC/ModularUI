package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.FloatSupplier;
import com.cleanroommc.modularui.widgets.TextWidget;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class StyledText<K extends StyledText<K>> extends BaseKey<K> {

    private final IKey key;
    @NotNull
    private Supplier<@NotNull Alignment> alignment = () -> Alignment.Center;
    @Nullable
    private IntSupplier color = null;
    @Nullable
    private BooleanSupplier shadow = null;
    @Nullable
    private FloatSupplier scale = null;

    public StyledText(IKey key) {
        this.key = key;
    }

    @Override
    public String get() {
        return this.key.get();
    }

    @Override
    public String getFormatted() {
        return this.key.getFormatted();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        renderer.setAlignment(getAlignment(), width, height);
        renderer.setColor(this.color != null ? this.color.getAsInt() : widgetTheme.getTextColor());
        renderer.setScale(getScale());
        renderer.setPos(x, y);
        renderer.setShadow(this.shadow != null ? this.shadow.getAsBoolean() : widgetTheme.getTextShadow());
        renderer.draw(getFormatted());
    }

    public @NotNull Alignment getAlignment() {
        return this.alignment.get();
    }

    public @Nullable IntSupplier getColor() {
        return this.color;
    }

    @Override
    public float getScale() {
        return this.scale == null ? super.getScale() : this.scale.getAsFloat();
    }

    public @Nullable Boolean isShadow() {
        return this.shadow == null ? null : this.shadow.getAsBoolean();
    }

    @Override
    public K style(TextFormatting formatting) {
        this.key.style(formatting);
        return getThis();
    }

    @Override
    public K alignment(@Nullable Alignment alignment) {
        return alignment(alignment == null ? null : () -> alignment);
    }

    @Override
    public K alignment(@Nullable Supplier<@NotNull Alignment> alignment) {
        this.alignment = alignment == null ? () -> Alignment.Center : alignment;
        return getThis();
    }

    @Override
    public K color(int color) {
        return color(() -> color);
    }

    @Override
    public K color(@Nullable IntSupplier color) {
        this.color = color;
        return getThis();
    }

    @Override
    public K scale(float scale) {
        return scale(() -> scale);
    }

    public K scale(@Nullable FloatSupplier scale) {
        this.scale = scale;
        return getThis();
    }

    @Override
    public K shadow(@Nullable Boolean shadow) {
        if (shadow == null) {
            return shadow((BooleanSupplier) null);
        } else {
            boolean hasShadow = shadow;
            return shadow(() -> hasShadow);
        }
    }

    @Override
    public K shadow(@Nullable BooleanSupplier shadow) {
        this.shadow = shadow;
        return getThis();
    }

    @Override
    public TextWidget<?> asWidget() {
        return new TextWidget<>(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }

    @Override
    public AnimatedText withAnimation() {
        return new AnimatedText(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }
}
