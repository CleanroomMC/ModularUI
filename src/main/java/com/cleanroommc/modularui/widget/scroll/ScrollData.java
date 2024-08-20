package com.cleanroommc.modularui.widget.scroll;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.Animator;
import com.cleanroommc.modularui.utils.Interpolation;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public abstract class ScrollData {

    private final GuiAxis axis;
    private final boolean axisStart;
    private final int thickness;
    private int scrollSpeed = 30;

    private int scrollSize;
    private int scroll;
    protected boolean dragging;

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

    public final int getVisibleSize(ScrollArea area, boolean isOtherActive) {
        return Math.max(0, getFullVisibleSize(area, isOtherActive) - area.getPadding().getTotal(this.axis));
    }

    public abstract float getProgress(ScrollArea area, int x, int y);

    @Nullable
    public abstract ScrollData getOtherScrollData(ScrollArea area);

    /**
     * Clamp scroll to the bounds of the scroll size;
     */
    public void clamp(ScrollArea area) {
        int size = getVisibleSize(area);

        if (this.scrollSize <= size) {
            this.scroll = 0;
        } else {
            this.scroll = Mth.clamp(this.scroll, 0, this.scrollSize - size);
        }
    }

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
        this.scrollAnimator.setCallback(value -> scrollTo(area, (int) value));
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
        return (int) (getVisibleSize(area, isOtherActive) * getFullVisibleSize(area, isOtherActive) / (float) this.scrollSize);
    }

    public abstract boolean isInsideScrollbarArea(ScrollArea area, int x, int y);

    public boolean isAnimating() {
        return this.scrollAnimator.isRunning();
    }

    public int getAnimatingTo() {
        return this.animatingTo;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void drawScrollbar(ScrollArea area);

    @OnlyIn(Dist.CLIENT)
    protected void drawScrollBar(int x, int y, int w, int h) {
        GuiDraw.drawRect(x, y, w, h, 0xffeeeeee);
        GuiDraw.drawRect(x + 1, y + 1, w - 1, h - 1, 0xff666666);
        GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, 0xffaaaaaa);
    }

    public abstract boolean onMouseClicked(ScrollArea area, int x, int y, int button);
}
