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
        GuiContext context = this.panel.getContext();
        WidgetTree.drawTree(this.panel, this.panel.getContext(), true, partialTicks);
    }

    @Override
    public boolean onDragStart(int button) {
        if (button == 0) {
            this.relativeClickX = panel.getContext().getMouseX() - panel.getArea().x;
            this.relativeClickY = panel.getContext().getMouseY() - panel.getArea().y;
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
            WidgetTree.resize(this.panel);
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        this.movingArea.x = this.panel.getContext().getMouseX() - this.relativeClickX;
        this.movingArea.y = this.panel.getContext().getMouseY() - this.relativeClickY;
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
    public void apply(IViewportStack stack) {
        if (isMoving()) {
            stack.pushViewport(getMovingArea());
            stack.shiftX(-this.movingArea.x + this.panel.getArea().x);
            stack.shiftY(-this.movingArea.y + this.panel.getArea().y);
        }
    }

    @Override
    public void unapply(IViewportStack stack) {
        if (isMoving()) {
            stack.popViewport();
            stack.shiftX(this.movingArea.x - this.panel.getArea().x);
            stack.shiftY(this.movingArea.y - this.panel.getArea().y);
        }
    }
}
