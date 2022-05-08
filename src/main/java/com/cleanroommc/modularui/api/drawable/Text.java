package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.Theme;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class Text implements IDrawable {

    public static final Text EMPTY = new Text("");

    private static final TextRenderer renderer = new TextRenderer();
    private final String text;
    private String formatting = "";
    @Nullable
    private Supplier<Object[]> localisationData;
    private int color;
    private boolean shadow = false;

    public Text(String text) {
        this.text = Objects.requireNonNull(text, "String in Text can't be null!");
        this.color = Theme.INSTANCE.getText();
    }

    public static Text localised(String key, Object... data) {
        return new Text(key).localise(data);
    }

    public static Text of(ITextComponent textComponent) {
        return new Text(textComponent.getFormattedText());
    }

    public Text color(int color) {
        this.color = Color.withAlpha(color, 255);
        return this;
    }

    public Text format(TextFormatting color) {
        this.formatting = color.toString() + this.formatting;
        return this;
    }

    public Text shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Text localise(Supplier<Object[]> localisationData) {
        this.localisationData = localisationData;
        return this;
    }

    public Text localise(Object... localisationData) {
        localise(() -> localisationData);
        return this;
    }

    public Text shadow() {
        return shadow(true);
    }

    public int getColor() {
        return color;
    }

    public boolean hasColor() {
        return Color.getAlpha(color) > 0;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public String getRawText() {
        return text;
    }

    @Override
    public void applyThemeColor(int color) {
        renderer.setColor(hasColor() ? this.color : Theme.INSTANCE.getText());
    }

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        renderer.setPos(x, y);
        renderer.setShadow(shadow);
        renderer.setAlignment(Alignment.Center, width, height);
        renderer.draw(getFormatted());
    }

    public String getFormatted() {
        String text = getRawText();
        if (localisationData != null && FMLCommonHandler.instance().getSide().isClient()) {
            text = I18n.format(text, localisationData.get()).replaceAll("\\\\n", "\n");
        }
        if (!this.formatting.isEmpty()) {
            text = formatting + text;
        }
        return text;
    }

    public static String getFormatted(Text... texts) {
        StringBuilder builder = new StringBuilder();
        for (Text text : texts) {
            builder.append(text.getFormatted());
        }
        return builder.toString();
    }

    public static Text ofJson(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            Text text = new Text(JsonHelper.getString(jsonObject, "E:404", "text"));
            text.shadow(JsonHelper.getBoolean(jsonObject, false, "shadow"));
            Integer color = JsonHelper.getElement(jsonObject, null, Color::ofJson, "color");
            if (color != null) {
                text.color(color);
            }
            if (JsonHelper.getBoolean(jsonObject, false, "localise")) {
                text.localise();
            }
            return text;
        }
        if (!json.isJsonArray()) {
            return new Text(json.getAsString());
        }
        return new Text("");
    }
}
