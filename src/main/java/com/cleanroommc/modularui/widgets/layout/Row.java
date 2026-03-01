package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.GuiAxis;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval(inVersion = "3.3.0")
@Deprecated
public class Row extends Flow {

    public Row() {
        super(GuiAxis.X);
    }
}
