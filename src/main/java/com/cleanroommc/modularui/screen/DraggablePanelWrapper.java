package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;

public class DraggablePanelWrapper implements IDraggable {

    private final ModularPanel panel;
    private final Area movingArea;
    private int relativeClickX, relativeClickY;
    private boolean moving;

    public DraggablePanelWrapper(ModularPanel panel) {
        this.panel = panel;
        this.movingArea = panel.getArea().createCopy();
    }

    @Override
    public void drawMovingState(float partialTicks) {
        WidgetTree.drawTree(this.panel, this.panel.getContext(), true);
    }

    @Override
    public boolean onDragStart(int button) {
        if (button == 0) {
            this.relativeClickX = panel.getContext().getMouseX() - panel.getArea().x;
            this.relativeClickY = panel.getContext().getMouseY() - panel.getArea().y;
            this.movingArea.x = this.panel.getContext().getAbsMouseX() - this.relativeClickX;
            this.movingArea.y = this.panel.getContext().getAbsMouseY() - this.relativeClickY;
            return true;
        }
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            float y = this.panel.getContext().getAbsMouseY() - this.relativeClickY;
            float x = this.panel.getContext().getAbsMouseX() - this.relativeClickX;
            y = y / (this.panel.getScreen().getViewport().height - this.panel.getArea().height);
            x = x / (this.panel.getScreen().getViewport().width - this.panel.getArea().width);
            this.panel.flex().top(y, y)
                    .left(x, x);
            this.movingArea.x = this.panel.getArea().x;
            this.movingArea.y = this.panel.getArea().y;
            WidgetTree.resize(this.panel);
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        this.movingArea.x = this.panel.getContext().getAbsMouseX() - this.relativeClickX;
        this.movingArea.y = this.panel.getContext().getAbsMouseY() - this.relativeClickY;
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
        this.panel.setEnabled(!moving);
    }

    @Override
    public void transform(IViewportStack stack) {
        Area area = this.panel.getArea();
        stack.translate(area.rx, area.ry);
        if (isMoving()) {
            stack.translate(-area.x, -area.y);
            stack.translate(this.movingArea.x, this.movingArea.y);
        }
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        if (isMoving()) {
            stack.translate(-this.movingArea.x + this.panel.getContext().unTransformX(this.panel.getArea().x, this.panel.getArea().y), -this.movingArea.y + this.panel.getContext().unTransformY(this.panel.getArea().x, this.panel.getArea().y));
        }
    }
}
