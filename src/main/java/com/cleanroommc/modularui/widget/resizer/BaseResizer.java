package com.cleanroommc.modularui.widget.resizer;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IParentResizer;
import com.cleanroommc.modularui.api.IResizer;
import com.cleanroommc.modularui.utils.Area;

public abstract class BaseResizer implements IResizer, IParentResizer {

    @Override
    public void preApply(Area area) {
    }

    @Override
    public void apply(Area area) {
    }

    @Override
    public void apply(Area area, IResizer resizer, ChildResizer child) {
    }

    @Override
    public void postApply(Area area) {
    }

    @Override
    public void add(IWidget parent, IWidget child) {
    }

    @Override
    public void remove(IWidget parent, IWidget child) {
    }
}