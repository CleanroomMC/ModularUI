package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.utils.Area;
import com.cleanroommc.modularui.widget.resizer.ChildResizer;

public interface IParentResizer {

    void apply(Area area, IResizer resizer, ChildResizer child);
}
