package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IParentWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;

public class ScrollWidget<W extends ScrollWidget<W>> extends AbstractScrollWidget<IWidget, W> implements IParentWidget<IWidget, W> {

    public ScrollWidget() {
        super(null, null);
    }

    public ScrollWidget(VerticalScrollData data) {
        super(null, data);
    }

    public ScrollWidget(HorizontalScrollData data) {
        super(data, null);
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        return super.addChild(child, index);
    }
}
