package io.github.cleanroommc.modularui.api;

import io.github.cleanroommc.modularui.api.math.GuiArea;
import io.github.cleanroommc.modularui.widget.Widget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public interface IWidgetParent {

    GuiArea getArea();

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
}
