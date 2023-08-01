package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.ScrollArea;
import com.cleanroommc.modularui.utils.ScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScrollWidget<W extends ScrollWidget<W>> extends ParentWidget<W> implements IViewport, Interactable {

    private final ScrollArea scroll = new ScrollArea();

    public ScrollWidget() {
        this(null, null);
    }

    public ScrollWidget(ScrollData data) {
        this(data, null);
    }

    public ScrollWidget(@Nullable ScrollData x, @Nullable ScrollData y) {
        super();
        this.scroll.setScrollData(x);
        this.scroll.setScrollData(y);
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
        return this.scroll;
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        stack.translate(-getScrollX(), -getScrollY());
    }

    @Override
    public void getSelfAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
        if (getArea().isInside(x, y) && !getScrollArea().isInsideScrollbarArea(x, y) && hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void resize() {
        super.resize();
        if (this.scroll.getScrollX() != null) {
            this.scroll.getScrollX().clamp(this.scroll);
        }
        if (this.scroll.getScrollY() != null) {
            this.scroll.getScrollY().clamp(this.scroll);
        }
    }

    @Override
    public boolean canHover() {
        return super.canHover() || this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
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
    public void onFrameUpdate() {
        this.scroll.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
    }

    @Override
    public void preDraw(GuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(this.scroll, context);
        }
    }

    @Override
    public void postDraw(GuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            this.scroll.drawScrollbar();
        }
    }

    public int getScrollX() {
        return this.scroll.getScrollX() != null ? this.scroll.getScrollX().scroll : 0;
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().scroll : 0;
    }
}
