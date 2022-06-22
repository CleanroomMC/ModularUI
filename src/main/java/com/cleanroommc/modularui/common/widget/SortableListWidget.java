package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortableListWidget<T> extends ListWidget {

    private final List<T> elements;
    private Function<T, Widget> widgetCreator = t -> new TextWidget(t.toString());

    public SortableListWidget(List<T> elements) {
        this.elements = new ArrayList<>(elements);
    }

    @Override
    public void initChildren() {
        this.children.clear();
        for (int i = 0; i < elements.size(); i++) {
            SortableListItem<T> listItem = new SortableListItem<>(elements.get(i));
            listItem.setWidgetCreator(widgetCreator);
            listItem.init(this);
            listItem.setCurrentIndex(i);
            this.children.add(listItem);
        }
        super.initChildren();
    }

    @Override
    public void onRebuild() {
        super.onRebuild();

    }

    public List<T> createElements() {
        return this.children.stream().map(widget -> ((SortableListItem<T>) widget).getValue()).collect(Collectors.toList());
    }

    protected void removeElement(int index) {
        SortableListItem<T> item = (SortableListItem<T>) children.remove(index);
        assignIndexes();
        checkNeedsRebuild();
    }

    protected void moveElementUp(int index) {
        if (index > 0) {
            Widget widget = children.remove(index);
            children.add(index - 1, widget);
            assignIndexes();
            checkNeedsRebuild();
        }
    }

    protected void moveElementDown(int index) {
        if (index < children.size() - 1) {
            Widget widget = children.remove(index);
            children.add(index + 1, widget);
            assignIndexes();
            checkNeedsRebuild();
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
}
