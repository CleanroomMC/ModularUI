package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.config.Config;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Color;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class Theme {

    public static final Config CONFIG;

    private static final Map<String, Theme> THEMES = new Object2ObjectOpenHashMap<>();

    protected IDrawable panelBackground = GuiTextures.BACKGROUND;
    protected IDrawable buttonBackground = GuiTextures.BUTTON;
    protected IDrawable disabledButtonBackground = GuiTextures.SLOT_DARK;
    protected int textColor = 0xFF404040;
    protected int buttonTextColor = Color.WHITE.normal;
    protected boolean textShadow = false;
    protected boolean buttonTextShadow = true;
    protected int panelColor = 0;
    protected int buttonColor = 0;

    static {
        Config.Builder builder = Config.builder("themes");

        CONFIG = builder.build();
    }
}
