package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.Widget;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortableListWidget<T> extends ListWidget {

    private final Map<T, SortableListItem<T>> widgetMap = new HashMap<>();
    private final List<T> startValues;
    private Function<T, Widget> widgetCreator = t -> new TextWidget(t.toString());
    private Consumer<List<T>> saveFunction = list -> {
    };
    private Consumer<T> onRemoveElement = t -> {
    };
    private boolean elementsRemovable = false;

    public static <T> SortableListWidget<T> removable(Collection<T> allValues, List<T> startValues) {
        return new SortableListWidget<>(true, allValues, startValues);
    }

    private SortableListWidget(boolean removable, Collection<T> allValues, List<T> startValues) {
        this.elementsRemovable = removable;
        for (T t : allValues) {
            widgetMap.put(t, null);
        }
        this.startValues = new ArrayList<>(startValues);
    }

    public SortableListWidget(List<T> startValues) {
        this(false, startValues, startValues);
    }

    @Override
    public void initChildren() {
        super.initChildren();
        this.children.clear();
        int i = 0;
        for (T t : widgetMap.keySet()) {
            SortableListItem<T> listItem = new SortableListItem<>(t);
            listItem.init(this);
            listItem.setCurrentIndex(i++);
            this.widgetMap.put(t, listItem);
            this.children.add(listItem);
        }
    }

    @Override
    public void onFirstRebuild() {
        this.children.clear();
        for (T t : startValues) {
            SortableListItem<T> listItem = widgetMap.get(t);
            if (listItem == null) {
                ModularUI.LOGGER.error("Unexpected error: Could not find sortable list item for {}!", t);
                continue;
            }
            this.children.add(listItem);
        }
        assignIndexes();
        layoutChildren(0, 0);
        onRebuild();
    }

    @Override
    public void onPause() {
        this.saveFunction.accept(createElements());
    }

    public List<T> createElements() {
        return this.children.stream().map(widget -> ((SortableListItem<T>) widget).getValue()).collect(Collectors.toList());
    }

    protected void removeElement(int index) {
        SortableListItem<T> item = (SortableListItem<T>) children.remove(index);
        onRemoveElement.accept(item.getValue());
        assignIndexes();
        layoutChildren(0, 0);
        onRebuild();
    }

    protected void moveElementUp(int index) {
        if (index > 0) {
            Widget widget = children.remove(index);
            children.add(index - 1, widget);
            assignIndexes();
            layoutChildren(0, 0);
            onRebuild();
        }
    }

    protected void moveElementDown(int index) {
        if (index < children.size() - 1) {
            Widget widget = children.remove(index);
            children.add(index + 1, widget);
            assignIndexes();
            layoutChildren(0, 0);
            onRebuild();
        }
    }

    protected void putAtIndex(int index, int toIndex) {
        Widget widget = children.remove(index);
        children.add(toIndex, widget);
        assignIndexes();
        checkNeedsRebuild();
    }

    protected void assignIndexes() {
        for (int i = 0; i < children.size(); i++) {
            ((SortableListItem<T>) children.get(i)).setCurrentIndex(i);
        }
    }

    public Function<T, Widget> getWidgetCreator() {
        return widgetCreator;
    }

    public Consumer<T> getOnRemoveElement() {
        return onRemoveElement;
    }

    public boolean areElementsRemovable() {
        return elementsRemovable;
    }

    public void addElement(T t) {
        if (!isInitialised()) {
            throw new IllegalStateException("List needs to be initialised to add elements dynamically.");
        }
        if (!widgetMap.containsKey(t)) {
            throw new NoSuchElementException("This list widget was not initialised with the value " + t);
        }
        SortableListItem<T> listItem = widgetMap.get(t);
        listItem.setActive(true);
        listItem.setEnabled(true);
        this.children.add(listItem);
        assignIndexes();
        checkNeedsRebuild();
    }

    public SortableListWidget<T> setWidgetCreator(Function<T, Widget> widgetCreator) {
        this.widgetCreator = widgetCreator;
        return this;
    }

    public SortableListWidget<T> setSaveFunction(Consumer<List<T>> saveFunction) {
        this.saveFunction = saveFunction;
        return this;
    }

    public SortableListWidget<T> setElementsRemovable(boolean elementsRemovable) {
        this.elementsRemovable = elementsRemovable;
        return this;
    }

    public SortableListWidget<T> setOnRemoveElement(Consumer<T> onRemoveElement) {
        this.onRemoveElement = onRemoveElement;
        return this;
    }
}
