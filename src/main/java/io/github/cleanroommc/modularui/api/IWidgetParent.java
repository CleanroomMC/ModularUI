package io.github.cleanroommc.modularui.api;

import io.github.cleanroommc.modularui.api.math.Pos2d;
import io.github.cleanroommc.modularui.api.math.Size;
import io.github.cleanroommc.modularui.widget.Widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IWidgetParent {

    Size getSize();

    Pos2d getAbsolutePos();

    Pos2d getPos();

    List<Widget> getChildren();

    static boolean forEachByLayer(IWidgetParent parent, Function<Widget, Boolean> consumer) {
        LinkedList<IWidgetParent> stack = new LinkedList<>();
        stack.addLast(parent);
        while (!stack.isEmpty()) {
            IWidgetParent parent1 = stack.pollFirst();
            for (Widget child : parent1.getChildren()) {
                if (child instanceof IWidgetParent) {
                    stack.addLast((IWidgetParent) child);
                }
                if (consumer.apply(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean forEachByLayer(IWidgetParent parent, Consumer<Widget> consumer) {
        return forEachByLayer(parent, widget -> {
            consumer.accept(widget);
            return false;
        });
    }
}
