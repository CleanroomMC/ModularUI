package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;
import java.util.function.Supplier;

public class Text implements IDrawable {

    @SideOnly(Side.CLIENT)
    public static final FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
    private final Supplier<String> textSupplier;
    private final String text;
    private int color = 0x212121;
    private boolean shadow = false;

    private Text(String text, Supplier<String> textSupplier) {
        this.text = text;
        this.textSupplier = textSupplier;
    }

    public static Text dynamic(Supplier<String> textSupplier) {
        return new Text(null, Objects.requireNonNull(textSupplier, "TextSupplier can't be null!"));
    }

    public Text(String text) {
        this(Objects.requireNonNull(text, "String in Text can't be null!"), null);
    }

    public Text color(int color) {
        this.color = color;
        return this;
    }

    public Text shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Text shadow() {
        return shadow(true);
    }

    public int getColor() {
        return color;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        String text = TextRenderer.getColorFormatString(color) + getText();
        TextRenderer.drawString(text, pos, color, size.width);
    }

    public String getFormatted() {
        String text = TextRenderer.getColorFormatString(color);
        if (hasShadow()) {
            text += TextRenderer.FORMAT_CHAR + 's';
        }
        return text + getText();
    }

    public static String getFormatted(Text... texts) {
        StringBuilder builder = new StringBuilder();
        for (Text text : texts) {
            builder.append(text.getFormatted());
        }
        return builder.toString();
    }
}
