package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.GuiAxis;
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

    private ScrollData scrollX, scrollY;
    private int scrollBarBackgroundColor = Color.withAlpha(Color.BLACK.normal, 0.25f);

    public ScrollArea(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public ScrollArea() {
    }

    public void setScrollData(ScrollData data) {
        if (data != null) {
            if (data.direction == ScrollDirection.HORIZONTAL) {
                this.scrollX = data;
            } else {
                this.scrollY = data;
            }
        }
    }

    public void setScrollDataX(ScrollData scrollX) {
        this.scrollX = scrollX;
    }

    public void setScrollDataY(ScrollData data) {
        this.scrollY = data;
    }

    public ScrollData getScrollX() {
        return this.scrollX;
    }

    public ScrollData getScrollY() {
        return this.scrollY;
    }

    public ScrollData getScrollData(GuiAxis axis) {
        return axis.isVertical() ? this.scrollY : this.scrollX;
    }

    public ScrollData getScrollData(ScrollDirection axis) {
        return getScrollData(axis.axis);
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
        ScrollData data;
        if (this.scrollX != null && this.scrollX.isInsideScrollbarArea(this, x, y)) {
            data = this.scrollX;
        } else if (this.scrollY != null && this.scrollY.isInsideScrollbarArea(this, x, y)) {
            data = this.scrollY;
        } else {
            return false;
        }

        data.dragging = true;
        int scrollbar = data.getScrollbarThickness();
        if (data.opposite) {
            return data.direction == ScrollDirection.VERTICAL ? x <= this.x + scrollbar : y <= this.y + scrollbar;
        } else {
            return data.direction == ScrollDirection.VERTICAL ? x >= this.ex() - scrollbar : y >= this.ey() - scrollbar;
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
        if (!isInside(x, y)) return false;

        ScrollData data;
        if (this.scrollX != null) {
            data = this.scrollY == null || shift ? this.scrollX : this.scrollY;
        } else if (this.scrollY != null) {
            data = this.scrollY;
        } else {
            return false;
        }

        int scrollAmount = (int) Math.copySign(data.scrollSpeed, scroll);
        int scrollTo;
        if (data.isAnimating()) {
            scrollTo = data.getAnimatingTo() - scrollAmount;
        } else {
            scrollTo = data.scroll - scrollAmount;
        }

        // simulate scroll to determine whether event should be canceled
        int oldScroll = data.scroll;
        data.scrollTo(this, scrollTo);
        boolean changed = data.scroll != oldScroll;
        data.scrollTo(this, oldScroll);
        if (changed) {
            data.animateTo(this, scrollTo);
            return true;
        }

        return data.cancelScrollEdge;
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
        }
        if (this.scrollY != null) {
            this.scrollY.dragging = false;
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
        if (this.scrollX != null && this.scrollX.dragging) {
            data = this.scrollX;
        } else if (this.scrollY != null && this.scrollY.dragging) {
            data = this.scrollY;
        } else {
            return;
        }
        float progress = data.direction.getProgress(this, x, y);
        data.animateTo(this, (int) (progress * (data.scrollSize - data.direction.getSide(this) + data.getScrollbarThickness())));
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
        return this.scrollX != null && this.scrollX.scrollSize > this.scrollX.direction.getSide(this);
    }

    public boolean isScrollBarYActive() {
        return this.scrollY != null && this.scrollY.scrollSize > this.scrollY.direction.getSide(this);
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
        if (this.scrollX != null) {
            this.scrollX.drawScrollbar(this);
        }
        if (this.scrollY != null) {
            this.scrollY.drawScrollbar(this);
        }
    }
}