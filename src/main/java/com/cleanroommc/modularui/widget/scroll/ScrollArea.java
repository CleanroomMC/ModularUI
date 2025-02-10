package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Scrollable area
 * <p>
 * This class is responsible for storing information for scrollable one
 * directional objects.
 */
public class ScrollArea extends Area {

    private HorizontalScrollData scrollX;
    private VerticalScrollData scrollY;
    private int scrollBarBackgroundColor = Color.withAlpha(Color.BLACK.main, 0.25f);

    public ScrollArea(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public ScrollArea() {}

    public void setScrollData(ScrollData data) {
        if (data instanceof HorizontalScrollData scrollData) {
            this.scrollX = scrollData;
        } else if (data instanceof VerticalScrollData scrollData) {
            this.scrollY = scrollData;
        }
    }

    public void removeScrollData() {
        this.scrollX = null;
        this.scrollY = null;
    }

    public void setScrollDataX(HorizontalScrollData scrollX) {
        this.scrollX = scrollX;
    }

    public void setScrollDataY(VerticalScrollData data) {
        this.scrollY = data;
    }

    public HorizontalScrollData getScrollX() {
        return this.scrollX;
    }

    public VerticalScrollData getScrollY() {
        return this.scrollY;
    }

    public ScrollData getScrollData(GuiAxis axis) {
        return axis.isVertical() ? this.scrollY : this.scrollX;
    }

    /* GUI code for easier manipulations */

    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(GuiContext context) {
        return this.mouseClicked(context.getAbsMouseX(), context.getAbsMouseY());
    }

    /**
     * This method should be invoked to register dragging
     */
    public boolean mouseClicked(int x, int y) {
        if (this.scrollX != null && this.scrollX.isInsideScrollbarArea(this, x, y)) {
            return this.scrollX.onMouseClicked(this, x, y, 0);
        } else if (this.scrollY != null && this.scrollY.isInsideScrollbarArea(this, x, y)) {
            return this.scrollY.onMouseClicked(this, y, x, 0);
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean mouseScroll(GuiContext context) {
        return this.mouseScroll(context.getAbsMouseX(), context.getAbsMouseY(), context.getMouseWheel(), GuiScreen.isShiftKeyDown());
    }

    /**
     * This method should be invoked when mouse wheel is scrolling
     */
    public boolean mouseScroll(int x, int y, int scroll, boolean shift) {
        if (!isInside(x, y)) {
            // not hovering TODO: this shouldnt be required
            return false;
        }

        ScrollData data;
        if (this.scrollX != null) {
            data = this.scrollY == null || shift ? this.scrollX : this.scrollY;
        } else if (this.scrollY != null) {
            data = this.scrollY;
        } else {
            // no scroll data present -> cant be scrolled
            return false;
        }

        int scrollAmount = (int) Math.copySign(data.getScrollSpeed(), scroll);
        int scrollTo;
        if (data.isAnimating()) {
            scrollTo = data.getAnimatingTo() - scrollAmount;
        } else {
            scrollTo = data.getScroll() - scrollAmount;
        }

        // simulate scroll to determine whether event should be canceled
        int oldScroll = data.getScroll();
        data.scrollTo(this, scrollTo);
        boolean changed = data.getScroll() != oldScroll;
        data.scrollTo(this, oldScroll);
        if (changed) {
            data.animateTo(this, scrollTo);
            return true;
        }
        return data.isCancelScrollEdge();
    }

    @SideOnly(Side.CLIENT)
    public void mouseReleased(GuiContext context) {
        this.mouseReleased(context.getAbsMouseX(), context.getAbsMouseY());
    }

    /**
     * When mouse button gets released
     */
    public void mouseReleased(int x, int y) {
        if (this.scrollX != null) {
            this.scrollX.dragging = false;
            this.scrollX.clickOffset = 0;
        }
        if (this.scrollY != null) {
            this.scrollY.dragging = false;
            this.scrollY.clickOffset = 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public void drag(GuiContext context) {
        this.drag(context.getMouseX(), context.getMouseY());
    }

    /**
     * This should be invoked in a drawing or and update method. It's
     * responsible for scrolling through this view when dragging.
     */
    public void drag(int x, int y) {
        ScrollData data;
        float progress;
        if (this.scrollX != null && this.scrollX.dragging) {
            data = this.scrollX;
            progress = data.getProgress(this, x, y);
        } else if (this.scrollY != null && this.scrollY.dragging) {
            data = this.scrollY;
            progress = data.getProgress(this, y, x);
        } else {
            return;
        }
        progress = MathUtils.clamp(progress, 0f, 1f);
        data.scrollTo(this, (int) (progress * (data.getScrollSize() - data.getVisibleSize(this) + data.getThickness())));
    }

    public boolean isInsideScrollbarArea(int x, int y) {
        if (!isInside(x, y)) {
            return false;
        }
        if (this.scrollX != null && this.scrollX.isInsideScrollbarArea(this, x, y)) {
            return true;
        }
        return this.scrollY != null && this.scrollY.isInsideScrollbarArea(this, x, y);
    }

    public boolean isScrollBarXActive() {
        return this.scrollX != null && this.scrollX.isScrollBarActive(this);
    }

    public boolean isScrollBarYActive() {
        return this.scrollY != null && this.scrollY.isScrollBarActive(this);
    }

    public int getScrollBarBackgroundColor() {
        return this.scrollBarBackgroundColor;
    }

    public void setScrollBarBackgroundColor(int scrollBarBackgroundColor) {
        this.scrollBarBackgroundColor = scrollBarBackgroundColor;
    }

    /**
     * This method is responsible for drawing a scroll bar
     */
    @SideOnly(Side.CLIENT)
    public void drawScrollbar() {
        boolean isXActive = false; // micro optimisation
        if (this.scrollX != null && this.scrollX.isScrollBarActive(this, false)) {
            isXActive = true;
            this.scrollX.drawScrollbar(this);
        }
        if (this.scrollY != null && this.scrollY.isScrollBarActive(this, isXActive)) {
            this.scrollY.drawScrollbar(this);
        }
    }
}