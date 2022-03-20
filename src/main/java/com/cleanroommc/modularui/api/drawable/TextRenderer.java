package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.mixin.FontRendererMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class TextRenderer {

    public static final FontRenderer FR = Minecraft.getMinecraft().fontRenderer;
    public static final char FORMAT_CHAR = '\u00a7';
    public static int DEFAULT_COLOR = 0x404040;

    public static String getColorFormatString(int color) {
        return FORMAT_CHAR + "{#" + Integer.toHexString(color & 0xFFFFFF) + "}";
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

    protected float x, y;
    protected int defaultColor = DEFAULT_COLOR;
    protected float scale = 1f;
    protected float maxX;
    protected boolean forceShadow = false;
    protected boolean doDraw = true;
    protected Pos2d posToFind = null;

    protected float currentX, wordWith = 0;
    protected int currentColor, currentIndex = 0;
    protected StringBuilder currentWord;
    private boolean breakLine = true;
    private float width = 0, height = 0;
    protected int foundIndex = -1;
    protected int[] lineWidths = null;
    protected int alignment = -1; // -1 = left, 0 = center, +1 = right
    protected float lineXOffset = 0;
    protected int currentLine = 0;

    private boolean shadowStyle;
    private String colorStyle = null;

    public TextRenderer() {
        this(0, 0, 0, 0);
    }

    public TextRenderer(Pos2d pos, int color, float maxWidth) {
        this(pos.x, pos.y, color, maxWidth);
    }

    public TextRenderer(float x, float y, int color, float maxWidth) {
        setUp(x, y, color, maxWidth);
    }

    public void setUp(Pos2d pos, int color, float maxWidth) {
        setUp(pos.x, pos.y, color, maxWidth);
    }

    public void setUp(float x, float y, int color, float maxWidth) {
        this.maxX = x + maxWidth;
        this.currentColor = color;
        this.defaultColor = color;
        this.x = x;
        this.y = y;
        this.currentX = x;
        this.currentIndex = 0;
        this.width = 0;
        this.height = 0;
        this.foundIndex = -1;
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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
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
        return new Size((int) (width + 0.5f), (int) (height + 0.5f));
    }

    public void draw(String text, Pos2d pos, int color, int maxWidth) {
        setUp(pos, color, maxWidth);
        this.alignment = -1;
        this.lineWidths = null;
        draw(text);
    }

    public void drawAligned(String text, float x, float y, float maxWidth, int color, int alignmentX) {
        drawAligned(text, x, y, maxWidth, -1, color, alignmentX, -1);
    }

    public void drawAligned(String text, float x, float y, float maxWidth, float height, int color, int alignmentX, int alignmentY) {
        if (alignmentX < 0 && alignmentY < 0) {
            // Left align doesn't require calculations
            setUp(x, y, color, maxWidth);
            draw(text);
            return;
        }
        Pos2d posToFind2 = posToFind;
        boolean doDraw2 = doDraw;
        posToFind = null;
        alignmentY = MathHelper.clamp(alignmentY, -1, 1);
        // first simulate rendering and collect widths of all lines
        setUp(x, y, color, maxWidth);
        setDoDraw(false);
        if (alignmentX >= 0) {
            this.alignment = alignmentX;
            this.lineWidths = new int[0];
        }
        draw(text);
        setDoDraw(doDraw2);
        posToFind = posToFind2;
        // now render offset according to the line widths
        float y1 = (alignmentY + 1) / 2f;
        if (this.height == 0) {
            this.height = getFontHeight();
        }
        y1 = height * y1 - this.height * y1;
        setUp(x, y + y1, color, maxWidth);
        draw(text);
        this.lineWidths = null;
        this.alignment = -1;
    }

    public void draw(String text) {
        if (maxX - x <= 0) {
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
        width = (int) Math.max(currentX - x, width);
        if (!doDraw && lineWidths != null) {
            lineWidths = ArrayUtils.add(lineWidths, (int) (currentX - x));
        }
        height += getFontHeight();
        currentX = x;
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
        if (colorStyle != null) {
            word = FORMAT_CHAR + colorStyle + word;
        }

        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        renderText(word, getRenderX(sf), getCurrentY() * sf, currentColor, shadowStyle, false);
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
                if (x == currentX || c == ' ') {
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
                if (y + height <= posToFind.y && y + height + getFontHeight() > posToFind.y &&
                        currentX + lineXOffset + wordWith <= posToFind.x && currentX + lineXOffset + wordWith + charWidth > posToFind.x) {
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
                lineXOffset = (maxX - x) / 2f - lineWidths[currentLine] / 2f;
            } else {
                lineXOffset = maxX - x - lineWidths[currentLine];
            }
        } else {
            lineXOffset = 0;
        }
    }

    private void resetStyles() {
        getMixinRenderer().invokeResetStyles();
        this.shadowStyle = this.forceShadow;
        this.currentColor = this.defaultColor;
        this.colorStyle = null;
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
        boolean color = isFormatColor(c);
        if (color) {
            colorStyle = String.valueOf(c);
        }
        return !(formatSpecial || color);
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
        return y + height;
    }

    public float getRenderX(float scaleFactor) {
        return (currentX + lineXOffset) * scaleFactor;
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
