package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.MCHelper;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FontRenderHelper {

    private static final int min = '0', max = 'r'; // min = 48, max = 114
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
        StringBuilder builder = appendFormatting(state, new StringBuilder());
        return builder.length() == 0 ? StringUtils.EMPTY : builder.toString();
    }

    public static StringBuilder appendFormatting(TextFormatting[] state, StringBuilder builder) {
        return appendFormatting(state, null, builder);
    }

    public static StringBuilder appendFormatting(TextFormatting[] state, TextFormatting @Nullable [] fallback, StringBuilder builder) {
        for (int i = 0, n = 6; i < n; i++) {
            if (state[i] != null) {
                builder.append(state[i]);
            } else if (fallback != null && fallback[i] != null) {
                builder.append(fallback[i]);
            }
        }
        return builder;
    }

    public static String format(@Nullable TextFormatting[] state, @Nullable TextFormatting[] parentState, String text) {
        if (state == null) {
            if (parentState == null) return text;
            return appendFormatting(parentState, new StringBuilder().append(TextFormatting.RESET)).append(text).toString();
        }
        StringBuilder s = appendFormatting(state, parentState, new StringBuilder().append(TextFormatting.RESET))
                .append(text);
        return s.toString();
    }

    public static TextFormatting @NotNull [] mergeState(TextFormatting @Nullable [] state1, TextFormatting @Nullable [] state2) {
        return mergeState(state1, state2, null);
    }

    public static TextFormatting @NotNull [] mergeState(TextFormatting @Nullable [] state1, TextFormatting @Nullable [] state2, TextFormatting @Nullable [] result) {
        if (state1 == null) {
            if (state2 == null) return createFormattingState();
            return state2;
        } else if (state2 == null) {
            return state1;
        }
        if (isReset(state2)) return state2; // state2 has higher priority
        if (result == null) result = Arrays.copyOf(state1, state1.length);
        for (int i = 0, n = 6; i < n; i++) {
            TextFormatting formatting = state2[i];
            if (formatting != null) addAfter(result, formatting);
        }
        return result;
    }

    public static boolean isReset(TextFormatting[] state) {
        return state[6] == TextFormatting.RESET;
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

    public static String fixString(String s, TextFormatting[] formatting) {
        int codes = getFormatLength(s, 0);
        if (codes == 0) return s;
        return s + getFormatting(formatting);
    }

    public static Object[] fixArgs(Object[] args, TextFormatting[] formatting) {
        Arrays.setAll(args, i -> fixString(args[i].toString(), formatting));
        return args;
    }
}
