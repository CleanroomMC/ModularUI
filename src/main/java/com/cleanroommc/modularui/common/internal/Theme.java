package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Color;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

public class Theme {

    public static final Theme INSTANCE = new Theme();

    public static final String KEY_BACKGROUND = "bg";
    public static final String KEY_BUTTON = "button";
    public static final String KEY_TEXT = "text";
    public static final String KEY_ITEM_SLOT = "itemslot";
    public static final String KEY_FLUID_SLOT = "fluidslot";
    public static final String KEY_SLOT_HIGHLIGHT = "slothighlight";

    private final Object2IntMap<String> colors = new Object2IntArrayMap<>(32);

    private Theme() {
        registerThemeColor(KEY_BACKGROUND, Color.LIGHT_BLUE.bright(0));
        registerThemeColor(KEY_BUTTON, Color.GREEN.normal);
        registerThemeColor(KEY_TEXT, 0x404040);
        registerThemeColor(KEY_ITEM_SLOT);
        registerThemeColor(KEY_FLUID_SLOT);
        registerThemeColor(KEY_SLOT_HIGHLIGHT, 0x80ffffff);
    }

    public void registerThemeColor(String name) {
        registerThemeColor(name, 0xFFFFFFFF);
    }

    public void registerThemeColor(String name, int value) {
        if (colors.containsKey(name)) {
            ModularUI.LOGGER.error("Theme already has a color with key {}", name);
            return;
        }
        colors.put(name, value);
    }

    public int getColor(@Nullable String key) {
        return key == null ? 0xFFFFFFFF : colors.getOrDefault(key, 0xFFFFFFFF);
    }

    public int getBackground() {
        return getColor(KEY_BACKGROUND);
    }

    public int getButton() {
        return getColor(KEY_BUTTON);
    }

    public int getText() {
        return getColor(KEY_TEXT);
    }

    public int getItemSlot() {
        return getColor(KEY_ITEM_SLOT);
    }

    public int getFluidSlot() {
        return getColor(KEY_FLUID_SLOT);
    }

    public int getSlotHighlight() {
        return getColor(KEY_SLOT_HIGHLIGHT);
    }
}
