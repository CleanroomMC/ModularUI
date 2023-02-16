package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.screen.GuiContext;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DraggableWidget<W extends DraggableWidget<W>> extends Widget<W> implements IDraggable {

    private boolean moving = false;
    private int relativeClickX, relativeClickY;
    private final Rectangle movingArea;

    public DraggableWidget() {
        this.movingArea = new Rectangle(getArea());
    }

    @Override
    public void drawMovingState(float partialTicks) {
        GuiContext context = getContext();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-getArea().x, -getArea().y, 0);
        GlStateManager.translate(context.getAbsMouseX() - this.relativeClickX, context.getAbsMouseY() - this.relativeClickY, 0);
        WidgetTree.drawTree(this, getContext(), true, partialTicks);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onDragStart(int mouseButton) {
        if (mouseButton == 0) {
            this.relativeClickX = getContext().getMouseX() - getArea().x;
            this.relativeClickY = getContext().getMouseY() - getArea().y;
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
    public @Nullable Rectangle getMovingArea() {
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
}
