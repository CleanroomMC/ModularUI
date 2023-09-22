package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.screen.viewport.TransformationMatrix;
import org.jetbrains.annotations.Nullable;

public class HoveredWidgetList {

    private final ObjectList<LocatedWidget> delegate;

    public HoveredWidgetList(ObjectList<LocatedWidget> delegate) {
        this.delegate = delegate;
    }

    public void add(IWidget widget, TransformationMatrix viewports) {
        this.delegate.addFirst(new LocatedWidget(widget, viewports));
    }

    @Nullable
    public IWidget peek() {
        return isEmpty() ? null : this.delegate.getFirst().getElement();
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public int size() {
        return this.delegate.size();
    }
}
