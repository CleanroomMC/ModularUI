package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.MCHelper;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FontRenderHelper {

    private static final int min = 48, max = 114;
    // array to access text formatting by character fast
    private static final TextFormatting[] formattingMap = new TextFormatting[max - min + 1];

    static {
        for (TextFormatting formatting : TextFormatting.values()) {
            char c = formatting.toString().charAt(1);
            formattingMap[c - min] = formatting;
            if (Character.isLetter(c)) {
                formattingMap[Character.toUpperCase(c) - min] = formatting;
            }
        }
    }

    /**
     * Returns the formatting for a character with a fast array lookup.
     *
     * @param c formatting character
     * @return formatting for character or null
     */
    @Nullable
    public static TextFormatting getForCharacter(char c) {
        if (c < min || c > max) return null;
        return formattingMap[c - min];
    }

    // a formatting state keeps track of a color format and each of the fancy style options
    // 0: color, 1 - 5: fancy style (random, bolt, italic, underline, strikethrough), 6: reset
    public static TextFormatting[] createFormattingState() {
        return new TextFormatting[7];
    }

    public static void addAfter(TextFormatting[] state, TextFormatting formatting) {
        if (formatting == TextFormatting.RESET) {
            Arrays.fill(state, null);
            state[6] = formatting;
            return;
        }
        // remove reset
        state[6] = null;
        if (formatting.isFancyStyling()) {
            state[formatting.ordinal() - 15] = formatting;
            return;
        }
        // color
        state[0] = formatting;
    }

    public static void parseFormattingState(TextFormatting[] state, String text) {
        int i = -2;
        while ((i = text.indexOf(167, i + 2)) >= 0 && i < text.length() - 1) {
            TextFormatting formatting = getForCharacter(text.charAt(i + 1));
            if (formatting != null) addAfter(state, formatting);
        }
    }

    public static String getFormatting(TextFormatting[] state) {
        if (isReset(state)) return TextFormatting.RESET.toString();
        StringBuilder builder = getFormatting(state, new StringBuilder());
        return builder.length() == 0 ? StringUtils.EMPTY : builder.toString();
    }

    public static StringBuilder getFormatting(TextFormatting[] state, StringBuilder builder) {
        for (int i = 0, n = 6; i < n; i++) {
            TextFormatting formatting = state[i];
            if (formatting != null) builder.append(formatting);
        }
        return builder;
    }

    public static boolean isReset(TextFormatting[] state) {
        return state[6] != null;
    }

    public static int getDefaultTextHeight() {
        FontRenderer fr = MCHelper.getFontRenderer();
        return fr != null ? fr.FONT_HEIGHT : 9;
    }

    /**
     * Calculates how many formatting characters there are at the given position of the string.
     *
     * @param s     string
     * @param start starting index
     * @return amount of formatting characters at index
     */
    public static int getFormatLength(String s, int start) {
        int i = Math.max(0, start);
        int l = 0;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 167) {
                if (i + 1 >= s.length()) return l;
                if (getForCharacter(c) == null) return l;
                l += 2;
                i++;
            } else {
                return l;
            }
        }
        return l;
    }
}
