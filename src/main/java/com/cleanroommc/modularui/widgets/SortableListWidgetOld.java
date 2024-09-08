package com.cleanroommc.modularui.widgets;
/*
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widget.DraggableWidget;
import com.cleanroommc.modularui.widget.WidgetTree;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SortableListWidgetOld<T, I extends SortableListWidgetOld.Item<T>> extends ListValueWidget<T, I, SortableListWidgetOld<T, I>> {

    public static <T, I extends SortableListWidgetOld.Item<T>> SortableListWidgetOld<T, I> sortableBuilder(Collection<T> fullList, List<T> list, Function<T, I> builder) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(builder);
        Map<T, I> map = new Object2ObjectOpenHashMap<>();
        SortableListWidgetOld<T, I> sortableListWidget = new SortableListWidgetOld<>(map::get);
        for (T t : fullList) {
            I item = builder.apply(t);
            map.put(t, item);
        }
        for (T t : list) {
            if (!fullList.contains(t)) {
                throw new IllegalArgumentException("Elements from list must also be inside the full list!");
            }
            sortableListWidget.add(t, -1);
        }
        return sortableListWidget;
    }

    private Consumer<List<T>> onChange;
    private Consumer<Item<T>> onRemove;
    private int timeSinceLastMove = 0;

    public SortableListWidgetOld(Function<T, I> valueToWidgetMapper) {
        super(valueToWidgetMapper, Item::getWidgetValue);
        width(80);
    }

    @Override
    public void onInit() {
        assignIndexes();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.timeSinceLastMove++;
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        T value = this.widgetToValueMapper.apply((I) child);
        if (child != this.valueToWidgetMapper.apply(value)) {
            throw new IllegalArgumentException();
        }
        ((Item<T>) child).listWidget = this;
        if (super.addChild(child, index)) {
            if (isValid()) {
                assignIndexes();
                if (this.onChange != null) {
                    this.onChange.accept(getValues());
                }
            }
            return true;
        }
        return false;
    }

    public void moveTo(int from, int to) {
        if (this.timeSinceLastMove < 3) return;
        if (from < 0 || to < 0 || from == to) {
            ModularUI.LOGGER.error("Failed to move element from {} to {}", from, to);
            return;
        }
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

    public boolean remove(int index) {
        IWidget widget = getChildren().remove(index);
        if (widget != null) {
            assignIndexes();
            if (isValid()) {
                WidgetTree.resize(this);
            }
            if (this.onChange != null) {
                this.onChange.accept(getValues());
            }
            if (this.onRemove != null) {
                this.onRemove.accept((Item<T>) widget);
            }
            return true;
        }
        return false;
    }

    private void assignIndexes() {
        for (int i = 0; i < getChildren().size(); i++) {
            Item<?> item = (Item<?>) getChildren().get(i);
            item.index = i;
        }
    }

    public SortableListWidgetOld<T, I> onChange(Consumer<List<T>> onChange) {
        this.onChange = onChange;
        return this;
    }

    public SortableListWidgetOld<T, I> onRemove(Consumer<Item<T>> onRemove) {
        this.onRemove = onRemove;
        return this;
    }

    public static class Item<T> extends DraggableWidget<Item<T>> implements IValueWidget<T> {

        private final T value;
        private final IWidget content;
        private ButtonWidget<? extends ButtonWidget<?>> removeButton;
        private final List<IWidget> children = new ArrayList<>();
        private Predicate<IGuiElement> dropPredicate;
        private SortableListWidgetOld<T, ? extends Item<T>> listWidget;
        private int index = -1;

        public Item(T value, IWidget content) {
            this.value = value;
            this.content = content;
            this.children.add(content);
            this.content.flex().heightRel(1f);
            flex().widthRel(1f).height(18);
            background(GuiTextures.BUTTON_CLEAN);
        }

        @Override
        public void onInit() {
            super.onInit();
            if (this.removeButton != null) {

                this.children.add(this.removeButton);
            }
        }

        @NotNull
        @Override
        public List<IWidget> getChildren() {
            return this.children;
        }

        @Override
        public boolean canDropHere(int x, int y, @Nullable IGuiElement widget) {
            return this.dropPredicate == null || this.dropPredicate.test(widget);
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
        public void onDragEnd(boolean successful) {
        }

        @Override
        public T getWidgetValue() {
            return this.value;
        }

        public int getIndex() {
            return this.index;
        }

        public Item<T> dropPredicate(Predicate<IGuiElement> dropPredicate) {
            this.dropPredicate = dropPredicate;
            return this;
        }

        public Item<T> removeable() {
            this.removeButton = new ButtonWidget<>()
                    .onMousePressed(mouseButton -> this.listWidget.remove(this.index))
                    .background(GuiTextures.CLOSE.asIcon())
                    .width(10).heightRel(1f)
                    .right(0);
            return this;
        }

        public Item<T> removeable(Consumer<ButtonWidget<? extends ButtonWidget<?>>> buttonBuilder) {
            removeable();
            buttonBuilder.accept(this.removeButton);
            return this;
        }
    }
}*/
