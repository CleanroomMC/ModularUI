package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.Theme;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TextWidget extends Widget {

    private final Text text;
    protected String localised;
    private int maxWidth = -1;
    private Alignment textAlignment = Alignment.TopLeft;
    private final TextRenderer textRenderer = new TextRenderer();

    public TextWidget() {
        this(new Text(""));
    }

    public TextWidget(Text text) {
        this.text = text;
    }

    public TextWidget(String text) {
        this(new Text(text));
    }

    /*public TextWidget(ITextComponent text) {
        this(new TextSpan().addText(text));
    }*/

    public static DynamicTextWidget dynamicText(Supplier<Text> supplier) {
        return new DynamicTextWidget(supplier);
    }

    public static DynamicTextWidget dynamicString(Supplier<String> supplier) {
        return new DynamicTextWidget(() -> new Text(supplier.get()));
    }

    /*public static DynamicTextWidget dynamicTextComponent(Supplier<ITextComponent> supplier) {
        return new DynamicTextWidget(() -> new TextSpan().addText(supplier.get()));
    }*/

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        //getText().readJson(json);
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
        textRenderer.setSimulate(true);
        textRenderer.setAlignment(textAlignment, width, maxHeight);
        textRenderer.draw(localised);
        textRenderer.setSimulate(false);
        return textRenderer.getLastSize();
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
        if (localised == null) {
            localised = text.getFormatted();
        }
        textRenderer.setAlignment(textAlignment, size.width, size.height);
        textRenderer.setColor(text.hasColor() ? text.getColor() : Theme.INSTANCE.getText());
        textRenderer.draw(localised);
    }

    public Text getText() {
        return text;
    }

    public TextWidget setDefaultColor(int color) {
        this.text.color(color);
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
