package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.widget.Widget;

public class ValueWidget<W extends ValueWidget<W, T>, T> extends Widget<W> implements IValueWidget<T> {

    private final T widgetValue;

    public ValueWidget(T widgetValue) {
        this.widgetValue = widgetValue;
    }

    @Override
    public T getWidgetValue() {
        return widgetValue;
    }
}
