package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IValueWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListValueWidget<T, I extends IValueWidget<T>, W extends ListValueWidget<T, I, W>> extends ListWidget<I, W> {

    public List<T> getValues() {
        List<T> list = new ArrayList<>();
        for (I widget : getTypeChildren()) {
            list.add(widget.getWidgetValue());
        }
        return list;
    }

    public <V> W children(Iterable<V> values, Function<V, I> widgetCreator) {
        for (V value : values) {
            child(widgetCreator.apply(value));
        }
        return getThis();
    }
}
