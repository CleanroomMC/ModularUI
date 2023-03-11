package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListWidget<T, I extends IWidget, W extends ListWidget<T, I, W>> extends ScrollWidget<W> implements ILayoutWidget {

    protected final Function<T, I> valueToWidgetMapper;
    protected final Function<I, T> widgetToValueMapper;

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
        this.valueToWidgetMapper = valueToWidgetMapper;
        this.widgetToValueMapper = widgetToValueMapper;
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

    public boolean add(T value, int index) {
        return addChild(this.valueToWidgetMapper.apply(value), index);
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
    }

    @Override
    public void postLayoutWidgets() {
        int y = 0;
        int lastMargin = getArea().getPadding().top;
        for (IWidget widget : getChildren()) {
            y += Math.max(lastMargin, widget.getArea().getMargin().top);
            widget.getArea().ry = y;
            y += widget.getArea().height;
            lastMargin = widget.getArea().getMargin().bottom;
        }
        getScrollArea().scrollSize = y + Math.max(lastMargin, getArea().getPadding().bottom);
    }
}
