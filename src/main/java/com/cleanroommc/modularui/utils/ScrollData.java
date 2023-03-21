package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.drawable.GuiDraw;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ScrollData {

    public final ScrollDirection direction;

    /**
     * Size of an element/item in the scroll area
     */
    public int scrollItemSize;

    /**
     * Size of the scrolling area
     */
    public int scrollSize;

    /**
     * Scroll position
     */
    public int scroll;

    /**
     * Whether this scroll area gets dragged
     */
    public boolean dragging;

    /**
     * Speed of how fast shit's scrolling
     */
    public int scrollSpeed = ModularUIConfig.defaultScrollSpeed;

    /**
     * Whether the scrollbar should be on opposite side (default is right
     * for vertical and bottom for horizontal)
     */
    public boolean opposite;

    /**
     * Width of scroll bar
     */
    public int scrollbarWidth = -1;

    /**
     * Whether this scroll area should cancel mouse events when mouse scroll
     * reaches the end
     */
    public boolean cancelScrollEdge = false;

    private Animator scrollAnimator;

    public ScrollData(ScrollDirection direction) {
        this.direction = direction;
    }

    public int getScrollbarThickness() {
        // TODO
        return this.scrollbarWidth <= 0 ? /*ModularUI.scrollbarWidth.get()*/ 4 : this.scrollbarWidth;
    }

    public void setSize(int items) {
        this.scrollSize = items * this.scrollItemSize;
    }

    /**
     * Scroll by relative amount
     */
    public void scrollBy(ScrollArea area, int x) {
        this.scroll += x;
        this.clamp(area);
    }

    /**
     * Scroll to the position in the scroll area
     */
    public void scrollTo(ScrollArea area, int x) {
        this.scroll = x;
        this.clamp(area);
    }

    public void animateTo(ScrollArea area, int x) {
        if (scrollAnimator == null) {
            scrollAnimator = new Animator(30, Interpolation.QUAD_OUT)
                    .setCallback(value -> scrollTo(area, (int) value));
        }

        scrollAnimator.setValueBounds(this.scroll, x);
        scrollAnimator.forward();
    }

    public void scrollIntoView(ScrollArea area, int x) {
        this.scrollIntoView(area, x, this.scrollItemSize, 0);
    }

    public void scrollIntoView(ScrollArea area, int x, int bottomOffset) {
        this.scrollIntoView(area, x, bottomOffset, 0);
    }

    public void scrollIntoView(ScrollArea area, int x, int bottomOffset, int topOffset) {
        if (this.scroll + topOffset > x) {
            this.scrollTo(area, x - topOffset);
        } else if (x > this.scroll + this.direction.getSide(area) - bottomOffset) {
            this.scrollTo(area, x - this.direction.getSide(area) + bottomOffset);
        }
    }

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public void clamp(ScrollArea area) {
        int size = this.direction.getSide(area);

        if (this.scrollSize <= size) {
            this.scroll = 0;
        } else {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - size);
        }
    }

    /**
     * Get index of the cursor based on the {@link #scrollItemSize}.
     */
    public int getIndex(ScrollArea area, int x, int y) {
        int axis = this.direction.getScroll(area, x, y);
        int index = axis / this.scrollItemSize;

        if (axis < 0) {
            return -1;
        } else if (axis > this.scrollSize) {
            return -2;
        }

        return index > this.scrollSize / this.scrollItemSize ? -1 : index;
    }

    /**
     * Calculates scroll bar's height
     */
    public int getScrollBarLength(ScrollArea area) {
        return (int) (this.direction.getSide(area) * this.direction.getFullSide(area) / (float) this.scrollSize);
    }

    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!area.isInside(x, y) || !isScrollBarActive(area)) {
            return false;
        }
        int scrollbar = this.getScrollbarThickness();
        if (this.direction == ScrollDirection.HORIZONTAL) {
            if (area.getScrollY() != null && area.getScrollY().isScrollBarActive(area, true)) {
                int thickness = area.getScrollY().getScrollbarThickness();
                if (area.getScrollY().opposite ? x < area.x + thickness : x >= area.ex() - thickness) {
                    return false;
                }
            }
            return this.opposite ? y >= area.y && y < area.y + scrollbar : y >= area.ey() - scrollbar && y < area.ey();
        }
        if (this.direction == ScrollDirection.VERTICAL) {
            if (area.getScrollX() != null && area.getScrollX().isScrollBarActive(area, true)) {
                int thickness = area.getScrollX().getScrollbarThickness();
                if (area.getScrollX().opposite ? y < area.y + thickness : y >= area.ey() - thickness) {
                    return false;
                }
            }
            return this.opposite ? x >= area.x && x < area.x + scrollbar : x >= area.ex() - scrollbar && x < area.ex();
        }
        return false;
    }

    public boolean isScrollBarActive(ScrollArea area) {
        return isScrollBarActive(area, false);
    }

    public boolean isScrollBarActive(ScrollArea area, boolean isOtherActive) {
        return this.scrollSize > this.direction.getSide(area, isOtherActive);
    }

    @SideOnly(Side.CLIENT)
    public void drawScrollbar(ScrollArea area) {
        int side = this.direction.getSide(area);

        if (!isScrollBarActive(area)) {
            return;
        }

        int scrollbar = this.getScrollbarThickness();
        int h = this.getScrollBarLength(area);
        int x = 0;
        int y = 0;
        int rx;
        int ry;

        if (this.direction == ScrollDirection.VERTICAL) {
            x = this.opposite ? 0 : area.width - scrollbar;
            rx = x + scrollbar;
            ry = area.height;
        } else {
            y = this.opposite ? 0 : area.height - scrollbar;
            ry = y + scrollbar;
            rx = area.width;
        }
        GuiDraw.drawRect(x, y, rx, ry, area.getScrollBarBackgroundColor());

        if (this.direction == ScrollDirection.VERTICAL) {
            y = ((this.direction.getFullSide(area) - h) * this.scroll) / (this.scrollSize - side);

            if (area.getScrollX() != null && area.getScrollX().isScrollBarActive(area, true) && area.getScrollX().opposite) {
                y += area.getScrollX().getScrollbarThickness();
            }
            ry = y + h;
        } else {
            y = this.opposite ? 0 : area.height - scrollbar;
            x = ((this.direction.getFullSide(area) - h) * this.scroll) / (this.scrollSize - side);
            if (area.getScrollY() != null && area.getScrollY().isScrollBarActive(area, true) && area.getScrollY().opposite) {
                x += area.getScrollY().getScrollbarThickness();
            }
            rx = x + h;
            ry = y + scrollbar;
        }

        // TODO
        if (/*ModularUI.scrollbarFlat.get()*/false) {
            Gui.drawRect(x, y, rx, ry, -6250336);
        } else {
            int color = 0;//ModularUI.scrollbarShadow.get();

            GuiDraw.drawDropShadow(x, y, rx, ry, 5, color, Color.withAlpha(color, 0));

            Gui.drawRect(x, y, rx, ry, 0xffeeeeee);
            Gui.drawRect(x + 1, y + 1, rx, ry, 0xff666666);
            Gui.drawRect(x + 1, y + 1, rx - 1, ry - 1, 0xffaaaaaa);
        }
    }

    public ScrollData copyWith(ScrollDirection direction) {
        ScrollData data = new ScrollData(direction);
        data.scroll = scroll;
        data.dragging = dragging;
        data.cancelScrollEdge = cancelScrollEdge;
        data.opposite = opposite;
        data.scrollAnimator = scrollAnimator;
        data.scrollbarWidth = scrollbarWidth;
        data.scrollItemSize = scrollItemSize;
        data.scrollSize = scrollSize;
        data.scrollSpeed = scrollSpeed;
        return data;
    }
}
