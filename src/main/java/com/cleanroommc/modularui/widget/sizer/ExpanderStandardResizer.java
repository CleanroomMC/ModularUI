package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widgets.layout.IExpander;

public class ExpanderStandardResizer extends StandardResizer implements IExpander {

    private final GuiAxis axis;

    public ExpanderStandardResizer(IWidget widget, GuiAxis axis) {
        super(widget);
        this.axis = axis;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return axis;
    }
}
