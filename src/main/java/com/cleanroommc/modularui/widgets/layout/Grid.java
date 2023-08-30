package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.ScrollData;
import com.cleanroommc.modularui.utils.ScrollDirection;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Grid extends ScrollWidget<Grid> implements ILayoutWidget {

    private final List<List<IWidget>> matrix = new ArrayList<>();
    private final Box minElementMargin = new Box();
    private int minRowHeight = 5, minColWidth = 5;
    private boolean dirty = false;

    public Grid() {
        this.minElementMargin.all(2);
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

    private int getElementWidth(Area area) {
        return area.width + Math.max(area.getMargin().left, this.minElementMargin.left) + Math.max(area.getMargin().right, this.minElementMargin.right);
    }

    private int getElementHeight(Area area) {
        return area.height + Math.max(area.getMargin().top, this.minElementMargin.top) + Math.max(area.getMargin().bottom, this.minElementMargin.bottom);
    }

    @Override
    public void layoutWidgets() {
        IntList rowSizes = new IntArrayList();
        IntList colSizes = new IntArrayList();

        int i = 0, j;
        for (List<IWidget> row : this.matrix) {
            j = 0;
            rowSizes.add(this.minRowHeight);
            for (IWidget child : row) {
                if (i == 0) {
                    colSizes.add(this.minColWidth);
                }
                if (child != null) {
                    rowSizes.set(i, Math.max(rowSizes.getInt(i), getElementHeight(child.getArea())));
                    colSizes.set(j, Math.max(colSizes.getInt(j), getElementWidth(child.getArea())));
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
                    child.getArea().rx = (int) (x + width * 0.5 - child.getArea().width * 0.5);
                    child.getArea().ry = (int) (y + height * 0.5 - child.getArea().height * 0.5);
                    child.resizer().setPosResized(true, true);
                }
                x += width;
            }
            y += height;
        }
        if (getScrollArea().getScrollX() != null) {
            getScrollArea().getScrollX().scrollSize = x;
        }
        if (getScrollArea().getScrollY() != null) {
            getScrollArea().getScrollY().scrollSize = y;
        }
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
        for (List<IWidget> row : this.matrix) {
            int rowHeight = 0;
            for (IWidget child : row) {
                if (child != null) {
                    rowHeight = Math.max(rowHeight, getElementHeight(child.getArea()));
                }
            }
            h += Math.min(rowHeight, this.minRowHeight);
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
                if (child != null) {
                    colSizes.set(j, Math.max(colSizes.getInt(j), getElementWidth(child.getArea())));
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

    public Grid row(IWidget... row) {
        List<IWidget> list = new ArrayList<>();
        Collections.addAll(list, row);
        return row(list);
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
            child.initialise(this);
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

    public <T, I extends IWidget> Grid mapTo(int rowLength, List<T> collection, IndexedElementMapper<T, I> widgetCreator) {
        return matrix(mapToMatrix(rowLength, collection, widgetCreator));
    }

    public Grid minColWidth(int minColWidth) {
        this.minColWidth = minColWidth;
        return this;
    }

    public Grid minRowHeight(int minRowHeight) {
        this.minRowHeight = minRowHeight;
        return this;
    }

    public Grid scrollable() {
        return scrollable(new ScrollData(ScrollDirection.VERTICAL), new ScrollData(ScrollDirection.HORIZONTAL));
    }

    public Grid scrollable(ScrollDirection direction) {
        return scrollable(new ScrollData(direction));
    }

    public Grid scrollable(ScrollData data) {
        getScrollArea().setScrollData(data);
        return this;
    }

    public Grid scrollable(ScrollData data1, ScrollData data2) {
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

    public Grid margin(int all) {
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

    public static <T, I extends IWidget> List<List<I>> mapToMatrix(int rowLength, List<T> collection, IndexedElementMapper<T, I> widgetCreator) {
        List<List<I>> matrix = new ArrayList<>();
        for (int i = 0; i < collection.size(); i++) {
            int r = i / rowLength;
            List<I> row;
            if (matrix.size() <= r) {
                row = new ArrayList<>();
                matrix.add(row);
            } else {
                row = matrix.get(r);
            }
            row.add(widgetCreator.apply(i, collection.get(i)));
        }
        return matrix;
    }

    public interface IndexedElementMapper<T, I> {
        I apply(int index, T value);
    }
}
