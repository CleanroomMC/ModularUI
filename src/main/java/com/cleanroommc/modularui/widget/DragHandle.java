package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.DraggablePanelWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.Nullable;

public class DragHandle extends Widget<DragHandle> implements IDraggable, IViewport {

    private IDraggable parentDraggable;

    @Override
    public void onInit() {
        IWidget parent = getParent();
        while (!(parent instanceof ModularPanel)) {
            if (parent instanceof IDraggable draggable) {
                this.parentDraggable = draggable;
                return;
            }
            parent = parent.getParent();
        }
        if (((ModularPanel) parent).isDraggable()) {
            this.parentDraggable = new DraggablePanelWrapper((ModularPanel) parent);
        }
    }

    @Override
    public void drawMovingState(ModularGuiContext context, float partialTicks) {
        if (this.parentDraggable != null) {
            this.parentDraggable.drawMovingState(context, partialTicks);
        }
    }

    @Override
    public boolean onDragStart(int button) {
        return this.parentDraggable != null && this.parentDraggable.onDragStart(button);
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (this.parentDraggable != null) {
            this.parentDraggable.onDragEnd(successful);
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        if (this.parentDraggable != null) {
            this.parentDraggable.onDrag(mouseButton, timeSinceLastClick);
        }
    }

    @Override
    public boolean canDropHere(int x, int y, @Nullable IWidget widget) {
        return this.parentDraggable != null && this.parentDraggable.canDropHere(x, y, widget);
    }

    @Override
    public @Nullable Area getMovingArea() {
        Area.SHARED.reset();
        return this.parentDraggable != null ? this.parentDraggable.getMovingArea() : Area.SHARED;
    }

    @Override
    public boolean isMoving() {
        return this.parentDraggable != null && this.parentDraggable.isMoving();
    }

    @Override
    public void setMoving(boolean moving) {
        if (this.parentDraggable != null) {
            this.parentDraggable.setMoving(moving);
        }
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
    }

    // TODO
    /*@Override
    public void transformChildren(IViewportStack stack) {
        if (this.parentDraggable != null) {
            this.parentDraggable.transformChildren(stack);
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (this.parentDraggable != null) {
            this.parentDraggable.getWidgetsAt(stack, widgets, x, y);
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (this.parentDraggable != null) {
            this.parentDraggable.getSelfAt(stack, widgets, x, y);
        }
    }*/
}
