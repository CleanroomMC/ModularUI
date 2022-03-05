package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IWidgetDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.TextRenderer;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

public class TextWidget extends Widget implements IWidgetDrawable {

    private Text[] text = {};
    private int defaultColor = TextRenderer.DEFAULT_COLOR;
    private String localised;
    private final TextRenderer textRenderer = new TextRenderer(Pos2d.ZERO, 0, 0);

    public TextWidget() {
    }

    public TextWidget(Text... text) {
        this.text = text;
    }

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
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
    }

    @Override
    public void onScreenUpdate() {
        if (isAutoSized()) {
            String l = Text.getFormatted(text);
            if (!l.equals(localised)) {
                checkNeedsRebuild();
                localised = l;
            }
        }
    }

    @Nullable
    @Override
    protected Size determineSize() {
        this.localised = Text.getFormatted(text);
        textRenderer.setUp(Pos2d.ZERO, 0, getWindow().getSize().width - getPos().x);
        Size size = textRenderer.calculateSize(localised);
        ModularUI.LOGGER.info("Determined Size {} of width {} of {}", size, getWindow().getSize().width - getPos().x, localised);
        return size;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        textRenderer.setUp(Pos2d.ZERO, defaultColor, getSize().width);
        textRenderer.draw(localised);
    }

    public TextWidget setDefaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    public TextWidget addText(Text... text) {
        this.text = ArrayUtils.addAll(this.text, text);
        return this;
    }

    public TextWidget setText(Text... text) {
        this.text = text;
        return this;
    }
}
