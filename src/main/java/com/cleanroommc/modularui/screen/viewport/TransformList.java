package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewportTransformation;
import com.cleanroommc.modularui.widget.sizer.Area;

import java.util.ArrayList;

public class TransformList extends ArrayList<IViewportTransformation> implements IViewportTransformation {

    public TransformList(IViewportTransformation transformation) {
        add(transformation);
    }

    @Override
    public int transformX(int x, Area area, boolean toLocal) {
        if (toLocal) {
            for (IViewportTransformation transformation : this) {
                x = transformation.transformX(x, area, true);
            }
        } else {
            for (int i = size() - 1; i >= 0; i--) {
                x = get(i).transformX(x, area, false);
            }
        }
        return x;
    }

    @Override
    public int transformY(int y, Area area, boolean toLocal) {
        if (toLocal) {
            for (IViewportTransformation transformation : this) {
                y = transformation.transformY(y, area, true);
            }
        } else {
            for (int i = size() - 1; i >= 0; i--) {
                y = get(i).transformY(y, area, false);
            }
        }
        return y;
    }

    @Override
    public void applyOpenGlTransformation() {
        for (IViewportTransformation transformation : this) {
            transformation.applyOpenGlTransformation();
        }
    }

    @Override
    public void unapplyOpenGlTransformation() {
        for (int j = size() - 1; j >= 0; j--) {
            get(j).unapplyOpenGlTransformation();
        }
    }
}
