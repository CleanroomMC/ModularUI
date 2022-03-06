package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.mixin.FontRendererMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class TextRenderer {

    public static final FontRenderer FR = Minecraft.getMinecraft().fontRenderer;
    public static final char FORMAT_CHAR = '\u00a7';
    public static int DEFAULT_COLOR = 0x212121;

    public static String getColorFormatString(int color) {
        return FORMAT_CHAR + "{#" + Integer.toHexString(color) + "}";
    }

    public static void drawString(String text, Pos2d pos, int color, int maxWidth) {
        TextRenderer renderer = new TextRenderer(pos, color, maxWidth);
        renderer.draw(text);
    }

    public static void drawString(String text, Pos2d pos, int color, int maxWidth, float textScale) {
        TextRenderer renderer = new TextRenderer(pos, color, maxWidth);
        renderer.setScale(textScale);
        renderer.draw(text);
    }

    private Pos2d pos;
    private int defaultColor = DEFAULT_COLOR;
    private float scale = 1f;
    private int maxX;
    private boolean forceShadow = false;
    private boolean doDraw = true;
    private Pos2d posToFind = null;

    protected int currentX, currentColor, wordWith = 0, currentIndex = 0;
    protected StringBuilder currentWord;
    private boolean breakLine = true;
    private int width = 0, height = 0;
    private int foundIndex = -1;
    private int[] lineWidths = null;
    private int alignment = -1; // -1 = left, 0 = center, +1 = right
    private int lineXOffset = 0;
    private int currentLine = 0;

    private boolean shadowStyle;

    public TextRenderer() {
        this(Pos2d.ZERO, 0, 0);
    }

    public TextRenderer(Pos2d pos, int color, int maxWidth) {
        setUp(pos, color, maxWidth);
    }

    public void setUp(Pos2d pos, int color, int maxWidth) {
        this.maxX = pos.x + maxWidth;
        this.currentColor = color;
        this.defaultColor = color;
        this.pos = pos;
        this.currentX = pos.x;
        this.currentIndex = 0;
        this.width = 0;
        this.height = 0;
        this.foundIndex = -1;
        this.posToFind = null;
        this.doDraw = true;
        this.currentLine = 0;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void forceShadow(boolean forceShadow) {
        this.forceShadow = forceShadow;
        this.shadowStyle = forceShadow;
    }

    public int getFoundIndex() {
        return foundIndex;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setDoDraw(boolean doDraw) {
        this.doDraw = doDraw;
    }

    public int findPos(Pos2d pos, String text) {
        setDoDraw(false);
        this.posToFind = pos;
        draw(text);
        setDoDraw(true);
        return foundIndex;
    }

    public void setPosToFind(Pos2d pos) {
        this.posToFind = pos;
    }

    public Size calculateSize(String text) {
        setDoDraw(false);
        draw(text);
        setDoDraw(true);
        return new Size(width, height);
    }

    public void draw(String text, Pos2d pos, int color, int maxWidth) {
        setUp(pos, color, maxWidth);
        this.alignment = -1;
        this.lineWidths = null;
        draw(text);
    }

    public void drawAligned(String text, int alignment, Pos2d pos, int color, int maxWidth) {
        if (alignment < 0) {
            // Left align doesn't require calculations
            setUp(pos, color, maxWidth);
            draw(text);
            return;
        }
        // first simulate rendering and collect widths of all lines
        setUp(pos, color, maxWidth);
        setDoDraw(false);
        this.alignment = alignment;
        this.lineWidths = new int[0];
        draw(text);
        setDoDraw(true);
        // now render offset according to the line widths
        setUp(pos, color, maxWidth);
        draw(text);
        this.lineWidths = null;
        this.alignment = -1;
    }

    public void draw(String text) {
        if (maxX - pos.x <= 0) {
            return;
        }
        currentWord = new StringBuilder();
        resetStyles();
        applyAlignment();

        boolean wasFormatChar = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                if (wasFormatChar) {
                    addChar('\u00a7');
                    wasFormatChar = false;
                }
                addChar(' ');
                newWord();
            } else if (c == '\n') {
                if (wasFormatChar) {
                    addChar('\u00a7');
                    wasFormatChar = false;
                }
                newWord();
                newLine();
            } else if (wasFormatChar) {
                if (c == '{') {
                    int closing = text.indexOf('}', i + 1);
                    if (closing < 0) {
                        addChar('\u00a7');
                        addChar('{');
                        break;
                    }
                    newWord();
                    String color = text.substring(i + 2, closing);
                    try {
                        this.currentColor = Integer.parseInt(color, 16);
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
            if (foundIndex >= 0) {
                break;
            }
        }
        newWord();
        newLine();
        if (doDraw && FMLCommonHandler.instance().getSide().isClient()) {
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
    }

    public void newLine() {
        width = Math.max(currentX - pos.x, width);
        if (!doDraw && lineWidths != null) {
            lineWidths = ArrayUtils.add(lineWidths, currentX - pos.x);
        }
        height += getFontHeight();
        currentX = pos.x;
        currentLine++;
        applyAlignment();
    }

    protected void newWord() {
        newWord(true);
    }

    protected void newWord(boolean breakLine) {
        if (doDraw && FMLCommonHandler.instance().getSide().isClient()) {
            drawWord();
        }
        this.currentWord = new StringBuilder();
        currentX += wordWith;
        wordWith = 0;
        this.breakLine = breakLine;
    }

    @SideOnly(Side.CLIENT)
    protected void drawWord() {
        String word = this.currentWord.toString();
        if (word.isEmpty()) {
            return;
        }

        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        renderText(word, (lineXOffset + currentX) * sf, getCurrentY() * sf, currentColor, shadowStyle, false);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    private void addChar(char c) {
        addChar(c, true);
    }

    protected void addChar(char c, boolean addToWidth) {
        if (addToWidth) {
            float charWidth = FR.getCharWidth(c) * scale;
            // line will is to wide with this character
            if (currentX + wordWith + charWidth > maxX) {
                // word occupies the whole width or a new word starts
                //didHitRightBorder = true;
                if (pos.x == currentX || c == ' ') {
                    //needsNewLine = true;
                    newWord();
                    newLine();
                    if (c == ' ') {
                        // don't add space to new line
                        return;
                    }
                } else {
                    if (!breakLine) {
                        newWord();
                    }
                    // go to next line before drawing the current word
                    newLine();
                }
            }
            if (posToFind != null) {
                if (pos.y + height <= posToFind.y && pos.y + height + getFontHeight() > posToFind.y &&
                        currentX + wordWith <= posToFind.x && currentX + wordWith + charWidth > posToFind.x) {
                    foundIndex = currentIndex;
                }
            }
            wordWith += charWidth;
        }
        currentWord.append(c);
        currentIndex++;
    }

    protected void applyAlignment() {
        if (doDraw && lineWidths != null && lineWidths.length > currentLine) {
            if (alignment == 0) {
                lineXOffset = (int) ((maxX - pos.x) / 2f - lineWidths[currentLine] / 2f);
            } else {
                lineXOffset = maxX - pos.x - lineWidths[currentLine];
            }
        } else {
            lineXOffset = 0;
        }
    }

    private void resetStyles() {
        getMixinRenderer().invokeResetStyles();
        this.shadowStyle = this.forceShadow;
        this.currentColor = this.defaultColor;
    }

    private boolean checkStyleChar(char c) {
        boolean formatSpecial = isFormatSpecial(c);
        if (formatSpecial) {
            switch (c) {
                case 'r':
                case 'R':
                    if (this.currentColor != defaultColor || (shadowStyle && !forceShadow)) {
                        newWord();
                    }
                    resetStyles();
                    break;
                case 's':
                case 'S':
                    if (!shadowStyle) {
                        newWord();
                    }
                    shadowStyle = true;
                    break;
            }
        }
        return !(formatSpecial || isFormatColor(c));
    }

    public float getFontHeight() {
        return FR.FONT_HEIGHT * scale;
    }

    public static int getDefaultColor() {
        return DEFAULT_COLOR;
    }

    public float getScale() {
        return scale;
    }

    public boolean isShadowStyle() {
        return shadowStyle;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public float getCurrentY() {
        return pos.y + height;
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

    /**
     * A different version of {@link FontRenderer#drawString(String, float, float, int, boolean)}, which optionally calls resetStyles.
     *
     * @param resetStyles if styles should be reset before rendering.
     * @return width
     */
    public static int renderText(String text, float x, float y, int color, boolean dropShadow, boolean resetStyles) {
        FontRendererMixin fr = getMixinRenderer();
        GlStateManager.enableAlpha();
        if (resetStyles) {
            fr.invokeResetStyles();
        }
        int i;

        if (dropShadow) {
            i = fr.invokeRenderString(text, x + 1.0F, y + 1.0F, color, true);
            i = Math.max(i, fr.invokeRenderString(text, x, y, color, false));
        } else {
            i = fr.invokeRenderString(text, x, y, color, false);
        }
        return i;
    }

    private static FontRendererMixin getMixinRenderer() {
        return (FontRendererMixin) (Object) FR;
    }
}
