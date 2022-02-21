package io.github.cleanroommc.modularui.drawable;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Text implements IDrawable {

    @SideOnly(Side.CLIENT)
    public static final FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
    private final Supplier<String> textSupplier;
    private final String text;
    private Supplier<Object[]> dataSupplier;
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

    public Text localise(Supplier<Object[]> dataSupplier) {
        this.dataSupplier = dataSupplier;
        return this;
    }

    public Text localise(Object... data) {
        return localise(() -> data);
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

    public boolean isShadow() {
        return shadow;
    }

    public String getRawText() {
        String text = textSupplier == null ? this.text : textSupplier.get();
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && dataSupplier != null) {
            text = I18n.format(text, dataSupplier.get());
        }
        return text;
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        String text = TextRenderer.getColorFormatString(color) + getRawText();
        TextRenderer.drawString(text, pos.x, pos.y, color, shadow, size.width);
    }

    @SideOnly(Side.CLIENT)
    public Pos2d drawText(Pos2d pos, int maxWidth) {
        String text = getRawText();
        return drawSplitString(text, pos.x, pos.y, maxWidth);
    }

    @SideOnly(Side.CLIENT)
    public static Pos2d drawText(Pos2d pos, int maxWidth, List<Text> texts) {
        Pos2d drawPos = new Pos2d(pos.x, pos.y);
        for (Text text : texts) {
            drawPos = text.drawText(drawPos, maxWidth);
        }
        return drawPos;
    }

    /**
     * Needed to copy a bunch of methods from front renderer because you can't draw a split string with shadow
     */
    @SideOnly(Side.CLIENT)
    private Pos2d drawSplitString(String str, float x, float y, int wrapWidth) {
        str = this.trimStringNewline(str);
        float lastX = x;
        for (String s : renderer.listFormattedStringToWidth(str, wrapWidth)) {
            lastX = this.renderStringAligned(s, x, y, wrapWidth);
            y += renderer.FONT_HEIGHT;
        }
        return new Pos2d(lastX, y);
    }

    @SideOnly(Side.CLIENT)
    private int renderStringAligned(String text, float x, float y, int width) {
        if (renderer.getBidiFlag()) {
            int i = renderer.getStringWidth(this.bidiReorder(text));
            x = x + width - i;
        }
        return renderer.drawString(text, x, y, color, shadow);
    }

    @SideOnly(Side.CLIENT)
    private String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    @SideOnly(Side.CLIENT)
    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
