package com.cleanroommc.modularui.drawable.text;

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class FormattingState {

    private TextFormatting reset;
    private TextFormatting color;
    private TextFormatting underline;
    private TextFormatting italic;
    private TextFormatting bold;
    private TextFormatting strikethrough;
    private TextFormatting obfuscated;
    private boolean forceDefaultColor;

    public void reset() {
        this.reset = null;
        this.color = null;
        this.underline = null;
        this.italic = null;
        this.bold = null;
        this.strikethrough = null;
        this.obfuscated = null;
        this.forceDefaultColor = false;
    }

    public void add(TextFormatting formatting, boolean removeAllOnReset) {
        if (formatting == TextFormatting.RESET) {
            if (removeAllOnReset) reset();
            this.reset = formatting;
            return;
        }
        if (formatting.isFancyStyling()) {
            switch (formatting) {
                case UNDERLINE -> this.underline = formatting;
                case ITALIC -> this.italic = formatting;
                case BOLD -> this.bold = formatting;
                case STRIKETHROUGH -> this.strikethrough = formatting;
                case OBFUSCATED -> this.obfuscated = formatting;
            }
            return;
        }
        this.color = formatting;
    }

    public void forceDefaultColor() {
        this.forceDefaultColor = true;
        this.color = null;
    }

    public String getFormatting() {
        StringBuilder sb = new StringBuilder();
        if (this.reset != null) sb.append(this.reset);
        if (this.color != null) sb.append(this.color);
        if (this.underline != null) sb.append(this.underline);
        if (this.italic != null) sb.append(this.italic);
        if (this.bold != null) sb.append(this.bold);
        if (this.strikethrough != null) sb.append(strikethrough);
        if (this.obfuscated != null) sb.append(obfuscated);
        return sb.toString();
    }

    public StringBuilder prependText(StringBuilder builder) {
        return prependText(builder, null);
    }

    public StringBuilder prependText(StringBuilder builder, @Nullable FormattingState fallback) {
        prependText(this, fallback, fs -> fs.reset, builder);
        if (!this.forceDefaultColor) {
            if (this.color != null) builder.append(color);
            else if (fallback != null && !fallback.forceDefaultColor && fallback.color != null) builder.append(fallback.color);
        }
        prependText(this, fallback, fs -> fs.underline, builder);
        prependText(this, fallback, fs -> fs.italic, builder);
        prependText(this, fallback, fs -> fs.bold, builder);
        prependText(this, fallback, fs -> fs.strikethrough, builder);
        prependText(this, fallback, fs -> fs.obfuscated, builder);
        return builder;
    }

    public void setFrom(FormattingState state) {
        this.reset = state.reset;
        this.color = state.color;
        this.underline = state.underline;
        this.italic = state.italic;
        this.bold = state.bold;
        this.strikethrough = state.strikethrough;
        this.obfuscated = state.obfuscated;
        this.forceDefaultColor = state.forceDefaultColor;
    }

    public void parseFrom(String text) {
        int i = -2;
        while ((i = text.indexOf(167, i + 2)) >= 0 && i < text.length() - 1) {
            TextFormatting formatting = FontRenderHelper.getForCharacter(text.charAt(i + 1));
            if (formatting != null) add(formatting, true);
        }
    }

    public FormattingState copy() {
        FormattingState state = new FormattingState();
        state.setFrom(this);
        return state;
    }

    public FormattingState merge(FormattingState state) {
        if (state.hasReset()) {
            setFrom(state);
            return this;
        }
        if (state.color != null) this.color = state.color;
        if (state.underline != null) this.underline = state.underline;
        if (state.italic != null) this.italic = state.italic;
        if (state.bold != null) this.bold = state.bold;
        if (state.strikethrough != null) this.strikethrough = state.strikethrough;
        if (state.obfuscated != null) this.obfuscated = state.obfuscated;
        if (state.forceDefaultColor) forceDefaultColor();
        return this;
    }

    public boolean hasReset() {
        return this.reset != null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reset", reset)
                .append("color", color)
                .append("underline", underline)
                .append("italic", italic)
                .append("bold", bold)
                .append("strikethrough", strikethrough)
                .append("obfuscated", obfuscated)
                .append("forceDefaultColor", forceDefaultColor)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormattingState that = (FormattingState) o;
        return forceDefaultColor == that.forceDefaultColor && reset == that.reset && color == that.color && underline == that.underline && italic == that.italic && bold == that.bold && strikethrough == that.strikethrough && obfuscated == that.obfuscated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reset, color, underline, italic, bold, strikethrough, obfuscated, forceDefaultColor);
    }

    private static void prependText(FormattingState state,
                                    @Nullable FormattingState fallback,
                                    Function<FormattingState, TextFormatting> getter,
                                    StringBuilder builder) {
        if (getter.apply(state) != null) builder.append(getter.apply(state));
        else if (fallback != null && getter.apply(fallback) != null) builder.append(getter.apply(fallback));
    }

    public static FormattingState merge(@Nullable FormattingState state1,
                                        @Nullable FormattingState state2) {
        return merge(state1, state2, null);
    }

    public static FormattingState merge(@Nullable FormattingState state1,
                                        @Nullable FormattingState state2,
                                        @Nullable FormattingState result) {
        if (state1 == null) {
            if (state2 == null) {
                if (result == null) result = new FormattingState();
                result.reset();
                return result;
            }
            return state2;
        } else if (state2 == null) {
            return state1;
        }
        if (result == null) result = new FormattingState();
        if (result != state1) result.setFrom(state1);
        return result.merge(state2);
    }

    public static StringBuilder appendFormat(StringBuilder builder, @Nullable FormattingState state) {
        return appendFormat(builder, state, null);
    }

    public static StringBuilder appendFormat(StringBuilder builder, @Nullable FormattingState state, @Nullable FormattingState fallback) {
        if (state == null) return builder;
        return state.prependText(builder, fallback);
    }
}
