package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
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
    public void drawMovingState(GuiContext context, float partialTicks) {
        context.pushMatrix();
        transform(context);
        WidgetTree.drawTree(this.panel, context, true);
        context.popMatrix();
    }

    @Override
    public boolean onDragStart(int button) {
        if (button == 0) {
            GuiContext context = this.panel.getContext();
            this.movingArea.x = context.transformX(0, 0);
            this.movingArea.y = context.transformY(0, 0);
            this.relativeClickX = context.getAbsMouseX() - this.movingArea.x;
            this.relativeClickY = context.getAbsMouseY() - this.movingArea.y;
            return true;
        }
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            float y = this.panel.getContext().getAbsMouseY() - this.relativeClickY;
            float x = this.panel.getContext().getAbsMouseX() - this.relativeClickX;
            y = y / (this.panel.getScreen().getScreenArea().height - this.panel.getArea().height);
            x = x / (this.panel.getScreen().getScreenArea().width - this.panel.getArea().width);
            this.panel.flex().resetPosition();
            this.panel.flex().relativeToScreen();
            this.panel.flex().topRelAnchor(y, y)
                    .leftRelAnchor(x, x);
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
        if (isMoving()) {
            Area area = this.panel.getArea();
            stack.translate(-area.x, -area.y);
            stack.translate(this.movingArea.x, this.movingArea.y);
        }
    }
}
