package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;

public class Center extends SingleChildWidget {

    public Center(Widget widget) {
        setChild(widget);
    }

    @Override
    public void onRebuildPre() {
        getChild().setPos(Pos2d.ZERO);
    }

    @Override
    public void onRebuildPost() {
        setSize(getChild().getSize());
        setPos(Alignment.Center.getAlignedPos(getParent().getSize(), getSize()));
    }
}
