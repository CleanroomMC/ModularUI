package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IParentWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class Grid extends AbstractScrollWidget<IWidget, Grid> implements ILayoutWidget, IParentWidget<IWidget, Grid> {

    private final List<List<IWidget>> matrix = new ArrayList<>();
    private final Box minElementMargin = new Box();
    private int minRowHeight = 5, minColWidth = 5;
    private Alignment alignment = Alignment.Center;
    private boolean collapseDisabledChild = false;
    private boolean dirty = false;

    public Grid() {
        super(null, null);
    }

    @Override
    public void onInit() {
        super.onInit();
        int maxRowSize = 0;
        for (List<? extends IWidget> row : this.matrix) {
            maxRowSize = Math.max(maxRowSize, row.size());
        }
        for (List<? extends IWidget> row : this.matrix) {
            while (row.size() < maxRowSize) {
                row.add(null);
            }
        }
    }

    private int getMarginStart(Area area, GuiAxis axis, int border) {
        int m = 0;
        if (border > -1) {
            m += Math.max(area.getMargin().getStart(axis), this.minElementMargin.getStart(axis));
        } else {
            m += area.getMargin().getStart(axis);
        }
        return m;
    }

    private int getMarginEnd(Area area, GuiAxis axis, int border) {
        int m = 0;
        if (border > -1) {
            m += Math.max(area.getMargin().getEnd(axis), this.minElementMargin.getEnd(axis));
        } else {
            m += area.getMargin().getEnd(axis);
        }
        return m;
    }

    private int getElementWidth(Area area, int border) {
        // border: -1 = start, 0 = none, 1 = end
        return area.width + getMarginStart(area, GuiAxis.X, border) + getMarginEnd(area, GuiAxis.X, border);
    }

    private int getElementHeight(Area area, int border) {
        // border: -1 = start, 0 = none, 1 = end
        return area.height + getMarginStart(area, GuiAxis.Y, border) + getMarginEnd(area, GuiAxis.Y, border);
    }

    private int border(int index, int size) {
        if (index == 0) return -1;
        if (index == size - 1) return 1;
        return 0;
    }

    @Override
    public boolean layoutWidgets() {
        IntList rowSizes = new IntArrayList();
        IntList colSizes = new IntArrayList();

        int i = 0, j;
        for (List<IWidget> row : this.matrix) {
            j = 0;
            rowSizes.add(this.minRowHeight);
            int yBorder = border(i, this.matrix.size());
            for (IWidget child : row) {
                if (i == 0) {
                    colSizes.add(this.minColWidth);
                }
                if (!shouldIgnoreChildSize(child)) {
                    if (!child.resizer().isWidthCalculated() || !child.resizer().isHeightCalculated()) return false;
                    int xBorder = border(j, row.size());
                    rowSizes.set(i, Math.max(rowSizes.getInt(i), getElementHeight(child.getArea(), yBorder)));
                    colSizes.set(j, Math.max(colSizes.getInt(j), getElementWidth(child.getArea(), xBorder)));
                }
                j++;
            }
            i++;
        }

        int x = 0, y = 0;
        for (int r = 0; r < rowSizes.size(); r++) {
            x = 0;
            int height = rowSizes.get(r);
            for (int c = 0; c < colSizes.size(); c++) {
                int width = colSizes.get(c);
                IWidget child = this.matrix.get(r).get(c);
                if (child != null) {
                    Area area = child.getArea();
                    int xBorder = border(c, colSizes.size());
                    int yBorder = border(r, rowSizes.size());
                    int xs = getMarginStart(area, GuiAxis.X, xBorder);
                    int xe = getMarginEnd(area, GuiAxis.X, xBorder);
                    int ys = getMarginStart(area, GuiAxis.Y, yBorder);
                    int ye = getMarginEnd(area, GuiAxis.Y, yBorder);
                    child.getArea().rx = (int) (x + xs + (width - xs - xe - area.width) * alignment.x);
                    child.getArea().ry = (int) (y + ys + (height - ys - ye - area.height) * alignment.y);
                    child.resizer().setPosResized(true, true);
                }
                x += width;
            }
            y += height;
        }
        if (getScrollArea().getScrollX() != null) {
            getScrollArea().getScrollX().setScrollSize(x);
        }
        if (getScrollArea().getScrollY() != null) {
            getScrollArea().getScrollY().setScrollSize(y);
        }
        return true;
    }

    @Override
    public boolean shouldIgnoreChildSize(IWidget child) {
        return child == null || (this.collapseDisabledChild && !child.isEnabled());
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        if (this.dirty) {
            makeFlatList();
            this.dirty = false;
        }
        return super.getChildren();
    }

    private void makeFlatList() {
        super.getChildren().clear();
        super.getChildren().addAll(this.matrix.stream().flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public int getDefaultHeight() {
        int h = 0;
        for (int i = 0; i < this.matrix.size(); i++) {
            List<IWidget> row = this.matrix.get(i);
            int yBorder = border(i, this.matrix.size());
            int rowHeight = 0;
            for (IWidget child : row) {
                if (!shouldIgnoreChildSize(child)) {
                    rowHeight = Math.max(rowHeight, getElementHeight(child.getArea(), yBorder));
                }
            }
            h += Math.max(rowHeight, this.minRowHeight);
        }
        return h;
    }

    @Override
    public int getDefaultWidth() {
        IntList colSizes = new IntArrayList();
        int i = 0, j;
        for (List<? extends IWidget> row : this.matrix) {
            j = 0;
            for (IWidget child : row) {
                if (i == 0) {
                    colSizes.add(this.minColWidth);
                }
                if (!shouldIgnoreChildSize(child)) {
                    int xBorder = border(j, row.size());
                    colSizes.set(j, Math.max(colSizes.getInt(j), getElementWidth(child.getArea(), xBorder)));
                }
                j++;
            }
            i++;
        }
        int w = 0;
        for (int colWidth : colSizes) {
            w += colWidth;
        }
        return w;
    }

    public <I extends IWidget> Grid matrix(List<List<I>> matrix) {
        this.matrix.clear();
        for (List<I> row : matrix) {
            this.matrix.add((List<IWidget>) row);
        }
        this.dirty = true;
        return this;
    }

    public Grid row(List<IWidget> row) {
        this.matrix.add(row);
        this.dirty = true;
        return this;
    }

    public Grid row(@NotNull IWidget... row) {
        Objects.requireNonNull(row);
        return row(new ArrayList<>(Arrays.asList(row)));
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        if (child == this || getChildren().contains(child)) {
            return false;
        }
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        super.getChildren().add(index, child);
        if (isValid()) {
            child.initialise(this, true);
        }
        onChildAdd(child);
        this.dirty = true;
        return true;
    }

    public Grid child(@Nullable IWidget widget) {
        this.matrix.get(this.matrix.size() - 1).add(widget);
        this.dirty = true;
        return this;
    }

    public Grid nextRow() {
        this.matrix.add(new ArrayList<>());
        return this;
    }

    public <T, I extends IWidget> Grid mapTo(int rowLength, @NotNull List<T> list, @NotNull IndexedElementMapper<T, I> widgetCreator) {
        Objects.requireNonNull(widgetCreator);
        Objects.requireNonNull(list);
        return matrix(mapToMatrix(rowLength, list, widgetCreator));
    }

    public <I extends IWidget> Grid mapTo(int rowLength, @NotNull List<I> list) {
        Objects.requireNonNull(list);
        return mapTo(rowLength, list.size(), list::get);
    }

    public <I extends IWidget> Grid mapTo(int rowLength, int size, @NotNull IntFunction<I> widgetCreator) {
        Objects.requireNonNull(widgetCreator);
        return matrix(mapToMatrix(rowLength, size, widgetCreator));
    }

    public Grid minColWidth(int minColWidth) {
        this.minColWidth = minColWidth;
        return this;
    }

    public Grid minRowHeight(int minRowHeight) {
        this.minRowHeight = minRowHeight;
        return this;
    }

    public Grid alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Grid scrollable() {
        return scrollable(new VerticalScrollData(), new HorizontalScrollData());
    }

    public Grid scrollable(ScrollData data) {
        getScrollArea().setScrollData(data);
        return this;
    }

    public Grid scrollable(VerticalScrollData data1, HorizontalScrollData data2) {
        getScrollArea().setScrollData(data1);
        getScrollArea().setScrollData(data2);
        return this;
    }

    public Grid minElementMargin(int left, int right, int top, int bottom) {
        this.minElementMargin.all(left, right, top, bottom);
        return getThis();
    }

    public Grid minElementMargin(int horizontal, int vertical) {
        this.minElementMargin.all(horizontal, vertical);
        return getThis();
    }

    public Grid minElementMargin(int all) {
        this.minElementMargin.all(all);
        return getThis();
    }

    public Grid minElementMarginLeft(int val) {
        this.minElementMargin.left(val);
        return getThis();
    }

    public Grid minElementMarginRight(int val) {
        this.minElementMargin.right(val);
        return getThis();
    }

    public Grid minElementMarginTop(int val) {
        this.minElementMargin.top(val);
        return getThis();
    }

    public Grid minElementMarginBottom(int val) {
        this.minElementMargin.bottom(val);
        return getThis();
    }

    /**
     * Configures this widget to collapse row/column if all the child widgets in that axis are disabled.
     */
    public Grid collapseDisabledChild() {
        this.collapseDisabledChild = true;
        return getThis();
    }

    public static <T, I extends IWidget> List<List<I>> mapToMatrix(int rowLength, List<T> list, IndexedElementMapper<T, I> widgetCreator) {
        return mapToMatrix(rowLength, list.size(), i -> widgetCreator.apply(i, list.get(i)));
    }

    public static <I extends IWidget> List<List<I>> mapToMatrix(int rowLength, int size, IntFunction<I> widgetCreator) {
        List<List<I>> matrix = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int r = i / rowLength;

            if (r == matrix.size())
                matrix.add(new ArrayList<>());

            matrix.get(r).add(widgetCreator.apply(i));
        }
        return matrix;
    }

    public Box getMinElementMargin() {
        return minElementMargin;
    }

    public int getMinRowHeight() {
        return minRowHeight;
    }

    public int getMinColWidth() {
        return minColWidth;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public boolean isCollapseDisabledChild() {
        return collapseDisabledChild;
    }

    public interface IndexedElementMapper<T, I> {

        I apply(int index, T value);
    }
}
