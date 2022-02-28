package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.drawable.Text;
import com.cleanroommc.modularui.common.drawable.TextRenderer;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class TextWidget extends Widget implements IWidgetDrawable {

    private String text;
    @Nullable
    private Supplier<String> textSupplier;
    @Nullable
    private Supplier<Object[]> localisationData;
    private int defaultColor = TextRenderer.DEFAULT_COLOR;
    private String localised;
    private boolean dynamicData = false;

    public TextWidget() {
    }

    public TextWidget(String text) {
        this.text = text;
    }

    public TextWidget(Text text) {
        this.text = text.getFormatted();
        this.defaultColor = text.getColor();
    }

    public TextWidget(Text... texts) {
        this.text = Text.getFormatted(texts);
    }

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        if (json.has("text")) {
            text = json.get("text").getAsString();
        }
        if (JsonHelper.getBoolean(json, false, "localize")) {
            localise();
        }
    }

    public static TextWidget dynamicText(Supplier<Text> textSupplier) {
        TextWidget textWidget = new TextWidget("");
        textWidget.textSupplier = () -> textSupplier.get().getFormatted();
        return textWidget;
    }

    public static TextWidget dynamic(Supplier<Text[]> textSupplier) {
        TextWidget textWidget = new TextWidget("");
        textWidget.textSupplier = () -> Text.getFormatted(textSupplier.get());
        return textWidget;
    }

    public TextWidget localise(Supplier<Object[]> localisationData) {
        this.localisationData = localisationData;
        this.dynamicData = true;
        return this;
    }

    public TextWidget localise(Object... localisationData) {
        localise(() -> localisationData);
        this.dynamicData = false;
        return this;
    }

    public TextWidget setDefaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    @Override
    public void onInit() {
        localised = getLocalised();
    }

    @Override
    public void onScreenUpdate() {
        if (isDynamic()) {
            String l = getLocalised();
            if (!l.equals(localised)) {
                checkNeedsRebuild();
                localised = l;
            }
        }
    }

    @Nullable
    @Override
    protected Size determineSize() {
        if (localised == null) {
            localised = getLocalised();
        }
        return TextRenderer.calcTextSize(localised, getWindow().getSize().width, 1);
    }

    public String getLocalised() {
        String text = textSupplier == null ? this.text : textSupplier.get();
        if (isClient() && localisationData != null) {
            return I18n.format(text, localisationData.get());
        }
        return text;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        TextRenderer.drawString(localised, Pos2d.ZERO, defaultColor, getSize().width);
    }

    public boolean isDynamic() {
        return dynamicData || textSupplier != null;
    }
}
