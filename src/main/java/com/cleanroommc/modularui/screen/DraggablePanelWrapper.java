package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.widget.WidgetTree;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DraggablePanelWrapper implements IDraggable {

    private final ModularPanel panel;
    private final int relativeClickX, relativeClickY;
    private boolean moving;

    public DraggablePanelWrapper(ModularPanel panel, int relativeClickX, int relativeClickY) {
        this.panel = panel;
        this.relativeClickX = relativeClickX;
        this.relativeClickY = relativeClickY;
    }

    @Override
    public void drawMovingState(float partialTicks) {
        GuiContext context = this.panel.getContext();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-this.panel.getArea().x, -this.panel.getArea().y, 0);
        GlStateManager.translate(context.getAbsMouseX() - this.relativeClickX, context.getAbsMouseY() - this.relativeClickY, 0);
        WidgetTree.drawInternal(this.panel, this.panel.getContext(), true, partialTicks);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onDragStart(int button) {
        return button == 0;
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

    }

    @Override
    public @Nullable Rectangle getArea() {
        return this.panel.getArea();
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
