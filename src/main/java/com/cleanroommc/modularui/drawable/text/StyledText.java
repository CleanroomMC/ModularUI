package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.TextWidget;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public class StyledText extends BaseKey {

    private final IKey key;
    private Alignment alignment = Alignment.Center;
    private IntSupplier color = null;
    private Boolean shadow = null;
    private float scale = 1f;

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
        renderer.setAlignment(this.alignment, width, height);
        renderer.setColor(this.color != null ? this.color.getAsInt() : widgetTheme.getColor());
        renderer.setScale(this.scale);
        renderer.setPos(x, y);
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.getTextShadow());
        renderer.draw(getFormatted());
    }

    public Alignment getAlignment() {
        return this.alignment;
    }

    public @Nullable IntSupplier getColor() {
        return this.color;
    }

    public float getScale() {
        return this.scale;
    }

    public @Nullable Boolean isShadow() {
        return this.shadow;
    }

    @Override
    public BaseKey style(TextFormatting formatting) {
        this.key.style(formatting);
        return this;
    }

    @Override
    public StyledText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public StyledText color(int color) {
        return color(() -> color);
    }

    @Override
    public StyledText color(@Nullable IntSupplier color) {
        this.color = color;
        return this;
    }

    @Override
    public StyledText scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public StyledText shadow(@Nullable Boolean shadow) {
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

    @Override
    public AnimatedText withAnimation() {
        return new AnimatedText(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }
}
