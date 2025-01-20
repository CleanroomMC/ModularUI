package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IKey;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

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

    public static void addAfter(TextFormatting[] state, TextFormatting formatting, boolean removeAllOnReset) {
        if (formatting == TextFormatting.RESET) {
            if (removeAllOnReset) Arrays.fill(state, null);
            state[0] = formatting;
            return;
        }
        // remove reset
        if (removeAllOnReset) state[6] = null;
        if (formatting.isFancyStyling()) {
            state[formatting.ordinal() - 15] = formatting;
            return;
        }
        // color
        state[0] = formatting;
    }

    public static String format(@Nullable FormattingState state, @Nullable FormattingState parentState, String text) {
        if (state == null) {
            if (parentState == null) return text;
            return parentState.prependText(new StringBuilder().append(TextFormatting.RESET)).append(text).toString();
        }
        StringBuilder s = state.prependText(new StringBuilder().append(TextFormatting.RESET), parentState)
                .append(text);
        return s.toString();
    }

    public static String formatArgs(Object[] args, @Nullable FormattingState parentState, String text) {
        if (args == null || args.length == 0) return text;
        args = Arrays.copyOf(args, args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof IKey key) {
                // parent format + key format + key text + parent format
                args[i] = FormattingState.appendFormat(new StringBuilder(key.getFormatted(parentState))
                        .append(TextFormatting.RESET), parentState).toString();
            }
        }
        return String.format(text, args);
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
