package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.utils.BitHelper;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

public class DraggableWidget<W extends DraggableWidget<W>> extends Widget<W> implements IDraggable {

    private boolean moving = false;
    private int relativeClickX, relativeClickY;
    private final Area movingArea;

    public DraggableWidget() {
        this.movingArea = getArea().createCopy();
    }

    @Override
    public void drawMovingState(float partialTicks) {
        WidgetTree.drawTree(this, getContext(), true);
    }

    @Override
    public boolean onDragStart(int mouseButton) {
        if (mouseButton == 0) {
            this.relativeClickX = getContext().getMouseX() - getArea().x;
            this.relativeClickY = getContext().getMouseY() - getArea().y;
            this.movingArea.x = getContext().getAbsMouseX() - this.relativeClickX;
            this.movingArea.y = getContext().getAbsMouseY() - this.relativeClickY;
            return true;
        }
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            flex().top(getContext().getAbsMouseY() - this.relativeClickY)
                    .left(getContext().getAbsMouseX() - this.relativeClickX);
            this.movingArea.x = getArea().x;
            this.movingArea.y = getArea().y;
            WidgetTree.resize(this);
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        this.movingArea.x = getContext().getAbsMouseX() - this.relativeClickX;
        this.movingArea.y = getContext().getAbsMouseY() - this.relativeClickY;
    }

    @Override
    public @Nullable Area getMovingArea() {
        return this.movingArea;
    }

    @Override
    public boolean isMoving() {
        return this.moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
        setEnabled(!moving);
    }

    @Override
    public void getWidgetsBeforeApply(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (!isMoving() && getArea().isInside(getContext().localX(x), getContext().localY(y))) {
            widgets.add(this, viewports);
        }
    }

    @Override
    public void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (!isMoving() && hasChildren()) {
            IViewport.getChildrenAt(this, viewports, widgets, x, y);
        }
    }

    @Override
    public void apply(IViewportStack stack, int context) {
        if (BitHelper.hasAnyBits(context, IViewport.DRAGGABLE | IViewport.COLLECT_WIDGETS)) {
            stack.pushViewport(this, getMovingArea());
            if (isMoving()) {
                stack.translate(-this.movingArea.x + getContext().globalX(getArea().x), -this.movingArea.y + getContext().globalY(getArea().y));
            }
        }
    }

    @Override
    public void unapply(IViewportStack stack, int context) {
        if (BitHelper.hasNone(context, IViewport.START_DRAGGING) && BitHelper.hasAnyBits(context, IViewport.DRAGGABLE | IViewport.COLLECT_WIDGETS)) {
            stack.popViewport(this);
        }
    }
}
