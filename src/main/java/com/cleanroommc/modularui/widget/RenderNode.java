package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;

import java.util.List;

public class RenderNode implements WidgetNode<RenderNode> {

    private IWidget linkedWidget;
    private RenderNode parent;
    private List<RenderNode> children;

    @Override
    public IWidget getWidget() {
        return linkedWidget;
    }

    @Override
    public RenderNode getParent() {
        return parent;
    }

    @Override
    public List<RenderNode> getChildren() {
        return children;
    }
}
