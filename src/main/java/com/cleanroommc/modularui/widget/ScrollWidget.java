package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.*;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.ScrollArea;
import com.cleanroommc.modularui.utils.ScrollDirection;
import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class ScrollWidget<W extends ScrollWidget<W>> extends ParentWidget<W> implements IViewport, Interactable {

    private final ScrollArea scroll;

    public ScrollWidget() {
        this(ScrollDirection.VERTICAL);
    }

    public ScrollWidget(ScrollDirection direction) {
        super();
        this.scroll = new ScrollArea(0);
        this.scroll.direction = direction;
        this.scroll.scrollSpeed = 20;
        listenGuiAction((IGuiAction.MouseReleased) mouseButton -> {
            this.scroll.mouseReleased(getContext());
            return false;
        });
    }

    @Override
    public Area getArea() {
        return this.scroll;
    }

    public ScrollArea getScrollArea() {
        return scroll;
    }

    public W cancelScrollEdge() {
        this.scroll.cancelScrollEdge = true;
        return getThis();
    }

    @Override
    public void apply(IViewportStack stack) {
        stack.pushViewport(getArea());

        if (this.scroll.direction == ScrollDirection.VERTICAL) {
            stack.shiftY(this.scroll.scroll);
        } else {
            stack.shiftX(this.scroll.scroll);
        }
    }

    @Override
    public void unapply(IViewportStack stack) {
        if (this.scroll.direction == ScrollDirection.VERTICAL) {
            stack.shiftY(-this.scroll.scroll);
        } else {
            stack.shiftX(-this.scroll.scroll);
        }

        stack.popViewport();
    }

    @Override
    public void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
        if (!getArea().isInside(x, y)) {
            return;
        }
        widgets.add(this, viewports);
        if (hasChildren()) {
            IViewport.getChildrenAt(this, viewports, widgets, x, y);
        }
    }

    @Override
    public void resize() {
        super.resize();

        this.scroll.clamp();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        GuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.STOP;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        return this.scroll.mouseScroll(getContext());
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        this.scroll.mouseReleased(getContext());
        return false;
    }

    @Override
    public void draw(float partialTicks) {
        super.draw(partialTicks);
        GuiContext context = getContext();

        this.scroll.drag(context.getAbsMouseX(), context.getAbsMouseY());

        GuiDraw.scissorTransformed(this.scroll.x + this.scroll.getPadding().left,
                this.scroll.y + this.scroll.getPadding().top,
                this.scroll.width - this.scroll.getPadding().horizontal(),
                this.scroll.height - this.scroll.getPadding().vertical(), context);

        GlStateManager.pushMatrix();

        /* Translate the contents using OpenGL (scroll) */
        if (this.scroll.direction == ScrollDirection.VERTICAL) {
            GlStateManager.translate(0, -this.scroll.scroll, 0);
        } else {
            GlStateManager.translate(-this.scroll.scroll, 0, 0);
        }

        this.preDraw(context);
        this.postDraw(context);

        GlStateManager.popMatrix();
        GuiDraw.unscissor(context);
        this.scroll.drawScrollbar();
    }

    protected void preDraw(GuiContext context) {
    }

    protected void postDraw(GuiContext context) {
    }
}
