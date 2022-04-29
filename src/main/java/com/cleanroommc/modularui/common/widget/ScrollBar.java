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
    private int handleClickOffset = -1;

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

    public int getVisibleSize() {
        if (scrollType == ScrollType.HORIZONTAL) {
            return horizontalScrollable.getVisibleWidth();
        } else if (scrollType == ScrollType.VERTICAL) {
            return verticalScrollable.getVisibleHeight();
        }
        return 0;
    }

    public int getActualSize() {
        if (scrollType == ScrollType.HORIZONTAL) {
            return horizontalScrollable.getActualWidth();
        } else if (scrollType == ScrollType.VERTICAL) {
            return verticalScrollable.getActualHeight();
        }
        return 0;
    }

    public boolean isActive() {
        int actualSize = getActualSize();
        return actualSize > 0 && actualSize > getVisibleSize();
    }

    @Override
    public void draw(float partialTicks) {
        if (isActive() && this.barTexture != null) {
            int size = calculateMainAxisSize();
            if (scrollType == ScrollType.HORIZONTAL) {
                float offset = horizontalScrollable.getHorizontalScrollOffset() / (float) (horizontalScrollable.getActualWidth());
                this.barTexture.draw(horizontalScrollable.getVisibleWidth() * offset, 0, size, horizontalScrollable.getHorizontalBarHeight(), partialTicks);
            } else if (scrollType == ScrollType.VERTICAL) {
                float offset = verticalScrollable.getVerticalScrollOffset() / (float) (verticalScrollable.getActualHeight());
                this.barTexture.draw(0, verticalScrollable.getVisibleHeight() * offset, verticalScrollable.getVerticalBarWidth(), size, partialTicks);
            }
        }
    }

    public int calculateMainAxisSize() {
        int actualSize = getActualSize();
        if (actualSize == 0) {
            return 1;
        }
        int size = getVisibleSize();
        return (int) Math.max(size / (double) actualSize * size, 6);
    }

    public void clampScrollOffset() {
        setScrollOffset(getScrollOffset());
    }

    public void setScrollOffsetOfCursor(float x) {
        int visible = getVisibleSize();
        setScrollOffset((int) (x - visible / 2f));
    }

    public void setScrollOffset(int offset) {
        offset = MathHelper.clamp(offset, 0, getActualSize() - getVisibleSize());
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
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        Pos2d relative = getContext().getCursor().getPos().subtract(getAbsolutePos());
        int barSize = calculateMainAxisSize();
        int actualSize = getActualSize();
        if (scrollType == ScrollType.HORIZONTAL) {
            float offset = horizontalScrollable.getHorizontalScrollOffset() / (float) (actualSize) * horizontalScrollable.getVisibleWidth();
            if (relative.x >= offset && relative.x <= offset + barSize) {
                this.handleClickOffset = (int) (relative.x - offset);
            } else {
                float newOffset = Math.max(0, (relative.x - barSize / 2f) / (float) horizontalScrollable.getVisibleWidth());
                setScrollOffset((int) (newOffset * actualSize));
            }
        } else if (scrollType == ScrollType.VERTICAL) {
            float offset = verticalScrollable.getVerticalScrollOffset() / (float) (actualSize) * verticalScrollable.getVisibleHeight();
            if (relative.y >= offset && relative.y <= offset + barSize) {
                this.handleClickOffset = (int) (relative.y - offset);
            } else {
                float newOffset = Math.max(0, (relative.y - barSize / 2f) / (float) verticalScrollable.getVisibleHeight());
                setScrollOffset((int) (newOffset * actualSize));
            }
        }
        return ClickResult.ACCEPT;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        if (this.handleClickOffset >= 0) {
            int actualSize = getActualSize();
            if (scrollType == ScrollType.HORIZONTAL) {
                int offset = getContext().getCursor().getX() - pos.x - this.handleClickOffset;
                float newOffset = Math.max(0, offset / (float) horizontalScrollable.getVisibleWidth());
                setScrollOffset((int) (newOffset * actualSize));
            } else if (scrollType == ScrollType.VERTICAL) {
                int offset = getContext().getCursor().getY() - pos.y - this.handleClickOffset;
                float newOffset = Math.max(0, offset / (float) verticalScrollable.getVisibleHeight());
                setScrollOffset((int) (newOffset * actualSize));
            }
        }
    }

    @Override
    public boolean onClickReleased(int buttonId) {
        if (this.handleClickOffset >= 0) {
            this.handleClickOffset = -1;
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseScroll(int direction) {
        setScrollOffset(getScrollOffset() - direction * 6);
        return true;
    }

    public ScrollBar setBarTexture(IDrawable barTexture) {
        this.barTexture = barTexture;
        return this;
    }
}
