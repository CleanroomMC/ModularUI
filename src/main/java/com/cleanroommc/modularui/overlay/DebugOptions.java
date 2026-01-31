package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.FloatValue;
import com.cleanroommc.modularui.value.IntValue;

public class DebugOptions {

    public static final DebugOptions INSTANCE = new DebugOptions();

    public BoolValue showHovered = new BoolValue(true);
    public BoolValue showPos = new BoolValue(true);
    public BoolValue showSize = new BoolValue(true);
    public BoolValue showWidgetTheme = new BoolValue(true);
    public BoolValue showExtra = new BoolValue(true);
    public BoolValue showOutline = new BoolValue(true);

    public BoolValue showParent = new BoolValue(true);
    public BoolValue showParentPos = new BoolValue(true);
    public BoolValue showParentSize = new BoolValue(true);
    public BoolValue showParentWidgetTheme = new BoolValue(false);
    public BoolValue showParentOutline = new BoolValue(true);

    public IntValue textColor = new IntValue(Color.argb(180, 40, 115, 220));
    public IntValue outlineColor = new IntValue(textColor.getIntValue());
    public IntValue cursorColor = new IntValue(Color.withAlpha(Color.GREEN.main, 0.8f));
    public FloatValue scale = new FloatValue(0.8f);

}
