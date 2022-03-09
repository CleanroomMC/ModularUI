package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class TextSpan implements IDrawable {

    private Text[] texts;
    private int alignment = -1;
    private int defaultColor = TextRenderer.DEFAULT_COLOR;
    private final TextRenderer renderer = new TextRenderer(Pos2d.ZERO, 0, 0);

    public TextSpan(Text... texts) {
        this.texts = texts;
    }

    public void setText(Text... texts) {
        this.texts = texts;
    }

    public TextSpan addText(Text... texts) {
        this.texts = ArrayUtils.addAll(this.texts, texts);
        return this;
    }

    public TextSpan addText(String text) {
        this.texts = ArrayUtils.add(this.texts, new Text(text));
        return this;
    }

    public TextSpan addText(ITextComponent textComponent) {
        return addText(Text.of(textComponent));
    }

    public TextSpan alignLeft() {
        this.alignment = -1;
        return this;
    }

    public TextSpan alignCenter() {
        this.alignment = 0;
        return this;
    }

    public TextSpan alignRight() {
        this.alignment = 1;
        return this;
    }

    public TextSpan setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        renderer.drawAligned(Text.getFormatted(texts), alignment, new Pos2d(x, y), defaultColor, (int) width);
    }

    public Text[] getTexts() {
        return texts;
    }

    public String getFormatted() {
        return Text.getFormatted(texts);
    }

    public int getAlignment() {
        return alignment;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public static TextSpan ofJson(JsonElement element) {
        TextSpan text = new TextSpan();
        if (element.isJsonObject()) {
            text.readJson(element.getAsJsonObject());
        } else {
            text.addText(element.getAsString());
        }
        return text;
    }

    public void readJson(JsonObject json) {
        if (json.has("text")) {
            JsonElement element = json.get("text");
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement textJson : array) {
                    addText(Text.ofJson(textJson));
                }
            } else {
                addText(Text.ofJson(element));
            }
        }
        if (json.has("alignment")) {
            JsonElement element = json.get("alignment");
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isString()) {
                    switch (primitive.getAsString()) {
                        case "c":
                        case "center":
                            alignCenter();
                            break;
                        case "l":
                        case "left":
                            alignLeft();
                            break;
                        case "r":
                        case "right":
                            alignRight();
                            break;
                    }
                } else if (primitive.isNumber()) {
                    alignment = Math.max(-1, Math.min(1, primitive.getAsInt()));
                }
            }
        }
    }
}
