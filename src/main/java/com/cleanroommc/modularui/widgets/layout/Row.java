package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.GuiAxis;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.2")
public class Row extends Flow {

    public Row() {
        super(GuiAxis.X);
    }
}
