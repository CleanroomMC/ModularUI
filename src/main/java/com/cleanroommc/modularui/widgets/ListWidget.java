package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.ScrollData;
import com.cleanroommc.modularui.utils.ScrollDirection;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.GuiAxis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListWidget<T, I extends IWidget, W extends ListWidget<T, I, W>> extends ScrollWidget<W> implements ILayoutWidget {

    protected final Function<T, I> valueToWidgetMapper;
    protected final Function<I, T> widgetToValueMapper;

    private ScrollData scrollData;
    private boolean keepScrollBarInArea = false;

    public ListWidget() {
        this(v -> null, w -> null);
    }

    public ListWidget(Collection<IWidget> widgets) {
        this();
        for (IWidget widget : widgets) {
            child(widget);
        }
    }

    public ListWidget(Function<T, I> valueToWidgetMapper, Function<I, T> widgetToValueMapper) {
        this.valueToWidgetMapper = Objects.requireNonNull(valueToWidgetMapper);
        this.widgetToValueMapper = Objects.requireNonNull(widgetToValueMapper);
        this.scrollData = new ScrollData(ScrollDirection.VERTICAL);
        getScrollArea().setScrollData(this.scrollData);
    }

    public static <T, V extends IValueWidget<T> & IWidget, W extends ListWidget<T, V, W>> ListWidget<T, V, W> of(Function<T, V> valueToWidgetMapper) {
        return new ListWidget<>(valueToWidgetMapper, IValueWidget::getValue);
    }

    public static <T, I extends IWidget, W extends ListWidget<T, I, W>> ListWidget<T, I, W> builder(List<T> list, Function<T, I> creator) {
        Map<T, I> map = new Object2ObjectOpenHashMap<>();
        Map<I, T> map_reverse = new Object2ObjectOpenHashMap<>();
        ListWidget<T, I, W> listWidget = new ListWidget<>(map::get, map_reverse::get);
        for (T t : list) {
            I widget = creator.apply(t);
            map.put(t, widget);
            map_reverse.put(widget, t);
            listWidget.child(widget);
        }
        return listWidget;
    }

    @Override
    public void resize() {
        super.resize();
        if (this.keepScrollBarInArea) return;
        if (this.scrollData.direction == ScrollDirection.VERTICAL) {
            getArea().width += this.scrollData.getScrollbarThickness();
        } else {
            getArea().height += this.scrollData.getScrollbarThickness();
        }
    }

    public boolean add(T value, int index) {
        if (addChild(this.valueToWidgetMapper.apply(value), index)) {
            if (isValid()) {
                WidgetTree.resize(this);
            }
            return true;
        }
        return false;
    }

    @Nullable
    public IWidget remove(T value) {
        IWidget widget = this.valueToWidgetMapper.apply(value);
        if (remove(widget)) {
            return widget;
        }
        return null;
    }

    public List<T> getValues() {
        return getChildren().stream()
                .map(widget -> this.widgetToValueMapper.apply((I) widget))
                .collect(Collectors.toList());
    }

    @Override
    public void layoutWidgets() {
        GuiAxis axis = this.scrollData.direction.axis;
        int p = 0;
        int lastMargin = getArea().getPadding().getStart(axis);
        for (IWidget widget : getChildren()) {
            if (widget.getFlex() != null && (axis.isVertical() ? widget.getFlex().hasYPos() : widget.getFlex().hasXPos())) {
                continue;
            }
            p += Math.max(lastMargin, widget.getArea().getMargin().getStart(axis));
            widget.getArea().setRelativePoint(axis, p);
            p += widget.getArea().getSize(axis);
            lastMargin = widget.getArea().getMargin().getEnd(axis);
        }
        getScrollData().scrollSize = p + Math.max(lastMargin, getArea().getPadding().getEnd(axis));
    }

    public ScrollData getScrollData() {
        return scrollData;
    }

    public W scrollDirection(ScrollDirection direction) {
        if (this.scrollData.direction != direction) {
            this.scrollData = this.scrollData.copyWith(direction);
            getScrollArea().setScrollDataY(null);
            getScrollArea().setScrollDataX(null);
            getScrollArea().setScrollData(this.scrollData);
        }
        return getThis();
    }

    public W keepScrollBarInArea() {
        this.keepScrollBarInArea = true;
        return getThis();
    }
}
