package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.utils.Color;

public class DebugOptions {

    public static final DebugOptions INSTANCE = new DebugOptions();

    public boolean showHovered = true;
    public boolean showName = true;
    public boolean showPos = true;
    public boolean showSize = true;
    public boolean showRelPos = true;
    public boolean showWidgetTheme = true;
    public boolean showOutline = true;

    public boolean showParent = true;
    public boolean showParentName = true;
    public boolean showParentPos = true;
    public boolean showParentSize = true;
    public boolean showParentRelPos = false;
    public boolean showParentWidgetTheme = false;
    public boolean showParentOutline = true;

    public int textColor = Color.argb(180, 40, 115, 220);
    public int outlineColor = textColor;
    public int cursorColor = Color.GREEN.main;
    public float scale = 0.8f;

}
