package com.cleanroommc.modularui.common.internal.wrapper;

import java.util.*;

public class MultiList<T> extends AbstractList<T> {

    private final List<List<T>> lists = new ArrayList<>();
    private final List<T> defaultList = new ArrayList<>();

    public MultiList() {
        this.lists.add(defaultList);
    }

    public void addList(List<T> list) {
        this.lists.add(list);
    }

    public void addElements(T... ts) {
        addList(Arrays.asList(ts));
    }

    public void clearLists() {
        lists.clear();
        defaultList.clear();
        lists.add(defaultList);
    }

    @Override
    public void clear() {
        for (List<T> list : lists) {
            list.clear();
        }
    }

    @Override
    public boolean add(T element) {
        defaultList.add(element);
        return true;
    }

    @Override
    public T get(int index) {
        if (index < 0) throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        for (List<T> list : lists) {
            if (index >= list.size()) {
                index -= list.size();
                continue;
            }
            return list.get(index);
        }
        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }

    @Override
    public int size() {
        return lists.stream().mapToInt(List::size).sum();
    }

    @Override
    public Iterator<T> iterator() {
        return new MultiListIterator();
    }

    private class MultiListIterator implements Iterator<T> {

        private int listCursor = 0, cursor = 0;
        private List<T> currentLists;

        @Override
        public boolean hasNext() {
            return listCursor < lists.size() || cursor < currentLists.size();
        }

        @Override
        public T next() {
            try {
                while (currentLists == null || cursor == currentLists.size()) {
                    currentLists = lists.get(listCursor++);
                    cursor = 0;
                }
                return currentLists.get(cursor++);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }
    }
}
