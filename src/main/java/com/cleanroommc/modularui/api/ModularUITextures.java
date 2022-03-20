package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.api.drawable.UITexture;

public class ModularUITextures {

    public static final UITexture ICON_INFO = UITexture.fullImage(ModularUI.ID, "gui/widgets/information");
    public static final UITexture VANILLA_BACKGROUND = AdaptableUITexture.of(ModularUI.ID, "gui/background/vanilla_background", 195, 136, 4);

    public static final UITexture VANILLA_TAB_TOP = UITexture.fullImage(ModularUI.ID, "gui/tab/tabs_top");
    public static final UITexture VANILLA_TAB_BOTTOM = UITexture.fullImage(ModularUI.ID, "gui/tab/tabs_bottom");
    public static final UITexture VANILLA_TAB_LEFT = UITexture.fullImage(ModularUI.ID, "gui/tab/tabs_left");
    public static final UITexture VANILLA_TAB_RIGHT = UITexture.fullImage(ModularUI.ID, "gui/tab/tabs_right");

    public static final UITexture VANILLA_TAB_TOP_START = VANILLA_TAB_TOP.getSubArea(0f, 0f, 1 / 3f, 1f);
    public static final UITexture VANILLA_TAB_TOP_MIDDLE = VANILLA_TAB_TOP.getSubArea(1 / 3f, 0f, 2 / 3f, 1f);
    public static final UITexture VANILLA_TAB_TOP_END = VANILLA_TAB_TOP.getSubArea(2 / 3f, 0f, 1f, 1f);
}
