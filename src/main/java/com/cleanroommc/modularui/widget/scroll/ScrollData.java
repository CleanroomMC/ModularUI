package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Scrollbar;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.MathUtils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

public abstract class ScrollData {

    /**
     * Creates scroll data which handles scrolling and scroll bar. Scrollbar is 4 pixel thick
     * and will be at the end of the cross axis (bottom/right).
     *
     * @param axis axis on which to scroll
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

    public static final int DEFAULT_THICKNESS = -1;

    private final GuiAxis axis;
    private final boolean axisStart;
    private final int thickness;
    private int fallbackThickness = -1;
    private int scrollSpeed = 30;
    private boolean cancelScrollEdge = true;
    private IDrawable scrollbar;

    private int scrollSize;
    private int scroll;
    protected boolean dragging;
    protected int clickOffset;

    private int animatingTo = 0;
    private final Animator scrollAnimator = new Animator()
            .duration(500)
            .curve(Interpolation.QUAD_OUT);

    protected ScrollData(GuiAxis axis, boolean axisStart, int thickness) {
        this.axis = axis;
        this.axisStart = axisStart;
        this.thickness = thickness > 0 ? Math.max(2, thickness) : -1;
    }

    public GuiAxis getAxis() {
        return this.axis;
    }

    public boolean isOnAxisStart() {
        return this.axisStart;
    }

    public int getThickness() {
        return this.thickness > 0 ? this.thickness : this.fallbackThickness;
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

    public final int getMinLength() {
        return getThickness() + 1; // make sure bar is always longer than thicker
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
        // the scroll area doesn't contribute to the visible size in this case
        return Math.max(0, getRawFullVisibleSize(area) - area.getPadding().getTotal(this.axis) + area.getScrollPadding().getTotalScrollPadding(this.axis));
    }

    protected final int getRawFullVisibleSize(ScrollArea area) {
        return area.getSize(this.axis);
    }

    public final int getFullVisibleSize(ScrollArea area) {
        return getFullVisibleSize(area, false);
    }

    public final int getFullVisibleSize(ScrollArea area, boolean isOtherActive) {
        int s = getRawFullVisibleSize(area);
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
        return (mainAxisPos - clickOffset) / (fullSize - getScrollBarLength(area));
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
            this.scroll = MathUtils.clamp(this.scroll, 0, this.scrollSize - size);
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
        this.scrollAnimator.bounds(this.scroll, x).onUpdate(value -> {
            if (scrollTo(area, (int) value)) {
                this.scrollAnimator.stop(false); // stop animation once an edge is hit
            }
        });
        this.scrollAnimator.reset();
        this.scrollAnimator.animate();
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
        return Math.max(length, getMinLength()); // min length of 4
    }

    public abstract boolean isInsideScrollbarArea(ScrollArea area, int x, int y);

    public boolean isAnimating() {
        return this.scrollAnimator.isAnimating();
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

    protected abstract int getFallbackThickness(WidgetTheme widgetTheme);

    protected final void applyWidgetTheme(WidgetTheme widgetTheme) {
        this.fallbackThickness = Math.max(2, getFallbackThickness(widgetTheme));
    }

    public ScrollData texture(IDrawable drawable) {
        this.scrollbar = drawable;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public abstract void drawScrollbar(ScrollArea area, ModularGuiContext context, WidgetTheme widgetTheme);

    @SideOnly(Side.CLIENT)
    protected void drawScrollBar(ModularGuiContext context, int x, int y, int w, int h, WidgetTheme widgetTheme) {
        IDrawable drawable = this.scrollbar != null ? this.scrollbar : widgetTheme.getBackground();
        if (drawable == null) drawable = Scrollbar.DEFAULT;
        drawable.draw(context, x, y, w, h, widgetTheme);
    }

    public boolean onMouseClicked(ScrollArea area, int mainAxisPos, int crossAxisPos, int button) {
        if (isOnAxisStart() ? crossAxisPos <= getThickness() : crossAxisPos >= area.getSize(this.axis.getOther()) - getThickness()) {
            this.dragging = true;
            this.clickOffset = mainAxisPos;

            int scrollBarSize = getScrollBarLength(area);
            int start = getScrollBarStart(area, scrollBarSize, false);
            boolean clickInsideBar = mainAxisPos >= start && mainAxisPos <= start + scrollBarSize;

            if (clickInsideBar) {
                this.clickOffset = mainAxisPos - start; // relative click position inside bar
            } else {
                this.clickOffset = scrollBarSize / 2; // assume click position in center of bar
            }

            return true;
        }
        return false;
    }
}
