package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.screen.DraggablePanelWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class DragHandle extends Widget<DragHandle> implements IDraggable {

    private IDraggable parentDraggable;

    @Override
    public void onInit() {
        IWidget parent = getParent();
        while (!(parent instanceof ModularPanel)) {
            if (parent instanceof IDraggable) {
                this.parentDraggable = (IDraggable) parent;
                return;
            }
            parent = parent.getParent();
        }
        if (((ModularPanel) parent).isDraggable()) {
            parentDraggable = new DraggablePanelWrapper((ModularPanel) parent);
        }
    }

    @Override
    public void drawMovingState(float partialTicks) {
        if (this.parentDraggable != null) {
            this.parentDraggable.drawMovingState(partialTicks);
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
    public boolean canDropHere(int x, int y, @Nullable IGuiElement widget) {
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
    public void apply(IViewportStack stack, int context) {
        if (this.parentDraggable != null) {
            this.parentDraggable.apply(stack, context);
        }
    }

    @Override
    public void unapply(IViewportStack stack, int context) {
        if (this.parentDraggable != null) {
            this.parentDraggable.unapply(stack, context);
        }
    }

    @Override
    public void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (this.parentDraggable != null) {
            this.parentDraggable.getWidgetsAt(viewports, widgets, x, y);
        }
    }

    @Override
    public void getWidgetsBeforeApply(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (this.parentDraggable != null) {
            this.parentDraggable.getWidgetsBeforeApply(viewports, widgets, x, y);
        }
    }
}
