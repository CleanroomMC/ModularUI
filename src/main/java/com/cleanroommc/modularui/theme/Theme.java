package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.config.Config;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class Theme {

    public static final Config CONFIG;

    private static final Map<String, Theme> THEMES = new Object2ObjectOpenHashMap<>();

    //public static final Theme VANILLA = new Theme(0, 0, 0, GuiTextures.BACKGROUND, 0xFF404040, 0, GuiTextures.BUTTON);
    //public static final Theme FANCY = new Theme(0xff0088ff, 0xFF5EBDFF, 0xcc000000, null, 0xFFFFFFFF, 0xff0088ff, null);

    /*public final ValueInt primaryColor;
    public final ValueInt accentColor;
    public final ValueInt backgroundColor;
    public final ValueTexture backgroundTexture;
    public final ValueInt textColor;
    public final ValueInt buttonColor;
    public final ValueTexture buttonTexture;

    public Theme(int primaryColor, int accentColor, int backgroundColor, UITexture backgroundTexture, int textColor, int buttonColor, UITexture buttonTexture) {
        this.primaryColor = new ValueInt("primaryColor", primaryColor).subtype(ValueInt.Subtype.COLOR_ALPHA);
        this.accentColor = new ValueInt("accentColor", accentColor).subtype(ValueInt.Subtype.COLOR_ALPHA);
        this.backgroundColor = new ValueInt("backgroundColor", backgroundColor).subtype(ValueInt.Subtype.COLOR_ALPHA);
        this.backgroundTexture = new ValueTexture("backgroundTexture", backgroundTexture);
        this.textColor = new ValueInt("textColor", textColor).subtype(ValueInt.Subtype.COLOR);
        this.buttonColor = new ValueInt("buttonColor", buttonColor).subtype(ValueInt.Subtype.COLOR_ALPHA);
        this.buttonTexture = new ValueTexture("buttonTexture", buttonTexture);
    }

    public void drawBackground(int x, int y, int w, int h) {
        UITexture texture = backgroundTexture.getTexture();
        if (texture == null) {
            GuiScreen.drawRect(x, y, w, h, backgroundColor.get());
        } else {
            Color.setGlColor(backgroundColor.get());
            texture.draw(x, y, w, h);
            Color.setGlColor(Color.WHITE.normal);
        }
    }*/

    static {
        Config.Builder builder = Config.builder("themes");

        CONFIG = builder.build();
    }
}
