package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;

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
        super(new VerticalScrollData());
        this.valueToWidgetMapper = Objects.requireNonNull(valueToWidgetMapper);
        this.widgetToValueMapper = Objects.requireNonNull(widgetToValueMapper);
        this.scrollData = getScrollArea().getScrollY();
    }

    public static <T, V extends IValueWidget<T> & IWidget, W extends ListWidget<T, V, W>> ListWidget<T, V, W> of(Function<T, V> valueToWidgetMapper) {
        return new ListWidget<>(valueToWidgetMapper, IValueWidget::getWidgetValue);
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
    public void onResized() {
        if (this.keepScrollBarInArea) return;
        if (this.scrollData.isVertical()) {
            getArea().width += this.scrollData.getThickness();
        } else {
            getArea().height += this.scrollData.getThickness();
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
        GuiAxis axis = this.scrollData.getAxis();
        int p = getArea().getPadding().getStart(axis);
        for (IWidget widget : getChildren()) {
            if (axis.isVertical() ?
                    widget.getFlex().hasYPos() || !widget.resizer().isHeightCalculated() :
                    widget.getFlex().hasXPos() || !widget.resizer().isWidthCalculated()) {
                continue;
            }
            p += widget.getArea().getMargin().getStart(axis);
            widget.getArea().setRelativePoint(axis, p);
            p += widget.getArea().getSize(axis) + widget.getArea().getMargin().getEnd(axis);
            if (axis.isHorizontal()) {
                widget.resizer().setXResized(true);
            } else {
                widget.resizer().setYResized(true);
            }
        }
        getScrollData().setScrollSize(p + getArea().getPadding().getEnd(axis));
    }

    public ScrollData getScrollData() {
        return this.scrollData;
    }

    public W scrollDirection(ScrollData data) {
        if (this.scrollData.getAxis() != data.getAxis()) {
            this.scrollData = data;
            getScrollArea().removeScrollData();
            getScrollArea().setScrollData(this.scrollData);
        }
        return getThis();
    }

    public W keepScrollBarInArea() {
        this.keepScrollBarInArea = true;
        return getThis();
    }
}
