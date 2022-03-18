package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.drawable.TextSpan;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.google.gson.JsonObject;
import net.minecraft.util.text.ITextComponent;

public class TextWidget extends Widget {

    private final TextSpan text;
    private String localised;
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

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        text.readJson(json);
    }

    @Override
    protected Size getDefaultSize() {
        this.localised = text.getFormatted();
        int width = maxWidth > 0 ? maxWidth : getWindow().getSize().width - getPos().x;
        textRenderer.setUp(Pos2d.ZERO, 0, width);
        return textRenderer.calculateSize(localised);
    }

    @Override
    public void onScreenUpdate() {
        if (isAutoSized()) {
            String l = text.getFormatted();
            if (!l.equals(localised)) {
                checkNeedsRebuild();
                localised = l;
            }
        }
    }

    @Override
    public void drawInBackground(float partialTicks) {
        textRenderer.drawAligned(localised, 0, 0, size.width, size.height, text.getDefaultColor(), textAlignment.x, textAlignment.y);
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
}
