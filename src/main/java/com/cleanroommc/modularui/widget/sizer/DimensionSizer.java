package com.cleanroommc.modularui.widget.sizer;

import java.util.function.IntSupplier;

public class DimensionSizer {

    private final GuiAxis axis;

    private final Unit p1 = new Unit(), p2 = new Unit();
    private Unit start, end, size;

    private boolean coverChildren = false;
    private boolean cancelAutoMovement = false;
    private boolean defaultMode = false;

    private boolean posCalculated = false, sizeCalculated = false;
    private boolean marginPaddingApplied = false;

    public DimensionSizer(GuiAxis axis) {
        this.axis = axis;
    }

    public void reset() {
        this.p1.reset();
        this.p2.reset();
        this.start = null;
        this.end = null;
        this.size = null;
    }

    public void resetPosition() {
        if (this.start != null) {
            this.start.reset();
            this.start = null;
        }
        if (this.end != null) {
            this.end.reset();
            this.end = null;
        }
    }

    public void resetSize() {
        if (this.size != null) {
            this.size.reset();
            this.size = null;
        }
    }

    public void setDefaultMode(boolean defaultMode) {
        this.defaultMode = defaultMode;
    }

    public void setCoverChildren(boolean coverChildren) {
        getSize();
        this.coverChildren = coverChildren;
    }

    public void setCancelAutoMovement(boolean cancelAutoMovement) {
        this.cancelAutoMovement = cancelAutoMovement;
    }

    public boolean hasStart() {
        return this.start != null;
    }

    public boolean hasEnd() {
        return this.end != null;
    }

    public boolean hasPos() {
        return this.start != null || this.end != null;
    }

    public boolean hasFixedSize() {
        return this.start == null || this.end == null;
    }

    public boolean hasSize() {
        return this.size != null;
    }

    public boolean isSizeCalculated() {
        return this.sizeCalculated;
    }

    public boolean isPosCalculated() {
        return this.posCalculated;
    }

    public boolean dependsOnChildren() {
        return this.coverChildren;
    }

    public boolean dependsOnParent() {
        return this.end != null ||
                (this.start != null && this.start.isRelative()) ||
                (this.size != null && this.size.isRelative());
    }

    public void setResized(boolean all) {
        setResized(all, all);
    }

    public void setResized(boolean pos, boolean size) {
        this.posCalculated = pos;
        this.sizeCalculated = size;
    }

    public boolean isMarginPaddingApplied() {
        return marginPaddingApplied;
    }

    public void setMarginPaddingApplied(boolean marginPaddingApplied) {
        this.marginPaddingApplied = marginPaddingApplied;
    }

    private boolean needsSize(Unit unit) {
        return unit.isRelative() && unit.getAnchor() != 0;
    }

    public void apply(Area area, IResizeable relativeTo, IntSupplier defaultSize) {
        if (this.sizeCalculated && this.posCalculated) return;
        int p, s;
        int parentSize = relativeTo.getArea().getSize(this.axis);
        boolean calcParent = relativeTo.isSizeCalculated(this.axis);

        if (this.sizeCalculated && !this.posCalculated) {
            s = area.getSize(this.axis);
            if (this.start != null) {
                p = calcPoint(this.start, s, parentSize, calcParent);
            } else if (this.end != null) {
                p = calcPoint(this.end, s, parentSize, calcParent);
                p = parentSize - p - s;
            } else {
                throw new IllegalStateException();
            }
        } else if (!this.sizeCalculated && this.posCalculated) {
            p = area.getRelativePoint(this.axis);
            if (this.size != null) {
                s = this.coverChildren ? 18 : calcSize(this.size, parentSize, calcParent);
            } else {
                s = defaultSize.getAsInt();
                this.sizeCalculated = s > 0;
            }
        } else {
            // calc start, end and size
            if (this.start == null && this.end == null) {
                p = 0;
                if (this.size == null) {
                    s = defaultSize.getAsInt();
                    this.sizeCalculated = s > 0;
                } else {
                    s = calcSize(this.size, parentSize, calcParent);
                }
                this.posCalculated = true;
            } else {
                if (this.size == null) {
                    if (this.start != null && this.end != null) {
                        p = calcPoint(this.start, -1, parentSize, calcParent);
                        boolean b = this.posCalculated;
                        this.posCalculated = false;
                        int x2 = calcPoint(this.end, -1, parentSize, calcParent);
                        s = Math.abs(parentSize - x2 - p);
                        this.posCalculated &= b;
                        this.sizeCalculated |= this.posCalculated;
                    } else {
                        s = defaultSize.getAsInt();
                        this.sizeCalculated = s > 0;
                        if (this.start == null) {
                            p = calcPoint(this.end, s, parentSize, calcParent);
                            p -= s;
                            this.posCalculated &= this.sizeCalculated;
                        } else {
                            p = calcPoint(this.start, s, parentSize, calcParent);
                            this.posCalculated &= (this.sizeCalculated || !needsSize(this.start));
                        }
                    }
                } else if (this.start != null) {
                    s = calcSize(this.size, parentSize, calcParent);
                    p = calcPoint(this.start, s, parentSize, calcParent);
                    this.posCalculated &= (this.sizeCalculated || !needsSize(this.start));
                } else {
                    s = calcSize(this.size, parentSize, calcParent);
                    p = calcPoint(this.end, s, parentSize, calcParent);
                    p = parentSize - p - s;
                    this.posCalculated &= this.sizeCalculated;
                }
            }
        }

        // apply padding and margin to size
        if (this.sizeCalculated && calcParent && ((this.size != null && this.size.isRelative()) ||
                (this.start != null && this.end != null && (this.start.isRelative() || this.end.isRelative())))) {
            Box padding = relativeTo.getArea().getPadding();
            Box margin = area.getMargin();
            s = Math.min(s, parentSize - padding.getTotal(this.axis) - margin.getTotal(this.axis));

            /*if (!calcParent || (this.size != null && !this.size.isRelative())) {
                area.setRelativePoint(this.axis, p);
            } else {
                area.setRelativePoint(this.axis, Math.max(p, padding.getStart(this.axis) + margin.getStart(this.axis)));
                s = Math.min(s, parentSize - padding.getTotal(this.axis) - margin.getTotal(this.axis));
            }*/

        }
        area.setRelativePoint(this.axis, p);
        area.setPoint(this.axis, p + relativeTo.getArea().x); // temporary
        area.setSize(this.axis, s);
    }

    public int postApply(Area area, Area relativeTo, int p0, int p1) {
        int moveAmount = 0;
        // calculate width and recalculate x based on the new width
        int s = p1 - p0, p;
        area.setSize(this.axis, s);
        this.sizeCalculated = true;
        if (!isPosCalculated()) {
            if (this.start != null) {
                p = calcPoint(this.start, s, relativeTo.getSize(this.axis), true);
            } else if (this.end != null) {
                p = calcPoint(this.end, s, relativeTo.getSize(this.axis), true);
                p = relativeTo.getSize(this.axis) - p - s;
            } else {
                p = area.getRelativePoint(this.axis) + p0/* + area.getMargin().getStart(this.axis)*/;
                if (!this.cancelAutoMovement) {
                    moveAmount = -p0;
                }
            }
            area.setRelativePoint(this.axis, p);
            this.posCalculated = true;
        }
        return moveAmount;
    }

    public void applyMarginAndPaddingToPos(Area area, Area relativeTo) {
        if (isMarginPaddingApplied()) return;
        setMarginPaddingApplied(true);
        int o = area.getMargin().getStart(this.axis) + relativeTo.getPadding().getStart(this.axis);
        if (o == 0) return;
        if (this.start != null && !this.start.isRelative()) return;
        if (this.end != null && !this.end.isRelative() && (this.size == null || !this.size.isRelative())) return;
        area.setRelativePoint(this.axis, area.getRelativePoint(this.axis) + o);
    }

    private int calcSize(Unit s, int parentSize, boolean parentSizeCalculated) {
        if (this.coverChildren) return 18;
        float val = s.getValue();
        if (s.isRelative()) {
            if (!parentSizeCalculated) return (int) val;
            val *= parentSize;
        }
        this.sizeCalculated = true;
        return (int) val;
    }

    public int calcPoint(Unit p, int width, int parentSize, boolean parentSizeCalculated) {
        float val = p.getValue();
        if (!parentSizeCalculated && (p == this.end || p.isRelative())) return (int) val;
        if (p.isRelative()) {
            val = parentSize * val;
            float anchor = p.getAnchor();
            if (width > 0 && anchor != 0) {
                val -= width * anchor;
            }
            if (p.getOffset() != 0) {
                val += p.getOffset();
            }
        }
        this.posCalculated = true;
        return (int) val;
    }

    protected Unit getStart() {
        if (this.start == null) {
            Unit u = null;
            if (this.p1.type == Unit.UNUSED) u = this.p1;
            else if (this.p2.type == Unit.UNUSED) u = this.p2;
            else if (!this.defaultMode) {
                if (this.end.type == Unit.DEFAULT) {
                    u = this.end;
                    this.end = null;
                } else if (this.size.type == Unit.DEFAULT) {
                    u = this.size;
                    this.size = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            this.start = u;
            this.start.reset();
        }
        this.start.type = this.defaultMode ? Unit.DEFAULT : Unit.START;
        return this.start;
    }

    protected Unit getEnd() {
        if (this.end == null) {
            Unit u = null;
            if (this.p1.type == Unit.UNUSED) u = this.p1;
            else if (this.p2.type == Unit.UNUSED) u = this.p2;
            else if (!this.defaultMode) {
                if (this.start.type == Unit.DEFAULT) {
                    u = this.start;
                    this.start = null;
                } else if (this.size.type == Unit.DEFAULT) {
                    u = this.size;
                    this.size = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            this.end = u;
            this.end.reset();
        }
        this.end.type = this.defaultMode ? Unit.DEFAULT : Unit.END;
        return this.end;
    }

    protected Unit getSize() {
        if (this.size == null) {
            Unit u = null;
            if (this.p1.type == Unit.UNUSED) u = this.p1;
            else if (this.p2.type == Unit.UNUSED) u = this.p2;
            else if (!this.defaultMode) {
                if (this.end.type == Unit.DEFAULT) {
                    u = this.end;
                    this.end = null;
                } else if (this.start.type == Unit.DEFAULT) {
                    u = this.start;
                    this.start = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            this.size = u;
            this.size.reset();
        }
        this.size.type = this.defaultMode ? Unit.DEFAULT : Unit.SIZE;
        return this.size;
    }
}
