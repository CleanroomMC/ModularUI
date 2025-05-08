package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.TextIcon;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TooltipLines extends AbstractList<String> {

    private final List<Object> elements;
    private final List<Line> lines = new ArrayList<>(8);
    private int lastElementIndex = 0;

    public TooltipLines(List<Object> elements) {
        this.elements = elements;
    }

    public void clearCache() {
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    private void buildUntil(int index) {
        while (index >= lines.size()) {
            Line line = parseNext();
            if (line == null) break;
            lines.add(line);
        }
    }

    private Line parseNext() {
        if (this.lastElementIndex >= elements.size()) return null;
        StringBuilder currentLine = new StringBuilder();
        int currentLength = 0;
        for (int i = this.lastElementIndex; i < this.elements.size(); i++) {
            Object o = elements.get(i);
            currentLength++;
            if (o == IKey.LINE_FEED) {
                Line line = new Line(currentLine.toString(), this.lastElementIndex, currentLength);
                this.lastElementIndex += currentLength;
                return line;
            }
            String s = null;
            if (o instanceof IKey key) {
                s = key.get();
            } else if (o instanceof String s1) {
                s = s1;
            } else if (o instanceof TextIcon ti) {
                s = ti.getText();
            }
            if (s != null) {
                currentLine.append(s);
            }
        }
        if (currentLength > 0) {
            Line line = new Line(currentLine.toString(), this.lastElementIndex, currentLength);
            this.lastElementIndex += currentLength;
            return line;
        }
        return null;
    }

    @Override
    public String get(int index) {
        buildUntil(index);
        return lines.get(index).text;
    }

    @Override
    public int size() {
        buildUntil(Integer.MAX_VALUE);
        return lines.size();
    }

    @Override
    public String remove(int index) {
        buildUntil(index);
        Line line = lines.remove(index);

        if (line.length == 1) {
            this.elements.remove(line.index);
        } else {
            this.elements.subList(line.index, line.index + line.length).clear();
        }
        for (int i = index; i < lines.size(); i++) {
            lines.get(i).index -= line.length;
        }
        this.lastElementIndex -= line.length;

        return line.text;
    }

    @Override
    public void add(int index, String s) {
        buildUntil(index);
        int elementIndex = index >= this.lines.size() ? this.lastElementIndex : this.lines.get(index).index;
        lines.add(index, new Line(s, elementIndex, 1));
        for (int i = index + 1; i < this.lines.size(); i++) {
            lines.get(i).index++;
        }
        this.elements.add(elementIndex, s);
        this.lastElementIndex++;
    }

    @Override
    public String set(int index, String element) {
        Line line = lines.get(index);
        if (line.length == 1) {
            this.elements.set(line.index, element);
            this.lines.set(index, new Line(element, line.index, line.length));
        } else {
            remove(index);
            add(index, element);
        }
        return line.text;
    }

    @Override
    public void clear() {
        this.elements.clear();
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    private static class Line {
        private final String text;
        private final int length;
        private int index;

        private Line(String text, int index, int length) {
            this.text = text;
            this.index = index;
            this.length = length;
        }
    }
}
