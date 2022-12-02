package com.cleanroommc.modularui.widget.resizer;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IParentResizer;
import com.cleanroommc.modularui.utils.Area;

public class ChildResizer extends DecoratedResizer {

    public IParentResizer parent;
    public IWidget element;
    private int x;
    private int y;
    private int w;
    private int h;

    public ChildResizer(IParentResizer parent, IWidget element) {
        super(null);
        this.parent = parent;
        this.element = element;
    }

    @Override
    public void apply(Area area) {
        if (this.resizer != null) {
            this.resizer.apply(area);
        }

        this.parent.apply(area, this.resizer, this);
        this.x = area.x;
        this.y = area.y;
        this.w = area.w;
        this.h = area.h;
    }

    @Override
    public void postApply(Area area) {
        if (this.resizer != null) {
            this.resizer.postApply(area);
        }
    }

    @Override
    public void add(IWidget parent, IWidget child) {
        if (this.resizer != null) {
            this.resizer.add(parent, child);
        }
    }

    @Override
    public void remove(IWidget parent, IWidget child) {
        if (this.resizer != null) {
            this.resizer.remove(parent, child);
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getW() {
        return this.w;
    }

    @Override
    public int getH() {
        return this.h;
    }
}
