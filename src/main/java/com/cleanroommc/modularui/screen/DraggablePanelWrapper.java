package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.widget.WidgetTree;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DraggablePanelWrapper implements IDraggable {

    private final ModularPanel panel;
    private final Rectangle movingArea;
    private int relativeClickX, relativeClickY;
    private boolean moving;

    public DraggablePanelWrapper(ModularPanel panel) {
        this.panel = panel;
        this.movingArea = new Rectangle(panel.getArea());
    }

    @Override
    public void drawMovingState(float partialTicks) {
        GuiContext context = this.panel.getContext();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-this.panel.getArea().x, -this.panel.getArea().y, 0);
        GlStateManager.translate(context.getAbsMouseX() - this.relativeClickX, context.getAbsMouseY() - this.relativeClickY, 0);
        WidgetTree.drawTree(this.panel, this.panel.getContext(), true, partialTicks);
        GlStateManager.popMatrix();
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
            this.panel.flex().top(this.panel.getContext().getAbsMouseY() - this.relativeClickY)
                    .left(this.panel.getContext().getAbsMouseX() - this.relativeClickX);
            WidgetTree.resize(this.panel);
        }
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        this.movingArea.x = this.panel.getContext().getMouseX() - this.relativeClickX;
        this.movingArea.y = this.panel.getContext().getMouseY() - this.relativeClickY;
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
        this.panel.setEnabled(!moving);
    }
}
