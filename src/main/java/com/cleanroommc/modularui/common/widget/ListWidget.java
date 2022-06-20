package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.drawable.GuiHelper;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.api.widget.scroll.IVerticalScrollable;
import com.cleanroommc.modularui.api.widget.scroll.ScrollType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ListWidget extends MultiChildWidget implements Interactable, IVerticalScrollable {

    private int scrollOffset = 0;
    private int totalHeight = 0;
    @Nullable
    private ScrollBar scrollBar;
    private int maxHeight = -1;
    private List<Widget> allChildren = new ArrayList<>();

    public static <T> ListWidget builder(List<T> list, BiFunction<T, Integer, Widget> widgetCreator) {
        ListWidget listWidget = new ListWidget();
        int i = 0;
        for (T t : list) {
            Widget widget = Objects.requireNonNull(widgetCreator.apply(t, i++), "ListWidget creator produced a null child! This is forbidden!");
            listWidget.addChild(widget);
        }
        return listWidget;
    }

    public static ListWidget builder(int size, Function<Integer, Widget> widgetCreator) {
        ListWidget listWidget = new ListWidget();
        for (int i = 0; i < size; i++) {
            Widget widget = Objects.requireNonNull(widgetCreator.apply(i), "ListWidget creator produced a null child! This is forbidden!");
            listWidget.addChild(widget);
        }
        return listWidget;
    }

    public ListWidget() {
        setScrollBar(ScrollBar.defaultTextScrollBar());
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        Size superSize = super.determineSize(maxWidth, maxHeight);
        return this.maxHeight > 0 ? new Size(superSize, this.maxHeight) : new Size(superSize, maxHeight);
    }

    @Override
    public void onInit() {
        this.allChildren.clear();
        this.allChildren.addAll(this.children);
        if (this.scrollBar != null) {
            this.allChildren.add(this.scrollBar);
        }
    }

    @Override
    public void onRebuild() {
        this.allChildren.clear();
        this.allChildren.addAll(this.children);
        if (this.scrollBar != null) {
            this.allChildren.add(this.scrollBar);
        }
        this.totalHeight = 0;
        for (Widget child : this.children) {
            this.totalHeight += child.getSize().height;
        }
    }

    @Override
    public void layoutChildren(int maxWidth, int maxHeight) {
        int y = this.scrollOffset;
        for (Widget widget : children) {
            widget.setPosSilent(new Pos2d(0, y));
            y += widget.getSize().height;
        }
    }

    @Override
    public void drawChildren(float partialTicks) {
        GuiHelper.useScissor(pos.x, pos.y, size.width, size.height, () -> super.drawChildren(partialTicks));
    }

    @Override
    public boolean childrenMustBeInBounds() {
        return true;
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (scrollBar != null) {
            return scrollBar.onMouseScroll(direction);
        }
        return false;
    }

    @Override
    public List<Widget> getChildren() {
        return allChildren;
    }

    @Override
    public void setVerticalScrollOffset(int offset) {
        if (this.scrollOffset != offset) {
            int dif = this.scrollOffset - offset;
            this.scrollOffset = offset;
            for (Widget widget : children) {
                widget.setPosSilent(widget.getPos().add(0, dif));
                widget.setEnabled(intersects(widget));
            }
        }
    }

    @Override
    public int getVerticalScrollOffset() {
        return scrollOffset;
    }

    @Override
    public int getVisibleHeight() {
        return size.height;
    }

    @Override
    public int getActualHeight() {
        return totalHeight;
    }

    @Override
    public ListWidget addChild(Widget widget) {
        return (ListWidget) super.addChild(widget);
    }

    public ListWidget setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public ListWidget setScrollBar(@Nullable ScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        if (this.scrollBar != null) {
            this.scrollBar.setScrollType(ScrollType.VERTICAL, null, this);
        }
        return this;
    }
}
