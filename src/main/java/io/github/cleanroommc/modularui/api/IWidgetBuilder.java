package io.github.cleanroommc.modularui.api;

import io.github.cleanroommc.modularui.api.math.Alignment;
import io.github.cleanroommc.modularui.api.math.Size;
import io.github.cleanroommc.modularui.drawable.IDrawable;
import io.github.cleanroommc.modularui.widget.Widget;

public interface IWidgetBuilder<T extends IWidgetBuilder<T>> {

    void addWidgetInternal(Widget widget);

    default T widget(Widget widget) {
        addWidgetInternal(widget);
        return (T) this;
    }

    default T drawable(IDrawable drawable, Alignment alignment) {
        addWidgetInternal(drawable.asWidget()
                .setAlignment(alignment));
        return (T) this;
    }

    default T drawable(IDrawable drawable, Alignment alignment, Size size) {
        addWidgetInternal(drawable.asWidget()
                .setAlignment(alignment)
                .setSize(size));
        return (T) this;
    }
}
