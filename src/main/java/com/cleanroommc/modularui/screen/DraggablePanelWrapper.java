package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.utils.BitHelper;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;

public class DraggablePanelWrapper implements IDraggable {

    private final ModularPanel panel;
    private final Area movingArea;
    private int relativeClickX, relativeClickY;
    private boolean moving;
    private int shiftX, shiftY;

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
    public void apply(IViewportStack stack, int context) {
        if (BitHelper.hasAnyBits(context, IViewport.DRAGGABLE | IViewport.COLLECT_WIDGETS)) {
            stack.pushViewport(this, getMovingArea());
            if (isMoving()) {
                this.shiftX = -this.movingArea.x + this.panel.getContext().globalX(this.panel.getArea().x);
                this.shiftY = -this.movingArea.y + this.panel.getContext().globalY(this.panel.getArea().y);
            } else {
                this.shiftX = 0;
                this.shiftY = 0;
            }
            stack.shiftX(this.shiftX);
            stack.shiftY(this.shiftY);
        }
    }

    @Override
    public void unapply(IViewportStack stack, int context) {
        if (BitHelper.hasNone(context, IViewport.START_DRAGGING) && BitHelper.hasAnyBits(context, IViewport.DRAGGABLE | IViewport.COLLECT_WIDGETS)) {
            stack.popViewport(this);
            stack.shiftX(-this.shiftX);
            stack.shiftY(-this.shiftY);
            this.shiftX = 0;
            this.shiftY = 0;
        }
    }
}
