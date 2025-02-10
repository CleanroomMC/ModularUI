package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.Animator;
import com.cleanroommc.modularui.utils.Interpolation;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

public abstract class ScrollData {

    /**
     * Creates scroll data which handles scrolling and scroll bar. Scrollbar is 4 pixel thick
     * and will be at the end of the cross axis (bottom/right).
     *
     * @param axis      axis on which to scroll
     * @return new scroll data
     */
    public static ScrollData of(GuiAxis axis) {
        return of(axis, false, DEFAULT_THICKNESS);
    }

    /**
     * Creates scroll data which handles scrolling and scroll bar. Scrollbar is 4 pixel thick.
     *
     * @param axis      axis on which to scroll
     * @param axisStart if the scroll bar should be at the start of the cross axis (left/top)
     * @return new scroll data
     */
    public static ScrollData of(GuiAxis axis, boolean axisStart) {
        return of(axis, axisStart, DEFAULT_THICKNESS);
    }

    /**
     * Creates scroll data which handles scrolling and scroll bar.
     *
     * @param axis      axis on which to scroll
     * @param axisStart if the scroll bar should be at the start of the cross axis (left/top)
     * @param thickness cross axis thickness of the scroll bar in pixel
     * @return new scroll data
     */
    public static ScrollData of(GuiAxis axis, boolean axisStart, int thickness) {
        if (axis.isHorizontal()) return new HorizontalScrollData(axisStart, thickness);
        return new VerticalScrollData(axisStart, thickness);
    }

    public static final int DEFAULT_THICKNESS = 4;

    private final GuiAxis axis;
    private final boolean axisStart;
    private final int thickness;
    private int scrollSpeed = 30;
    private boolean cancelScrollEdge = true;

    private int scrollSize;
    private int scroll;
    protected boolean dragging;
    protected int clickOffset;

    private int animatingTo = 0;
    private final Animator scrollAnimator = new Animator(30, Interpolation.QUAD_OUT);

    protected ScrollData(GuiAxis axis, boolean axisStart, int thickness) {
        this.axis = axis;
        this.axisStart = axisStart;
        this.thickness = thickness <= 0 ? 4 : thickness;
    }

    public GuiAxis getAxis() {
        return this.axis;
    }

    public boolean isOnAxisStart() {
        return this.axisStart;
    }

    public int getThickness() {
        return this.thickness;
    }

    public int getScrollSpeed() {
        return this.scrollSpeed;
    }

    public void setScrollSpeed(int scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }

    public void setScrollSize(int scrollSize) {
        this.scrollSize = scrollSize;
    }

    public int getScroll() {
        return this.scroll;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public boolean isVertical() {
        return this.axis.isVertical();
    }

    public boolean isHorizontal() {
        return this.axis.isHorizontal();
    }

    /**
     * Determines if scrolling of widgets below should still be canceled if this scroll view
     * has hit the end and is currently not scrolling.
     * Most of the time this should be true
     *
     * @return true if scrolling should be canceled even when this view hit an edge
     */
    public boolean isCancelScrollEdge() {
        return cancelScrollEdge;
    }

    public void setCancelScrollEdge(boolean cancelScrollEdge) {
        this.cancelScrollEdge = cancelScrollEdge;
    }

    protected final int getRawVisibleSize(ScrollArea area) {
        return Math.max(0, getRawFullVisibleSize(area) - area.getPadding().getTotal(this.axis));
    }

    protected final int getRawFullVisibleSize(ScrollArea area) {
        return area.getSize(this.axis);
    }

    public final int getFullVisibleSize(ScrollArea area) {
        return getFullVisibleSize(area, false);
    }

    public final int getFullVisibleSize(ScrollArea area, boolean isOtherActive) {
        int s = getRawVisibleSize(area);
        ScrollData data = getOtherScrollData(area);
        if (data != null && (isOtherActive || data.isScrollBarActive(area, true))) {
            s -= data.getThickness();
        }
        return s;
    }

    public final int getVisibleSize(ScrollArea area) {
        return getVisibleSize(area, false);
    }

    public final int getVisibleSize(ScrollArea area, int fullVisibleSize) {
        return Math.max(0, fullVisibleSize - area.getPadding().getTotal(this.axis));
    }

    public final int getVisibleSize(ScrollArea area, boolean isOtherActive) {
        return getVisibleSize(area, getFullVisibleSize(area, isOtherActive));
    }

    public float getProgress(ScrollArea area, int mainAxisPos, int crossAxisPos) {
        float fullSize = (float) getFullVisibleSize(area);
        return (mainAxisPos - area.getPoint(this.axis) - clickOffset) / (fullSize - getScrollBarLength(area));
    }

    @Nullable
    public abstract ScrollData getOtherScrollData(ScrollArea area);

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public boolean clamp(ScrollArea area) {
        int size = getVisibleSize(area);

        int old = this.scroll;
        if (this.scrollSize <= size) {
            this.scroll = 0;
        } else {
            this.scroll = MathHelper.clamp(this.scroll, 0, this.scrollSize - size);
        }
        return old != this.scroll; // returns true if the area was clamped
    }

    public boolean scrollBy(ScrollArea area, int x) {
        this.scroll += x;
        return clamp(area);
    }

    /**
     * Scroll to the position in the scroll area
     */
    public boolean scrollTo(ScrollArea area, int x) {
        this.scroll = x;
        return clamp(area);
    }

    public void animateTo(ScrollArea area, int x) {
        this.scrollAnimator.setCallback(value -> {
            return scrollTo(area, (int) value); // stop animation once an edge is hit
        });
        this.scrollAnimator.setValueBounds(this.scroll, x);
        this.scrollAnimator.forward();
        this.animatingTo = x;
    }

    public final boolean isScrollBarActive(ScrollArea area) {
        return isScrollBarActive(area, false);
    }

    public final boolean isScrollBarActive(ScrollArea area, boolean isOtherActive) {
        int s = getRawVisibleSize(area);
        if (s < this.scrollSize) return true;
        ScrollData data = getOtherScrollData(area);
        if (data == null || s - data.getThickness() >= this.scrollSize) return false;
        if (isOtherActive || data.isScrollBarActive(area, true)) {
            s -= data.getThickness();
        }
        return s < this.scrollSize;
    }

    public final boolean isOtherScrollBarActive(ScrollArea area, boolean isSelfActive) {
        ScrollData data = getOtherScrollData(area);
        return data != null && data.isScrollBarActive(area, isSelfActive);
    }

    public int getScrollBarLength(ScrollArea area) {
        boolean isOtherActive = isOtherScrollBarActive(area, false);
        int length = (int) (getVisibleSize(area, isOtherActive) * getFullVisibleSize(area, isOtherActive) / (float) this.scrollSize);
        return Math.max(length, 4); // min length of 4
    }

    public abstract boolean isInsideScrollbarArea(ScrollArea area, int x, int y);

    public boolean isAnimating() {
        return this.scrollAnimator.isRunning();
    }

    public int getAnimationDirection() {
        if (!isAnimating()) return 0;
        return this.scrollAnimator.getMax() >= this.scrollAnimator.getMin() ? 1 : -1;
    }

    public int getAnimatingTo() {
        return this.animatingTo;
    }

    public int getScrollBarStart(ScrollArea area, int scrollBarLength, int fullVisibleSize) {
        return ((fullVisibleSize - scrollBarLength) * getScroll()) / (getScrollSize() - getVisibleSize(area, fullVisibleSize));
    }

    public int getScrollBarStart(ScrollArea area, int scrollBarLength, boolean isOtherActive) {
        return getScrollBarStart(area, scrollBarLength, getFullVisibleSize(area, isOtherActive));
    }

    @SideOnly(Side.CLIENT)
    public abstract void drawScrollbar(ScrollArea area);

    @SideOnly(Side.CLIENT)
    protected void drawScrollBar(int x, int y, int w, int h) {
        GuiDraw.drawRect(x, y, w, h, 0xffeeeeee);
        GuiDraw.drawRect(x + 1, y + 1, w - 1, h - 1, 0xff666666);
        GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, 0xffaaaaaa);
    }

    public boolean onMouseClicked(ScrollArea area, int mainAxisPos, int crossAxisPos, int button) {
        if (isOnAxisStart() ? crossAxisPos <= area.getPoint(this.axis.getOther()) + getThickness() : crossAxisPos >= area.getEndPoint(this.axis.getOther()) - getThickness()) {
            this.dragging = true;
            this.clickOffset = mainAxisPos;

            int scrollBarSize = getScrollBarLength(area);
            int start = getScrollBarStart(area, scrollBarSize, false);
            int areaStart = area.getPoint(this.axis);
            boolean clickInsideBar = mainAxisPos >= areaStart + start && mainAxisPos <= areaStart + start + scrollBarSize;

            if (clickInsideBar) {
                this.clickOffset = mainAxisPos - areaStart - start; // relative click position inside bar
            } else {
                this.clickOffset = scrollBarSize / 2; // assume click position in center of bar
            }

            return true;
        }
        return false;
    }
}
