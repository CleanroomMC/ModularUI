package com.cleanroommc.modularui.widget.resizer.layout;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IResizer;
import com.cleanroommc.modularui.utils.Area;
import com.cleanroommc.modularui.utils.ScrollArea;
import com.cleanroommc.modularui.utils.ScrollDirection;
import com.cleanroommc.modularui.widget.resizer.AutomaticResizer;
import com.cleanroommc.modularui.widget.resizer.Box;
import com.cleanroommc.modularui.widget.resizer.ChildResizer;

public class ColumnResizer extends AutomaticResizer {

    private int x;
    private int y;
    private int w;

    /**
     * Default width
     */
    private int width;

    /**
     * Keeps on adding elements vertically without shifting them into
     * the next row and resize the height of the element
     */
    private boolean vertical;

    /**
     * Stretch column to the full width of the parent element
     */
    private boolean stretch;

    /**
     * Scroll mode, this will automatically calculate the scroll area
     */
    private boolean scroll;

    /**
     * Place elements after it reached the bottom on the left, instead of the right
     */
    private boolean flip;

    public static ColumnResizer apply(IWidget element, int margin) {
        ColumnResizer resizer = new ColumnResizer(element, margin);

        //element.flex().post(resizer);

        return resizer;
    }

    protected ColumnResizer(IWidget element, int margin) {
        super(element, margin);
    }

    public ColumnResizer width(int width) {
        this.width = width;

        return this;
    }

    public ColumnResizer vertical() {
        this.vertical = true;

        return this;
    }

    public ColumnResizer stretch() {
        this.stretch = true;

        return this;
    }

    public ColumnResizer scroll() {
        this.scroll = true;

        return this;
    }

    public ColumnResizer flip() {
        this.flip = true;

        return this;
    }

    @Override
    public void apply(Area area) {
        this.x = 0;
        this.y = 0;
        this.w = 0;
    }

    @Override
    public void apply(Area area, IResizer resizer, ChildResizer child) {
        Box margin = child.element.getMargin();
        int w = resizer == null ? this.width : resizer.getW();
        int h = resizer == null ? this.height : resizer.getH();

        if (w == 0) {
            w = this.width;
        }

        if (h == 0) {
            h = this.height;
        }

        if (this.stretch) {
            w = this.parent.getArea().width - this.padding * 2;
        }

        int marginTop = margin.top;

        if (!this.vertical && this.y + h + marginTop > this.parent.getArea().height - this.padding * 2) {
            this.x += (this.w + this.padding) * (this.flip ? -1 : 1);
            this.y = this.w = 0;

            marginTop = 0;
        }

        int x = this.parent.getArea().x + this.x + this.padding + margin.left;
        int y = this.parent.getArea().y + this.y + this.padding + marginTop;

        area.set(x, y, w, h);

        this.w = Math.max(this.w, w + margin.horizontal());
        this.y += h + this.margin + marginTop + margin.bottom;
    }

    @Override
    public void postApply(Area area) {
        /*if (this.scroll && this.parent.getArea() instanceof ScrollArea) {
            ScrollArea scroll = (ScrollArea) this.parent.getArea();

            if (this.vertical && scroll.direction == ScrollDirection.VERTICAL) {
                scroll.scrollSize = this.y - this.margin + this.padding * 2;
            } else if (!this.vertical && scroll.direction == ScrollDirection.HORIZONTAL) {
                scroll.scrollSize = this.x + this.w + this.padding * 2;
            }

            scroll.clamp();
        }*/
    }

    @Override
    public int getH() {
        if (this.vertical && !this.scroll) {
            int y = this.padding * 2;

            for (ChildResizer child : this.getResizers()) {
                int h = child.resizer == null ? 0 : child.resizer.getH();

                y += (h == 0 ? this.height : h) + this.margin + child.element.getMargin().vertical();
            }

            return y - this.margin;
        }

        return super.getH();
    }
}