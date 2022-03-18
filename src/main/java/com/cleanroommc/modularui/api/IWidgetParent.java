package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.widget.Widget;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IWidgetParent {

    Size getSize();

    Pos2d getAbsolutePos();

    Pos2d getPos();

    List<Widget> getChildren();

    ModularUIContext getContext();

    /**
     * If autoSized is true, this method is called after all children are build.
     *
     * @return the desired size for this widget. Null will do nothing
     */
    @Nullable
    default Size determineSize() {
        return null;
    }

    /**
     * Called during rebuild.
     * {@link Widget#isAutoPositioned()} must be checked for each child!!!
     */
    default void layoutChildren() {
    }

    static boolean forEachByLayer(List<Widget> parent, Function<Widget, Boolean> consumer) {
        return forEachByLayer(new Wrapper(parent), consumer);
    }

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

    class Wrapper implements IWidgetParent {
        private final List<Widget> children;

        public Wrapper(List<Widget> children) {
            this.children = children;
        }

        @Override
        public Size getSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pos2d getAbsolutePos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pos2d getPos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Widget> getChildren() {
            return children;
        }

        @Override
        public ModularUIContext getContext() {
            throw new UnsupportedOperationException();
        }
    }
}
