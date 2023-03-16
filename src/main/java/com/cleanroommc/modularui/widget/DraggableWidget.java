package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidgetList;
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
        WidgetTree.drawTree(this, getContext(), true, partialTicks);
    }

    @Override
    public boolean onDragStart(int mouseButton) {
        if (mouseButton == 0) {
            this.relativeClickX = getContext().getMouseX() - getContext().localX(getArea().x);
            this.relativeClickY = getContext().getMouseY() - getContext().localY(getArea().y);
            return true;
        }
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            flex().top(getContext().getAbsMouseY() - this.relativeClickY)
                    .left(getContext().getAbsMouseX() - this.relativeClickX);
            WidgetTree.resize(this);
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        this.movingArea.x = getContext().getMouseX() - this.relativeClickX;
        this.movingArea.y = getContext().getMouseY() - this.relativeClickY;
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
    public void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (isMoving()) return;
        if (getArea().isInside(x, y)) {
            widgets.add(this, viewports);
        }
        if (hasChildren()) {
            IViewport.getChildrenAt(this, viewports, widgets, x, y);
        }
    }

    @Override
    public void apply(IViewportStack stack) {
        if (isMoving()) {
            stack.pushViewport(getMovingArea());
            stack.shiftX(this.movingArea.x - getArea().x);
            stack.shiftY(this.movingArea.y - getArea().y);
        }
    }

    @Override
    public void unapply(IViewportStack stack) {
        if (isMoving()) {
            stack.popViewport();
            stack.shiftX(-this.movingArea.x + getArea().x);
            stack.shiftY(-this.movingArea.y + getArea().y);
        }
    }
}
