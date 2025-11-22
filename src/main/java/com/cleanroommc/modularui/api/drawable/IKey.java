package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.text.AnimatedText;
import com.cleanroommc.modularui.drawable.text.CompoundKey;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.drawable.text.FormattingState;
import com.cleanroommc.modularui.drawable.text.KeyIcon;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.drawable.text.StringKey;
import com.cleanroommc.modularui.drawable.text.StyledText;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widgets.TextWidget;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * This represents a piece of text in a GUI.
 */
public interface IKey extends IDrawable, IJsonSerializable {

    int TEXT_COLOR = 0xFF404040;

    TextRenderer renderer = new TextRenderer();

    IKey EMPTY = str("");
    IKey LINE_FEED = str("\n");
    IKey SPACE = str(" ");

    // Formatting for convenience
    TextFormatting BLACK = TextFormatting.BLACK;
    TextFormatting DARK_BLUE = TextFormatting.DARK_BLUE;
    TextFormatting DARK_GREEN = TextFormatting.DARK_GREEN;
    TextFormatting DARK_AQUA = TextFormatting.DARK_AQUA;
    TextFormatting DARK_RED = TextFormatting.DARK_RED;
    TextFormatting DARK_PURPLE = TextFormatting.DARK_PURPLE;
    TextFormatting GOLD = TextFormatting.GOLD;
    TextFormatting GRAY = TextFormatting.GRAY;
    TextFormatting DARK_GRAY = TextFormatting.DARK_GRAY;
    TextFormatting BLUE = TextFormatting.BLUE;
    TextFormatting GREEN = TextFormatting.GREEN;
    TextFormatting AQUA = TextFormatting.AQUA;
    TextFormatting RED = TextFormatting.RED;
    TextFormatting LIGHT_PURPLE = TextFormatting.LIGHT_PURPLE;
    TextFormatting YELLOW = TextFormatting.YELLOW;
    TextFormatting WHITE = TextFormatting.WHITE;
    TextFormatting OBFUSCATED = TextFormatting.OBFUSCATED;
    TextFormatting BOLD = TextFormatting.BOLD;
    TextFormatting STRIKETHROUGH = TextFormatting.STRIKETHROUGH;
    TextFormatting UNDERLINE = TextFormatting.UNDERLINE;
    TextFormatting ITALIC = TextFormatting.ITALIC;
    TextFormatting RESET = TextFormatting.RESET;

    /**
     * Creates a translated text.
     *
     * @param key translation key
     * @return text key
     */
    static IKey lang(@NotNull String key) {
        return new LangKey(key);
    }

    /**
     * Creates a translated text with arguments. The arguments can change.
     *
     * @param key  translation key
     * @param args translation arguments
     * @return text key
     */
    static IKey lang(@NotNull String key, @Nullable Object... args) {
        return new LangKey(key, args);
    }

    /**
     * Creates a translated text with arguments supplier.
     *
     * @param key          translation key
     * @param argsSupplier translation arguments supplier
     * @return text key
     */
    static IKey lang(@NotNull String key, @NotNull Supplier<Object[]> argsSupplier) {
        return new LangKey(key, argsSupplier);
    }

    /**
     * Creates a translated text.
     *
     * @param keySupplier translation key supplier
     * @return text key
     */
    static IKey lang(@NotNull Supplier<String> keySupplier) {
        return new LangKey(keySupplier);
    }

    /**
     * Creates a translated text with arguments supplier.
     *
     * @param keySupplier  translation key supplier
     * @param argsSupplier translation arguments supplier
     * @return text key
     */
    static IKey lang(@NotNull Supplier<String> keySupplier, @NotNull Supplier<Object[]> argsSupplier) {
        return new LangKey(keySupplier, argsSupplier);
    }

    /**
     * Creates a string literal text.
     *
     * @param key string
     * @return text key
     */
    static IKey str(@NotNull String key) {
        return new StringKey(key);
    }

    /**
     * Creates a formatted string literal text with arguments. The arguments can be dynamic.
     * The string is formatted using {@link String#format(String, Object...)}.
     *
     * @param key  string
     * @param args arguments
     * @return text key
     */
    static IKey str(@NotNull String key, @Nullable Object... args) {
        return new StringKey(key, args);
    }

    /**
     * @deprecated renamed to str()
     */
    @Deprecated
    static IKey format(@NotNull String key, @Nullable Object... args) {
        return str(key, args);
    }

    /**
     * Creates a composed text key.
     *
     * @param keys text keys
     * @return composed text key.
     */
    static IKey comp(@NotNull IKey... keys) {
        return new CompoundKey(keys);
    }

    /**
     * Creates a dynamic text key.
     *
     * @param getter string supplier
     * @return dynamic text key
     */
    static IKey dynamic(@NotNull Supplier<@NotNull String> getter) {
        return dynamicKey(() -> IKey.str(getter.get()));
    }

    /**
     * Creates a dynamic text key.
     *
     * @param getter key supplier
     * @return dynamic text key
     */
    static IKey dynamicKey(@NotNull Supplier<@NotNull IKey> getter) {
        return new DynamicKey(getter);
    }

    /**
     * @return the current unformatted string
     */
    String get();

    /**
     * @param parentFormatting formatting of the parent in case of composite keys
     * @return the current formatted string
     */
    default String getFormatted(@Nullable FormattingState parentFormatting) {
        return get();
    }

    /**
     * @return the current formatted string
     */
    default String getFormatted() {
        return getFormatted(null);
    }

    @SideOnly(Side.CLIENT)
    @Override
    default void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        drawAligned(context, x, y, width, height, widgetTheme, Alignment.CENTER);
    }

    @SideOnly(Side.CLIENT)
    default void drawAligned(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme, Alignment alignment) {
        renderer.setColor(widgetTheme.getTextColor());
        renderer.setShadow(widgetTheme.getTextShadow());
        renderer.setAlignment(alignment, width, height);
        renderer.setScale(getScale());
        renderer.setPos(x, y);
        renderer.draw(getFormatted());
    }

    @Override
    default boolean canApplyTheme() {
        return true;
    }

    @Override
    default int getDefaultWidth() {
        renderer.setAlignment(Alignment.TopLeft, -1, -1);
        renderer.setScale(getScale());
        renderer.setPos(0, 0);
        renderer.setSimulate(true);
        renderer.draw(getFormatted());
        renderer.setSimulate(false);
        return (int) renderer.getLastActualWidth();
    }

    @Override
    default int getDefaultHeight() {
        renderer.setAlignment(Alignment.TopLeft, -1, -1);
        renderer.setScale(getScale());
        renderer.setPos(0, 0);
        renderer.setSimulate(true);
        renderer.draw(getFormatted());
        renderer.setSimulate(false);
        return (int) renderer.getLastActualHeight();
    }

    default float getScale() {
        return 1f;
    }

    default TextWidget<?> asWidget() {
        return new TextWidget<>(this);
    }

    default StyledText withStyle() {
        return new StyledText(this);
    }

    default AnimatedText withAnimation() {
        return new AnimatedText(this);
    }

    /**
     * @return a formatting state of this key
     */
    default @Nullable FormattingState getFormatting() {
        return null;
    }

    /**
     * Set text formatting to this key. If {@link IKey#RESET} is used, then that's applied first and then all other formatting of this key.
     * With {@link null}, you can remove a color formatting. No matter the parents color, the default color will be used.
     *
     * @param formatting a formatting rul
     * @return this
     */
    IKey style(@Nullable TextFormatting formatting);

    default IKey style(TextFormatting... formatting) {
        for (TextFormatting tf : formatting) style(tf);
        return this;
    }

    default IKey removeFormatColor() {
        return style((TextFormatting) null);
    }

    IKey removeStyle();

    default StyledText alignment(Alignment alignment) {
        return withStyle().alignment(alignment);
    }

    default StyledText color(int color) {
        return withStyle().color(() -> color);
    }

    default StyledText color(@Nullable IntSupplier color) {
        return withStyle().color(color);
    }

    default StyledText scale(float scale) {
        return withStyle().scale(scale);
    }

    default StyledText shadow(@Nullable Boolean shadow) {
        return withStyle().shadow(shadow);
    }

    @Override
    default Icon asIcon() {
        return new Icon(this);
    }

    default KeyIcon asTextIcon() {
        return new KeyIcon(this);
    }

    @Override
    default void loadFromJson(JsonObject json) {
        if (json.has("color") || json.has("shadow") || json.has("align") || json.has("alignment") || json.has("scale")) {
            StyledText styledText = this instanceof StyledText styledText1 ? styledText1 : withStyle();
            if (json.has("color")) {
                styledText.color(JsonHelper.getInt(json, 0, "color"));
            }
            styledText.shadow(JsonHelper.getBoolean(json, false, "shadow"));
            styledText.alignment(JsonHelper.deserialize(json, Alignment.class, styledText.getAlignment(), "align", "alignment"));
            styledText.scale(JsonHelper.getFloat(json, 1, "scale"));
        }
    }
}
