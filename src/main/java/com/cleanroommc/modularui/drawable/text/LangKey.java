package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ClientScreenHandler;

import net.minecraft.client.resources.I18n;

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class LangKey extends BaseKey {

    private final Supplier<String> keySupplier;
    private final Supplier<Object[]> argsSupplier;
    private String string;
    private String lastLang;
    private Object[] lastArgs;
    private long time = 0;

    public LangKey(@NotNull String key) {
        this(key, () -> null);
    }

    public LangKey(@NotNull String key, @Nullable Object[] args) {
        this(key, () -> ArrayUtils.isEmpty(args) ? null : args);
    }

    public LangKey(@NotNull String key, @NotNull Supplier<Object[]> argsSupplier) {
        this(() -> Objects.requireNonNull(key), argsSupplier);
    }

    public LangKey(@NotNull Supplier<String> keySupplier) {
        this(keySupplier, () -> null);
    }

    public LangKey(@NotNull Supplier<String> keySupplier, @NotNull Supplier<Object[]> argsSupplier) {
        this.keySupplier = Objects.requireNonNull(keySupplier);
        this.lastLang = Objects.requireNonNull(keySupplier.get());
        this.argsSupplier = Objects.requireNonNull(argsSupplier);
        this.lastArgs = argsSupplier.get();
    }

    public Supplier<String> getKeySupplier() {
        return keySupplier;
    }

    public Supplier<Object[]> getArgsSupplier() {
        return argsSupplier;
    }

    public String getKey() {
        return this.lastLang;
    }

    public Object[] getArgs() {
        return this.lastArgs;
    }

    @Override
    public String get() {
        return toString(false, null);
    }

    @Override
    public String getFormatted(@Nullable TextFormatting[] parentFormatting) {
        return toString(true, parentFormatting);
    }

    private String toString(boolean formatted, TextFormatting @Nullable [] parentFormatting) {
        if (this.time != ClientScreenHandler.getTicks()) {
            this.time = ClientScreenHandler.getTicks();
            this.lastLang = this.keySupplier.get();
            this.lastArgs = FontRenderHelper.fixArgs(this.argsSupplier.get(), getFormatting(), parentFormatting);
            this.string = I18n.format(this.lastLang, this.lastArgs).replaceAll("\\\\n", "\n");
        }
        return formatted ? FontRenderHelper.format(getFormatting(), parentFormatting, this.string) : this.string;
    }
}
