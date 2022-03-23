package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.google.gson.JsonObject;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TextWidget extends Widget {

    private final TextSpan text;
    protected String localised;
    private int maxWidth = -1;
    private Alignment textAlignment = Alignment.TopLeft;
    private final TextRenderer textRenderer = new TextRenderer(Pos2d.ZERO, 0, 0);

    public TextWidget() {
        this(new TextSpan());
    }

    public TextWidget(TextSpan text) {
        this.text = text;
    }

    public TextWidget(Text... texts) {
        this(new TextSpan().addText(texts));
    }

    public TextWidget(String text) {
        this(new TextSpan().addText(text));
    }

    public TextWidget(ITextComponent text) {
        this(new TextSpan().addText(text));
    }

    public static DynamicTextWidget dynamicSpan(Supplier<TextSpan> supplier) {
        return new DynamicTextWidget(supplier);
    }

    public static DynamicTextWidget dynamicTexts(Supplier<Text[]> supplier) {
        return new DynamicTextWidget(() -> new TextSpan(supplier.get()));
    }

    public static DynamicTextWidget dynamicText(Supplier<Text> supplier) {
        return new DynamicTextWidget(() -> new TextSpan(supplier.get()));
    }

    public static DynamicTextWidget dynamicString(Supplier<String> supplier) {
        return new DynamicTextWidget(() -> new TextSpan().addText(supplier.get()));
    }

    public static DynamicTextWidget dynamicTextComponent(Supplier<ITextComponent> supplier) {
        return new DynamicTextWidget(() -> new TextSpan().addText(supplier.get()));
    }

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        getText().readJson(json);
    }

    @Override
    public void onRebuild() {
        if (localised == null) {
            this.localised = getText().getFormatted();
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        this.localised = getText().getFormatted();
        int width = this.maxWidth > 0 ? this.maxWidth : maxWidth - getPos().x;
        textRenderer.setUp(Pos2d.ZERO, 0, width);
        textRenderer.setDoDraw(false);
        textRenderer.draw(localised);
        textRenderer.setDoDraw(true);
        return new Size(textRenderer.getWidth() + 1, textRenderer.getHeight() + 1);
    }

    @Override
    public void onScreenUpdate() {
        if (isAutoSized()) {
            String l = getText().getFormatted();
            if (!l.equals(localised)) {
                checkNeedsRebuild();
                localised = l;
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        textRenderer.drawAligned(localised, 0, 0, size.width, size.height, text.getDefaultColor(), textAlignment.x, textAlignment.y);
    }

    public TextSpan getText() {
        return text;
    }

    public TextWidget setDefaultColor(int color) {
        this.text.setDefaultColor(color);
        return this;
    }

    public TextWidget addText(Text... text) {
        this.text.addText(text);
        return this;
    }

    public TextWidget setText(Text... text) {
        this.text.setText(text);
        return this;
    }

    public TextWidget setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public TextWidget setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public TextWidget setScale(float scale) {
        this.textRenderer.setScale(scale);
        return this;
    }
}
