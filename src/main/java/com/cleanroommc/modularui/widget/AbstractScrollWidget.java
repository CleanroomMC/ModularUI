package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A scrollable parent widget. Children can be added.
 *
 * @param <I> type of children (in most cases just {@link IWidget})
 * @param <W> type of this widget
 */
public abstract class AbstractScrollWidget<I extends IWidget, W extends AbstractScrollWidget<I, W>> extends AbstractParentWidget<I, W> implements IViewport, Interactable {

    private final ScrollArea scroll = new ScrollArea();
    private boolean scrollXActive, scrollYActive;

    public AbstractScrollWidget(@Nullable HorizontalScrollData x, @Nullable VerticalScrollData y) {
        super();
        this.scroll.setScrollDataX(x);
        this.scroll.setScrollDataY(y);
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
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek(), getAdditionalHoverInfo(stack, x, y));
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (getArea().isInside(x, y) && !getScrollArea().isInsideScrollbarArea(x, y) && hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        if (onOpen) checkScrollbarActive(true);
        getScrollArea().getScrollPadding().scrollPaddingAll(0);
        applyAdditionalOffset(this.scroll.getScrollX());
        applyAdditionalOffset(this.scroll.getScrollY());
    }

    private void checkScrollbarActive(boolean onOpen) {
        boolean scrollYActive = this.scroll.getScrollY() != null && this.scroll.getScrollY().isScrollBarActive(getScrollArea());
        boolean scrollXActive = this.scroll.getScrollX() != null && this.scroll.getScrollX().isScrollBarActive(getScrollArea(), this.scrollYActive);
        if (!onOpen && (scrollYActive != this.scrollYActive || scrollXActive != this.scrollXActive)) {
            scheduleResize();
        }
        this.scrollXActive = scrollXActive;
        this.scrollYActive = scrollYActive;
    }

    private void applyAdditionalOffset(ScrollData data) {
        if (data != null && data.isScrollBarActive(getScrollArea())) {
            getScrollArea().getScrollPadding().scrollPadding(data.getAxis().getOther(), data.isOnAxisStart(), data.getThickness());
        }
    }

    @Override
    public boolean canHover() {
        return super.canHover() || this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        ModularGuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.STOP;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return this.scroll.mouseScroll(getContext());
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        this.scroll.mouseReleased(getContext());
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        checkScrollbarActive(false);
        this.scroll.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(this.scroll, context);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            this.scroll.drawScrollbar();
        }
    }

    public int getScrollX() {
        return this.scroll.getScrollX() != null ? this.scroll.getScrollX().getScroll() : 0;
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }
}
