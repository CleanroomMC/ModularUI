package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.api.widget.scroll.IHorizontalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.IVerticalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.ScrollType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScrollBar extends Widget implements Interactable {

    private ScrollType scrollType;
    private IVerticalScrollable verticalScrollable;
    private IHorizontalScrollable horizontalScrollable;
    private IDrawable barTexture = IDrawable.EMPTY;
    private int actualSize = 0;
    private boolean dragHandle = false;

    public void setScrollType(ScrollType scrollType, @Nullable IHorizontalScrollable horizontalScrollable, @Nullable IVerticalScrollable verticalScrollable) {
        this.scrollType = scrollType;
        this.verticalScrollable = verticalScrollable;
        this.horizontalScrollable = horizontalScrollable;
    }

    @Override
    public void onInit() {
        if ((scrollType == ScrollType.VERTICAL && verticalScrollable == null) || (scrollType == ScrollType.HORIZONTAL && horizontalScrollable == null)) {
            throw new IllegalStateException("Scroll bar was not properly initialized");
        }
        if (isAutoSized()) {
            setSizeProvider((screenSize, window, parent) -> {
                if (scrollType == ScrollType.HORIZONTAL) {
                    return new Size(horizontalScrollable.getVisibleWidth(), horizontalScrollable.getHorizontalBarHeight());
                } else if (scrollType == ScrollType.VERTICAL) {
                    return new Size(verticalScrollable.getVerticalBarWidth(), verticalScrollable.getVisibleHeight());
                }
                return Size.ZERO;
            });
        }
        if (isAutoPositioned()) {
            setPosProvider((screenSize, window, parent) -> {
                if (scrollType == ScrollType.HORIZONTAL) {
                    return new Pos2d(0, parent.getSize().height - horizontalScrollable.getHorizontalBarHeight());
                } else if (scrollType == ScrollType.VERTICAL) {
                    return new Pos2d(parent.getSize().width - verticalScrollable.getVerticalBarWidth(), 0);
                }
                return Pos2d.ZERO;
            });
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (scrollType == ScrollType.HORIZONTAL) {
            return new Size(horizontalScrollable.getVisibleWidth(), horizontalScrollable.getHorizontalBarHeight());
        } else if (scrollType == ScrollType.VERTICAL) {
            return new Size(verticalScrollable.getVerticalBarWidth(), verticalScrollable.getVisibleHeight());
        }
        return super.determineSize(maxWidth, maxHeight);
    }

    @Override
    public void onScreenUpdate() {
        super.onScreenUpdate();
        if (scrollType == ScrollType.HORIZONTAL) {
            this.actualSize = horizontalScrollable.getActualWidth();
        } else if (scrollType == ScrollType.VERTICAL) {
            this.actualSize = verticalScrollable.getActualHeight();
        }
    }

    public int getVisibleSize() {
        if (scrollType == ScrollType.HORIZONTAL) {
            return horizontalScrollable.getVisibleWidth();
        } else if (scrollType == ScrollType.VERTICAL) {
            return verticalScrollable.getVisibleHeight();
        }
        return 0;
    }

    public boolean isActive() {
        return this.actualSize > 0 && this.actualSize > getVisibleSize();
    }

    @Override
    public void draw(float partialTicks) {
        if (isActive() && this.barTexture != null) {
            int size = calculateMainAxisSize();
            if (scrollType == ScrollType.HORIZONTAL) {
                float offset = horizontalScrollable.getHorizontalScrollOffset() / (float) (this.actualSize);
                this.barTexture.draw(horizontalScrollable.getVisibleWidth() * offset, 0, size, horizontalScrollable.getHorizontalBarHeight(), partialTicks);
            } else if (scrollType == ScrollType.VERTICAL) {
                float offset = verticalScrollable.getVerticalScrollOffset() / (float) (this.actualSize);
                this.barTexture.draw(0, verticalScrollable.getVisibleHeight() * offset, verticalScrollable.getVerticalBarWidth(), size, partialTicks);
            }
        }
    }

    public int calculateMainAxisSize() {
        if (this.actualSize == 0) {
            return 1;
        }
        int size = getVisibleSize();
        return Math.max(size / this.actualSize * size, 6);
    }

    public void setScrollOffset(int offset) {
        offset = MathHelper.clamp(offset, 0, this.actualSize);
        if (scrollType == ScrollType.HORIZONTAL) {
            horizontalScrollable.setHorizontalScrollOffset(offset);
        } else if (scrollType == ScrollType.VERTICAL) {
            verticalScrollable.setVerticalScrollOffset(offset);
        }
    }

    public int getScrollOffset() {
        if (scrollType == ScrollType.HORIZONTAL) {
            return horizontalScrollable.getHorizontalScrollOffset();
        } else if (scrollType == ScrollType.VERTICAL) {
            return verticalScrollable.getVerticalScrollOffset();
        }
        return 0;
    }

    public int getBarSize() {
        if (scrollType == ScrollType.HORIZONTAL) {
            return horizontalScrollable.getHorizontalBarHeight();
        } else if (scrollType == ScrollType.VERTICAL) {
            return verticalScrollable.getVerticalBarWidth();
        }
        return 0;
    }

    @Override
    public boolean onClick(int buttonId, boolean doubleClick) {
        Pos2d relative = getContext().getCursor().getPos().subtract(getAbsolutePos());
        int barSize = calculateMainAxisSize();
        if (scrollType == ScrollType.HORIZONTAL) {
            float offset = horizontalScrollable.getHorizontalScrollOffset() / (float) (this.actualSize) * horizontalScrollable.getVisibleWidth();
            if (relative.x >= offset && relative.x <= offset + calculateMainAxisSize()) {
                this.dragHandle = true;
            } else {
                float newOffset = Math.max(0, relative.x / (float) horizontalScrollable.getVisibleWidth() - barSize / 2f);
                setScrollOffset((int) (newOffset * this.actualSize));
            }
        } else if (scrollType == ScrollType.VERTICAL) {
            float offset = verticalScrollable.getVerticalScrollOffset() / (float) (this.actualSize) * verticalScrollable.getVisibleHeight();
            if (relative.y >= offset && relative.y <= offset + calculateMainAxisSize()) {
                this.dragHandle = true;
            } else {
                float newOffset = Math.max(0, relative.y / (float) verticalScrollable.getVisibleHeight() - barSize / 2f);
                setScrollOffset((int) (newOffset * this.actualSize));
            }
        }
        return true;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        if (this.dragHandle) {

        }
    }

    @Override
    public boolean onClickReleased(int buttonId) {
        if (this.dragHandle) {
            this.dragHandle = false;
            return true;
        }
        return false;
    }

    public void setOffsetOf(Pos2d clickPos) {
        if (scrollType == ScrollType.HORIZONTAL) {

        } else if (scrollType == ScrollType.VERTICAL) {

        }
    }

    @Override
    public void onHoverMouseScroll(int direction) {
        setScrollOffset(getScrollOffset() + direction);
    }

    public ScrollBar setBarTexture(IDrawable barTexture) {
        this.barTexture = barTexture;
        return this;
    }
}
