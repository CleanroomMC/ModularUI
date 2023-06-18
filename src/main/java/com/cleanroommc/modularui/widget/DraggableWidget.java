package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;

public class DraggableWidget<W extends DraggableWidget<W>> extends Widget<W> implements IDraggable {

    private boolean moving = false;
    private int relativeClickX, relativeClickY;
    private final Area movingArea;
    private int realX, realY;

    public DraggableWidget() {
        this.movingArea = getArea().createCopy();
    }

    @Override
    public void drawMovingState(GuiContext context, float partialTicks) {
        WidgetTree.drawTree(this, context, true);
    }

    @Override
    public boolean onDragStart(int mouseButton) {
        if (mouseButton == 0) {
            this.realX = getContext().transformX(0, 0);
            this.realY = getContext().transformY(0, 0);
            this.movingArea.x = this.realX;
            this.movingArea.y = this.realY;
            this.relativeClickX = getContext().getAbsMouseX() - this.realX;
            this.relativeClickY = getContext().getAbsMouseY() - this.realY;
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
    public void getSelfAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
        if (!isMoving() && isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
        if (!isMoving() && hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        if (isMoving()) {
            // remove relative transformation
            stack.translate(-getArea().rx, -getArea().ry);
            // translate to current pos
            stack.translate(-this.realX, -this.realY);
            stack.translate(this.movingArea.x, this.movingArea.y);
        }
    }
}
