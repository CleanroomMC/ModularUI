package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SingleChildWidget<W extends SingleChildWidget<W>> extends Widget<W> {

    private IWidget child;
    private List<IWidget> list = Collections.emptyList();

    public IWidget getChild() {
        return child;
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return this.list;
    }

    private void updateList() {
        this.list = this.child == null ? Collections.emptyList() : Collections.singletonList(this.child);
    }

    public W child(IWidget child) {
        if (child == this || this.child == child) {
            return getThis();
        }
        if (this.child != null) {
            this.child.dispose();
        }
        this.child = child;
        updateList();
        if (child != null && isValid()) {
            child.initialise(this, true);
            scheduleResize();
        }
        return getThis();
    }
}
