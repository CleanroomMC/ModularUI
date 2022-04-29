package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IWidgetBuilder;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.api.widget.scroll.IHorizontalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.IVerticalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.ScrollType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public class Scrollable extends Widget implements IWidgetBuilder<Scrollable>, IWidgetParent, Interactable, IHorizontalScrollable, IVerticalScrollable {

    private int xScroll = -1, yScroll = -1;
    private final List<Widget> children = new ArrayList<>();
    private final List<Widget> allChildren = new ArrayList<>();
    private Size actualSize = Size.ZERO;
    private int grabScrollX = -1, grabScrollY = -1;
    @UnknownNullability
    private ScrollBar horizontalScrollBar, verticalScrollBar;

    @Override
    public void setHorizontalScrollOffset(int offset) {
        if (!canScrollHorizontal() || !this.horizontalScrollBar.isActive()) {
            offset = 0;
        }
        if (this.xScroll != offset) {
            int dif = xScroll - offset;
            this.xScroll = offset;
            for (Widget widget : children) {
                widget.setPosSilent(widget.getPos().add(dif, 0));
                widget.setEnabled(intersects(widget));
            }
        }
    }

    @Override
    public int getHorizontalScrollOffset() {
        return xScroll;
    }

    @Override
    public int getVisibleWidth() {
        return size.width;
    }

    @Override
    public int getActualWidth() {
        return actualSize.width;
    }

    @Override
    public void setVerticalScrollOffset(int offset) {
        if (!canScrollVertical() || !this.verticalScrollBar.isActive()) {
            offset = 0;
        }
        if (this.yScroll != offset) {
            int dif = yScroll - offset;
            this.yScroll = offset;
            for (Widget widget : children) {
                widget.setPosSilent(widget.getPos().add(0, dif));
                widget.setEnabled(intersects(widget));
            }
        }
    }

    @Override
    public int getVerticalScrollOffset() {
        return this.yScroll;
    }

    @Override
    public int getVisibleHeight() {
        return size.height;
    }

    @Override
    public int getActualHeight() {
        return actualSize.height;
    }

    public boolean canScrollVertical() {
        return verticalScrollBar != null;
    }

    public boolean canScrollHorizontal() {
        return horizontalScrollBar != null;
    }

    @Override
    public void initChildren() {
        if (canScrollHorizontal()) {
            this.allChildren.add(horizontalScrollBar);
        }
        if (canScrollVertical()) {
            this.allChildren.add(verticalScrollBar);
        }
    }

    @Override
    public void onRebuild() {
        this.actualSize = MultiChildWidget.getSizeOf(children);
        if (this.xScroll < 0 || this.yScroll == 0) {
            setHorizontalScrollOffset(0);
            setVerticalScrollOffset(0);
            checkNeedsRebuild();
        }
    }

    @Override
    public void addWidgetInternal(Widget widget) {
        if (MultiChildWidget.checkChild(this, widget)) {
            this.children.add(widget);
            this.allChildren.add(widget);
        }
    }

    @Override
    public boolean childrenMustBeInBounds() {
        return true;
    }

    @Override
    public void drawChildren(float partialTicks) {
        GuiHelper.useScissor(pos.x, pos.y, size.width, size.height, () -> {
            IWidgetParent.super.drawChildren(partialTicks);
        });
    }

    @Override
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        this.grabScrollX = getContext().getMousePos().x;
        this.grabScrollY = getContext().getMousePos().y;
        return ClickResult.ACCEPT;
    }

    @Override
    public boolean onClickReleased(int buttonId) {
        if (this.grabScrollX >= 0 || this.grabScrollY >= 0) {
            this.grabScrollX = -1;
            this.grabScrollY = -1;
            return true;
        }
        return false;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        if (this.grabScrollX >= 0 && this.grabScrollY >= 0) {
            int dif = getContext().getMousePos().x - grabScrollX;
            if (dif != 0) {
                horizontalScrollBar.setScrollOffset(xScroll - dif);
                grabScrollX = getContext().getMousePos().x;
            }
            dif = getContext().getMousePos().y - grabScrollY;
            if (dif != 0) {
                verticalScrollBar.setScrollOffset(yScroll - dif);
                grabScrollY = getContext().getMousePos().y;
            }
        }
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (canScrollHorizontal() && (canScrollVertical() && Interactable.hasShiftDown()) || !canScrollVertical()) {
            horizontalScrollBar.onMouseScroll(direction);
        } else if (canScrollVertical()) {
            verticalScrollBar.onMouseScroll(direction);
        }
        return true;
    }

    @Override
    public List<Widget> getChildren() {
        return this.allChildren;
    }

    public Scrollable setHorizontalScroll(@Nullable ScrollBar scrollBar) {
        this.horizontalScrollBar = scrollBar;
        if (this.horizontalScrollBar != null) {
            this.horizontalScrollBar.setScrollType(ScrollType.HORIZONTAL, this, null);
        }
        return this;
    }

    public Scrollable setHorizontalScroll() {
        return setHorizontalScroll(new ScrollBar()
                .setBarTexture(new Rectangle().setColor(Color.WHITE.normal).setCornerRadius(1)));
    }

    public Scrollable setVerticalScroll(@Nullable ScrollBar scrollBar) {
        this.verticalScrollBar = scrollBar;
        if (this.verticalScrollBar != null) {
            this.verticalScrollBar.setScrollType(ScrollType.VERTICAL, null, this);
        }
        return this;
    }

    public Scrollable setVerticalScroll() {
        return setVerticalScroll(new ScrollBar()
                .setBarTexture(new Rectangle().setColor(Color.WHITE.normal).setCornerRadius(1)));
    }
}
