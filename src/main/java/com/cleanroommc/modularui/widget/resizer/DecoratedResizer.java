package com.cleanroommc.modularui.widget.resizer;

import com.cleanroommc.modularui.api.IResizer;

public abstract class DecoratedResizer extends BaseResizer {

    public IResizer resizer;

    public DecoratedResizer(IResizer resizer) {
        this.resizer = resizer;
    }
}