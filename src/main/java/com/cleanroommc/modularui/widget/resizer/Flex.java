package com.cleanroommc.modularui.widget.resizer;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IResizer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Area;
import com.cleanroommc.modularui.widget.resizer.constraint.BoundsResizer;
import com.cleanroommc.modularui.widget.resizer.layout.ColumnResizer;
import com.cleanroommc.modularui.widget.resizer.layout.GridResizer;
import com.cleanroommc.modularui.widget.resizer.layout.RowResizer;

import java.util.function.Supplier;

/**
 * Flex class
 * <p>
 * This class is used to define resizing behavior for a
 * {@link IWidget}.
 */
public class Flex implements IResizer {

    public Unit x;
    public Unit y;
    public Unit w;
    public Unit h;

    public boolean relativeToParent = true;
    public IResizer relative;
    public IResizer post;

    public final IWidget parent;

    public Flex(IWidget parent) {
        this.parent = parent;
        reset();
    }

    public Flex reset() {
        this.x = new Unit();
        this.y = new Unit();
        this.w = new Unit();
        this.h = new Unit();

        this.relative = this.post = null;

        return this;
    }

    public void link(Flex flex) {
        this.x = flex.x;
        this.y = flex.y;
        this.w = flex.w;
        this.h = flex.h;

        this.relative = flex.relative;
        this.post = flex.post;
    }

    public Flex set(float x, float y, float w, float h) {
        return this.set(x, y, w, h, Measure.PIXELS);
    }

    public Flex set(float x, float y, float w, float h, Measure measure) {
        this.x.set(x, measure);
        this.y.set(y, measure);
        this.w.set(w, measure);
        this.h.set(h, measure);

        return this;
    }

    /* X */

    public Flex x(int value) {
        this.x.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex x(float value) {
        this.x.set(value, Measure.RELATIVE);

        return this;
    }

    public Flex x(float value, int offset) {
        this.x.set(value, Measure.RELATIVE, offset);

        return this;
    }

    public Flex x(Supplier<Float> value) {
        this.x.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex x(Supplier<Float> value, int offset) {
        this.x.set(value, Measure.PIXELS, offset);

        return this;
    }

    /* Y */

    public Flex y(int value) {
        this.y.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex y(float value) {
        this.y.set(value, Measure.RELATIVE, 0);

        return this;
    }

    public Flex y(float value, int offset) {
        this.y.set(value, Measure.RELATIVE, offset);

        return this;
    }

    public Flex y(Supplier<Float> value) {
        this.y.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex y(Supplier<Float> value, int offset) {
        this.y.set(value, Measure.PIXELS, offset);

        return this;
    }

    /* Width */

    public Flex w(int value) {
        this.w.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex w(float value) {
        this.w.set(value, Measure.RELATIVE, 0);

        return this;
    }

    public Flex w(float value, int offset) {
        this.w.set(value, Measure.RELATIVE, offset);

        return this;
    }

    public Flex w(Supplier<Float> value) {
        this.w.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex w(Supplier<Float> value, int offset) {
        this.w.set(value, Measure.PIXELS, offset);

        return this;
    }

    public Flex wTo(IResizer flex) {
        this.w.target = flex;

        return this;
    }

    public Flex wTo(IResizer flex, int offset) {
        this.w.target = flex;
        this.w.offset = offset;

        return this;
    }

    public Flex wTo(IResizer flex, float anchor) {
        this.w.target = flex;
        this.w.targetAnchor = anchor;

        return this;
    }

    public Flex wTo(IResizer flex, float anchor, int offset) {
        this.w.target = flex;
        this.w.targetAnchor = anchor;
        this.w.offset = offset;

        return this;
    }

    /* Height */

    public Flex h(int value) {
        this.h.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex h(float value) {
        this.h.set(value, Measure.RELATIVE, 0);

        return this;
    }

    public Flex h(float value, int offset) {
        this.h.set(value, Measure.RELATIVE, offset);

        return this;
    }

    public Flex h(Supplier<Float> value) {
        this.h.set(value, Measure.PIXELS, 0);

        return this;
    }

    public Flex h(Supplier<Float> value, int offset) {
        this.h.set(value, Measure.PIXELS, offset);

        return this;
    }

    public Flex hTo(IResizer target) {
        return this.hTo(target, 0);
    }

    public Flex hTo(IResizer target, int offset) {
        return this.hTo(target, 0F, offset);
    }

    public Flex hTo(IResizer target, float anchor) {
        return this.hTo(target, anchor, 0);
    }

    public Flex hTo(IResizer target, float anchor, int offset) {
        this.h.target = target;
        this.h.targetAnchor = anchor;
        this.h.offset = offset;

        return this;
    }

    /* Other variations */

    public Flex xy(int x, int y) {
        this.x.set(x, Measure.PIXELS);
        this.y.set(y, Measure.PIXELS);

        return this;
    }

    public Flex xy(float x, float y) {
        this.x.set(x, Measure.RELATIVE);
        this.y.set(y, Measure.RELATIVE);

        return this;
    }

    public Flex wh(int w, int h) {
        this.w.set(w, Measure.PIXELS);
        this.h.set(h, Measure.PIXELS);

        return this;
    }

    public Flex wh(float w, float h) {
        this.w.set(w, Measure.RELATIVE);
        this.h.set(h, Measure.RELATIVE);

        return this;
    }

    public Flex fill(ModularScreen relative) {
        return relative(relative).wh(1f, 1f);
    }

    public Flex fill(IResizer relative) {
        return relative(relative).wh(1f, 1f);
    }

    public Flex fill(IWidget relative) {
        return relative(relative).wh(1f, 1f);
    }

    public Flex maxW(int max) {
        this.w.max = max;

        return this;
    }

    public Flex maxH(int max) {
        this.h.max = max;

        return this;
    }

    public Flex anchor(float x, float y) {
        this.x.anchor = x;
        this.y.anchor = y;

        return this;
    }

    public Flex anchorX(float x) {
        this.x.anchor = x;

        return this;
    }

    public Flex anchorY(float y) {
        this.y.anchor = y;

        return this;
    }

    /* Convenience methods */

    public Flex above(Flex flex, int offset) {
        return this.relative(flex).y(0, offset).anchorY(1);
    }

    public Flex under(Flex flex, int offset) {
        return this.relative(flex).y(1, offset);
    }

    public Flex left(Flex flex, int offset) {
        return this.relative(flex).x(0, offset).anchorX(1);
    }

    public Flex right(Flex flex, int offset) {
        return this.relative(flex).x(1, offset);
    }

    public Flex centerIn(IResizer resizer) {
        return this.relative(resizer).anchor(.5f, .5f).xy(.5f, .5f);
    }

    public Flex centerIn(IWidget resizer) {
        //return centerIn(resizer.getArea());
        return this;
    }

    public Flex centerIn(ModularScreen resizer) {
        //return centerIn(resizer.getViewport());
        return this;
    }

    public Flex centerInParent() {
        return relativeToParent().anchor(.5f, .5f).xy(.5f, .5f);
    }

    /* Post resizers convenience methods
     * TODO: remove child resizers when switching to another post method */

    public RowResizer row(int margin) {
        if (this.post instanceof RowResizer) {
            return (RowResizer) this.post;
        }

        return RowResizer.apply(this.parent, margin);
    }

    public ColumnResizer column(int margin) {
        if (this.post instanceof ColumnResizer) {
            return (ColumnResizer) this.post;
        }

        return ColumnResizer.apply(this.parent, margin);
    }

    public GridResizer grid(int margin) {
        if (this.post instanceof GridResizer) {
            return (GridResizer) this.post;
        }

        return GridResizer.apply(this.parent, margin);
    }

    public BoundsResizer bounds(IWidget target, int margin) {
        if (this.post instanceof BoundsResizer) {
            return (BoundsResizer) this.post;
        }

        return BoundsResizer.apply(this.parent, target, margin);
    }

    /* Hierarchy */

    /**
     * This method assigns the element.area as the relative target.
     *
     * @param widget {@link IWidget} to which this is relative to.
     * @return this
     */
    public Flex relative(IWidget widget) {
        //this.relative = widget.getArea();
        return relativeToParent(false);
    }

    public Flex relative(ModularScreen screen) {
        //this.relative = screen.getViewport();
        return relativeToParent(false);
    }

    public Flex relative(IResizer relative) {
        this.relative = relative;
        return relativeToParent(false);
    }

    public Flex relativeToParent(boolean b) {
        this.relativeToParent = b;
        return this;
    }

    public Flex relativeToParent() {
        return relativeToParent(true);
    }

    public Flex post(IResizer post) {
        this.post = post;
        return this;
    }

    /* IResizer implementation */

    @Override
    public void preApply(Area area) {
    }

    @Override
    public void apply(Area area) {
        if (this.relativeToParent) {
            /*this.relative = this.parent instanceof ModularPanel ?
                    ((ModularPanel) this.parent).getScreen().getViewport() :
                    this.parent.getParent().getArea();*/
        }

        if (this.post != null) {
            this.post.preApply(area);
        }

        area.w = this.getW();
        area.h = this.getH();
        area.x = this.getX();
        area.y = this.getY();

        if (this.post != null) {
            this.post.apply(area);
        }
    }

    @Override
    public void postApply(Area area) {
        if (this.post != null) {
            this.post.postApply(area);
        }
    }

    @Override
    public void add(IWidget parent, IWidget child) {
        if (this.post != null) {
            this.post.add(parent, child);
        }
    }

    @Override
    public void remove(IWidget parent, IWidget child) {
        if (this.post != null) {
            this.post.remove(parent, child);
        }
    }

    public int getX() {
        int value = (int) this.x.getValue();

        if (this.relative != null) {
            if (this.x.unit == Measure.PIXELS) {
                value += this.relative.getX();
            } else {
                value = this.relative.getX() + (int) (this.relative.getW() * this.x.getValue());
            }
            value += this.x.offset;
        }

        if (this.x.anchor != 0) {
            int w = getW();
            value -= this.x.anchor * w;
        }

        return value;
    }

    public int getY() {
        int value = (int) this.y.getValue();

        if (this.relative != null) {
            value += this.relative.getY();

            if (this.y.unit == Measure.RELATIVE) {
                value = this.relative.getY() + (int) (this.relative.getH() * this.y.getValue());
            }
        }

        value += this.y.offset;

        if (this.y.anchor != 0) {
            value -= this.y.anchor * this.getH();
        }

        return value;
    }

    public int getW() {
        if (this.w.target != null) {
            int w = this.w.targetAnchor == 0 ? 0 : (int) (this.w.target.getW() * this.w.targetAnchor);

            return this.w.normalize((this.w.target.getX() + w) - this.getX() + this.w.offset);
        }

        int value = this.post == null ? 0 : this.post.getW();

        if (value != 0) {
            return value;
        }

        value = (int) this.w.getValue();

        if (this.relative != null && this.w.unit == Measure.RELATIVE) {
            value = (int) (this.relative.getW() * this.w.getValue());
        }

        value = value + this.w.offset;

        if (this.w.max > 0) {
            value = Math.min(value, this.w.max);
        }

        return value;
    }

    public int getH() {
        if (this.h.target != null) {
            int h = this.h.targetAnchor == 0 ? 0 : (int) (this.h.target.getH() * this.h.targetAnchor);

            return this.h.normalize((this.h.target.getY() + h) - this.getY() + this.h.offset);
        }

        int value = this.post == null ? 0 : this.post.getH();

        if (value != 0) {
            return value;
        }

        value = (int) this.h.getValue();

        if (this.relative != null && this.h.unit == Measure.RELATIVE) {
            value = (int) (this.relative.getH() * this.h.getValue());
        }

        value = value + this.h.offset;

        if (this.h.max > 0) {
            value = Math.min(value, this.h.max);
        }

        return value;
    }

    /**
     * Unit class
     */
    public static class Unit {
        private float value;
        private Supplier<Float> valueConsumer;
        public int offset;
        public int max;
        public float anchor;
        public Measure unit = Measure.PIXELS;
        public IResizer target;
        public float targetAnchor;

        public void set(Supplier<Float> value, Measure unit) {
            this.set(value, unit, 0);
        }

        public void set(Supplier<Float> value, Measure unit, int offset) {
            this.valueConsumer = value;
            this.unit = unit;
            this.offset = offset;

            /* Reset the value and target */
            this.value = 0;
            this.target = null;
            this.targetAnchor = 0;
        }

        public void set(float value, Measure unit) {
            this.set(value, unit, 0);
        }

        public void set(float value, Measure unit, int offset) {
            this.value = value;
            this.unit = unit;
            this.offset = offset;

            /* Reset target */
            this.target = null;
            this.targetAnchor = 0;
        }

        public float getValue() {
            return this.valueConsumer == null ? this.value : this.valueConsumer.get();
        }

        public int normalize(int value) {
            return this.max > 0 ? Math.min(value, this.max) : value;
        }
    }

    /**
     * Unit measurement for sizer class. This determines logic for
     * calculating units.
     * <p>
     * {@link Measure#PIXELS} are absolute. Meanwhile
     * {@link Measure#RELATIVE} are percentage (or rather a scalar
     * between 0 and 1 equaling to 0% to 100%).
     */
    public enum Measure {
        PIXELS, RELATIVE
    }
}