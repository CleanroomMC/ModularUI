package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widget.WidgetTree;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SortableListWidget<T, I extends SortableListWidget.Item<T>> extends ListWidget<T, I, SortableListWidget<T, I>> {

    public static <T, I extends SortableListWidget.Item<T>> SortableListWidget<T, I> sortableBuilder(List<T> list, Function<T, I> builder) {
        Map<T, I> map = new Object2ObjectOpenHashMap<>();
        SortableListWidget<T, I> sortableListWidget = new SortableListWidget<>(map::get);
        for (T t : list) {
            I item = builder.apply(t);
            map.put(t, item);
            sortableListWidget.child(item);
        }
        return sortableListWidget;
    }

    private Consumer<List<T>> onChange;
    private int timeSinceLastMove = 0;

    public SortableListWidget(Function<T, I> valueToWidgetMapper) {
        super(valueToWidgetMapper, Item::getValue);
        flex().startDefaultMode()
                .width(80)
                .endDefaultMode();
    }

    @Override
    public void onInit() {
        assignIndexes();
    }

    @Override
    public void onFrameUpdate() {
        this.timeSinceLastMove++;
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        T value = this.widgetToValueMapper.apply((I) child);
        if (child != this.valueToWidgetMapper.apply(value)) {
            throw new IllegalArgumentException();
        }
        ((Item<T>) child).listWidget = this;
        return super.addChild(child, index);
    }

    public void moveTo(int from, int to) {
        if (this.timeSinceLastMove < 3) return;
        if (from < 0 || to < 0 || from == to) throw new IllegalArgumentException();
        Item<?> child = (Item<?>) getChildren().remove(from);
        getChildren().add(to, child);
        assignIndexes();
        if (isValid()) {
            WidgetTree.resize(this);
        }
        if (this.onChange != null) {
            this.onChange.accept(getValues());
        }
        this.timeSinceLastMove = 0;
    }

    private void assignIndexes() {
        for (int i = 0; i < getChildren().size(); i++) {
            Item<?> item = (Item<?>) getChildren().get(i);
            item.index = i;
        }
    }

    public SortableListWidget<T, I> onChange(Consumer<List<T>> onChange) {
        this.onChange = onChange;
        return this;
    }

    public static class Item<T> extends DraggableWidget<Item<T>> implements IValueWidget<T> {

        private final T value;
        private final IWidget content;
        private final List<IWidget> children;
        private Predicate<IGuiElement> dropPredicate;
        private SortableListWidget<T, ? extends Item<T>> listWidget;
        private int index = -1, time;

        public Item(T value, IWidget content) {
            this.value = value;
            this.content = content;
            this.children = Collections.singletonList(content);
            this.content.flex().size(1f, 1f);
            flex().width(1f).height(18);
            background(GuiTextures.BUTTON);
        }

        @NotNull
        @Override
        public List<IWidget> getChildren() {
            return children;
        }

        @Override
        public boolean canDropHere(int x, int y, @Nullable IGuiElement widget) {
            return dropPredicate == null || dropPredicate.test(widget);
        }

        @Override
        public boolean onDragStart(int mouseButton) {
            time = 0;
            return super.onDragStart(mouseButton);
        }

        @Override
        public void onDrag(int mouseButton, long timeSinceLastClick) {
            super.onDrag(mouseButton, timeSinceLastClick);
            IGuiElement hovered = getContext().getHovered();
            Item<?> item = (Item<?>) WidgetTree.findParent(hovered, guiElement -> guiElement instanceof Item);
            if (item != null && item != this && item.listWidget == this.listWidget) {
                this.listWidget.moveTo(this.index, item.index);
            }
        }

        @Override
        public void onFrameUpdate() {
            if (isMoving() && ++time % 4 == 0) {
                time = 0;

            }
        }

        @Override
        public void onDragEnd(boolean successful) {
        }

        @Override
        public T getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public Item<T> dropPredicate(Predicate<IGuiElement> dropPredicate) {
            this.dropPredicate = dropPredicate;
            return this;
        }
    }
}
