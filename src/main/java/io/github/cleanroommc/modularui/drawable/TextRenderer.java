package io.github.cleanroommc.modularui.drawable;

import io.github.cleanroommc.modularui.ModularUIMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextRenderer {

    public static final FontRenderer FR = Minecraft.getMinecraft().fontRenderer;
    public static final char FORMAT_CHAR = '\u00a7';
    public static int DEFAULT_COLOR = 0x212121;

    public static void drawString(String text, float x, float y, int color, boolean dropShadow, float maxWidth) {
        TextRenderer renderer = new TextRenderer(x, y, color, dropShadow, maxWidth);
        renderer.draw(text);
    }

    public static String getColorFormatString(int color) {
        return FORMAT_CHAR + "{#" + Integer.toHexString(color) + "}";
    }

    private final float maxX;
    private int color;
    private final int defaultColor;
    private final boolean shadow;
    private final float x, y;
    private float currentX, currentY;
    private float currentWidth = 0;
    private StringBuilder word;
    private boolean needsNewLine = false;
    private final float scale = 0.5f;

    public TextRenderer(float x, float y, int color, boolean dropShadow, float maxWidth) {
        this.maxX = x + maxWidth;
        this.color = color;
        this.defaultColor = color;
        this.shadow = dropShadow;
        this.x = x;
        this.y = y;
        this.currentX = x;
        this.currentY = y;
    }

    public void draw(String text) {
        word = new StringBuilder();

        boolean wasFormatChar = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                if (wasFormatChar) {
                    addChar('\u00a7');
                    wasFormatChar = false;
                }
                addChar(' ');
                drawWord();
            }

            if (wasFormatChar) {
                if (c == '{') {
                    int closing = text.indexOf('}', i + 1);
                    if (closing < 0) {
                        addChar('\u00a7');
                        addChar('{');
                        break;
                    }
                    drawWord();
                    String color = text.substring(i + 2, closing);
                    try {
                        this.color = Integer.parseInt(color, 16);
                    } catch (NumberFormatException e) {
                        ModularUIMod.LOGGER.throwing(e);
                    }
                    i = closing;
                } else {
                    boolean formatChar = !(isFormatColor(c) || isFormatSpecial(c));
                    if (!formatChar && (c == 'r' || c == 'R')) {
                        drawWord();
                        this.color = defaultColor;
                    }
                    addChar('\u00a7', formatChar);
                    addChar(c, formatChar);
                }
                wasFormatChar = false;
            } else {
                if (c == 167) {
                    wasFormatChar = true;
                } else {
                    addChar(c);
                }
            }
        }
        drawWord();
    }

    private boolean checkShouldDraw(float charWidth) {
        return currentX + currentWidth + charWidth > maxX;
    }

    private void drawWord() {
        String word = this.word.toString();
        if (word.isEmpty()) {
            return;
        }

        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        FR.drawString(word, currentX * sf, currentY * sf, color, shadow);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();

        this.word = new StringBuilder();
        currentX += currentWidth;
        currentWidth = 0;
        if (needsNewLine) {
            currentX = x;
            currentY += FR.FONT_HEIGHT * scale;
            needsNewLine = false;
        }
    }

    private void addChar(char c) {
        addChar(c, true);
    }

    private void addChar(char c, boolean addToWidth) {
        if (addToWidth) {
            float charWidth = FR.getCharWidth(c) * scale;
            if (checkShouldDraw(charWidth)) {
                needsNewLine = true;
                drawWord();
            }
            currentWidth += charWidth;
        }
        word.append(c);
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
    }
}
