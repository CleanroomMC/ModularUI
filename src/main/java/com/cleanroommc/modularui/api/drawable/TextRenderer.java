package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders strings with custom formatting into constrained sizes.
 * §{#FFFFFF} will color something white
 * §s will add shadow
 */
@SideOnly(Side.CLIENT)
public class TextRenderer {

    public static final FontRenderer FR = Minecraft.getMinecraft().fontRenderer;
    public static final char FORMAT_CHAR = '\u00a7';
    public static int DEFAULT_COLOR = 0x212121;

    public static void drawString(String text, Pos2d pos, int color, float maxWidth) {
        TextRenderer renderer = new TextRenderer(pos, color, maxWidth);
        renderer.draw(text);
    }

    public static void drawString(String text, Pos2d pos, int color, float maxWidth, float textScale) {
        TextRenderer renderer = new TextRenderer(pos, color, maxWidth);
        renderer.setScale(textScale);
        renderer.draw(text);
    }

    public static Size calcTextSize(String text, float maxWidth, float textScale) {
        TextRenderer renderer = new TextRenderer(Pos2d.ZERO, DEFAULT_COLOR, maxWidth);
        renderer.setScale(textScale);
        return renderer.calcSize(text);
    }

    public static String getColorFormatString(int color) {
        return FORMAT_CHAR + "{#" + Integer.toHexString(color) + "}";
    }

    private float maxX;
    private int color;
    private int defaultColor;
    private float scale = 1f;
    private Pos2d pos;
    private float currentX, currentY;
    private float currentWidth = 0;
    private float maxWidth = 0;
    private StringBuilder word;
    private boolean needsNewLine = false;
    private boolean forceShadow = false;
    private boolean didHitRightBorder = false;
    private boolean breakOnHitRightBorder = false;

    /**
     * Need to keep track of styles, because mc resets them on each draw call
     */
    private boolean randomStyle;
    private boolean boldStyle;
    private boolean italicStyle;
    private boolean underlineStyle;
    private boolean strikethroughStyle;
    /**
     * Custom style §s
     */
    private boolean shadowStyle;

    private boolean calcSizeMode = false;

    public TextRenderer() {
    }

    public TextRenderer(Pos2d pos, int color, float maxWidth) {
        setUp(pos, color, maxWidth);
    }

    public void setUp(Pos2d pos, int color, float maxWidth) {
        this.maxX = pos.x + maxWidth;
        this.color = color;
        this.defaultColor = color;
        this.pos = pos;
        this.currentX = pos.x;
        this.currentY = pos.y;
        this.didHitRightBorder = false;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void forceShadow(boolean forceShadow) {
        this.forceShadow = forceShadow;
        this.shadowStyle = forceShadow;
    }

    public void setBreakOnHitRightBorder(boolean breakOnHitRightBorder) {
        this.breakOnHitRightBorder = breakOnHitRightBorder;
    }

    public boolean didHitRightBorder() {
        return didHitRightBorder;
    }

    public Pos2d getLastPos() {
        return new Pos2d(currentX, currentY);
    }

    public Size calcSize(String text) {
        calcSizeMode = true;
        draw(text);
        int sizeX = (int) (Math.max(maxWidth, currentX - pos.x));
        int sizeY = (int) (currentY - pos.y + FR.FONT_HEIGHT * scale);
        calcSizeMode = false;
        return new Size(sizeX, sizeY);
    }

    public void draw(String text) {
        word = new StringBuilder();
        resetStyles();

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
            } else if (c == '\n') {
                if (wasFormatChar) {
                    addChar('\u00a7');
                    wasFormatChar = false;
                }
                needsNewLine = true;
                drawWord();
            } else if (wasFormatChar) {
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
                        ModularUI.LOGGER.throwing(e);
                    }
                    i = closing;
                } else {
                    boolean isNotStyleChar = checkStyleChar(c);
                    addChar('\u00a7', isNotStyleChar);
                    addChar(c, isNotStyleChar);
                }
                wasFormatChar = false;
            } else {
                if (c == 167) {
                    wasFormatChar = true;
                } else {
                    addChar(c);
                }
            }
            if (breakOnHitRightBorder && didHitRightBorder) {
                return;
            }
        }
        drawWord();
        if (!calcSizeMode) {
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
    }

    private void drawWord() {
        if (!calcSizeMode) {
            String word = this.word.toString();
            if (word.isEmpty()) {
                return;
            }
            word = getActiveStyles() + word;

            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 0f);
            float sf = 1 / scale;
            FR.drawString(word, currentX * sf, currentY * sf, color, shadowStyle);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        }

        this.word = new StringBuilder();
        currentX += currentWidth;
        currentWidth = 0;
        if (needsNewLine) {
            newLine();
        }
    }

    private void addChar(char c) {
        addChar(c, true);
    }

    private void addChar(char c, boolean addToWidth) {
        if (addToWidth) {
            float charWidth = FR.getCharWidth(c) * scale;
            // line will is to wide with this character
            if (currentX + currentWidth + charWidth > maxX) {
                // word occupies the whole width or a new word starts
                didHitRightBorder = true;
                if (pos.x == currentX || c == ' ') {
                    needsNewLine = true;
                    drawWord();
                    if (c == ' ') {
                        // don't add space to new line
                        return;
                    }
                } else {
                    // go to next line before drawing the current word
                    newLine();
                }
            }
            currentWidth += charWidth;
        }
        word.append(c);
    }

    public void newLine() {
        maxWidth = Math.max(currentX - pos.x, maxWidth);
        currentX = pos.x;
        currentY += FR.FONT_HEIGHT * scale;
        needsNewLine = false;
    }

    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
        this.shadowStyle = this.forceShadow;
    }

    private boolean checkStyleChar(char c) {
        boolean formatSpecial = isFormatSpecial(c);
        if (formatSpecial) {
            switch (c) {
                case 'r':
                case 'R':
                    if (this.color != defaultColor) {
                        drawWord();
                        this.color = defaultColor;
                    }
                    resetStyles();
                    break;
                case 'k':
                case 'K':
                    randomStyle = true;
                    break;
                case 'l':
                case 'L':
                    boldStyle = true;
                    break;
                case 'o':
                case 'O':
                    italicStyle = true;
                    break;
                case 'n':
                case 'N':
                    underlineStyle = true;
                    break;
                case 'm':
                case 'M':
                    strikethroughStyle = true;
                    break;
                case 's':
                case 'S':
                    shadowStyle = true;
                    break;
            }
        }
        return !(formatSpecial || isFormatColor(c));
    }

    private String getActiveStyles() {
        StringBuilder builder = new StringBuilder();
        if (randomStyle) {
            builder.append(FORMAT_CHAR).append('k');
        }
        if (boldStyle) {
            builder.append(FORMAT_CHAR).append('l');
        }
        if (italicStyle) {
            builder.append(FORMAT_CHAR).append('o');
        }
        if (underlineStyle) {
            builder.append(FORMAT_CHAR).append('n');
        }
        if (strikethroughStyle) {
            builder.append(FORMAT_CHAR).append('m');
        }
        if (shadowStyle) {
            builder.append(FORMAT_CHAR).append('s');
        }
        return builder.toString();
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' ||
                formatChar >= 'K' && formatChar <= 'O' ||
                formatChar == 'r' || formatChar == 'R' ||
                formatChar == 's' || formatChar == 'S';
    }
}
