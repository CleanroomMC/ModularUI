package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.drawable.IDrawable;
import com.cleanroommc.modularui.slot.BaseSlot;
import com.cleanroommc.modularui.widget.SlotWidget;
import com.cleanroommc.modularui.widget.Widget;

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

    default T slot(BaseSlot slot) {
        addWidgetInternal(new SlotWidget(slot));
        return (T) this;
    }
}
