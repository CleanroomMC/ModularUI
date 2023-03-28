package com.cleanroommc.modularui.widget.sizer;

import java.util.function.IntSupplier;

public class DimensionSizer {

    private final GuiAxis axis;

    private Unit start, end, size;
    private Unit p1, p2;

    private boolean coverChildren = false;
    private boolean cancelAutoMovement = false;
    private boolean defaultMode = false;

    public DimensionSizer(GuiAxis axis) {
        this.axis = axis;
        reset();
    }

    public void reset() {
        this.p1 = new Unit();
        this.p2 = new Unit();
        this.start = null;
        this.end = null;
        this.size = null;
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

    public boolean hasSize() {
        return this.size != null;
    }

    public boolean dependsOnChildren() {
        return this.coverChildren;
    }

    public boolean dependsOnParent() {
        return this.end != null ||
                (this.start != null && this.start.isRelative()) ||
                (this.size != null && this.size.isRelative());
    }

    public void apply(Area area, Area relativeTo, IntSupplier defaultSize) {
        int p, s;
        int parentSize = relativeTo.getSize(this.axis);

        // calc start, end and size
        if (this.start == null && this.end == null) {
            p = 0;
            s = this.size == null ? defaultSize.getAsInt() : calcSize(this.size, parentSize);
        } else {
            if (this.size == null) {
                if (this.start != null && this.end != null) {
                    p = calcPoint(this.start, -1, parentSize);
                    int x2 = calcPoint(this.end, -1, parentSize);
                    s = Math.abs(parentSize - x2 - p);
                } else {
                    s = defaultSize.getAsInt();
                    if (this.start == null) {
                        p = calcPoint(this.end, s, parentSize);
                        p -= s;
                    } else {
                        p = calcPoint(this.start, s, parentSize);
                    }
                }
            } else if (end == null) {
                s = calcSize(this.size, parentSize);
                p = calcPoint(this.start, s, parentSize);
            } else {
                s = calcSize(this.size, parentSize);
                p = calcPoint(this.end, s, parentSize);
                p = parentSize - p - s;
            }
        }

        // apply padding and margin
        Box.SHARED.all(0);
        Box padding = relativeTo.getPadding();
        Box margin = area.getMargin();

        if (parentSize < 1 || (this.size != null && !this.size.isRelative())) {
            area.setRelativePoint(this.axis, p);
        } else {
            area.setRelativePoint(this.axis, Math.max(p, padding.getStart(this.axis) + margin.getStart(this.axis)));
            s = Math.min(s, parentSize - padding.getTotal(this.axis) - margin.getTotal(this.axis));
        }

        p += relativeTo.x;
        area.setSize(this.axis, s);
        area.setPoint(this.axis, p);
    }

    public int postApply(Area area, Area relativeTo, int p0, int p1) {
        int moveAmount = 0;
        // calculate width and recalculate x based on the new width
        int s = p1 - p0, p;
        area.setSize(this.axis, s);
        if (this.start != null) {
            p = calcPoint(this.start, s, relativeTo.getSize(this.axis));
        } else if (this.end != null) {
            p = calcPoint(this.end, s, relativeTo.getSize(this.axis));
            p = relativeTo.getSize(this.axis) - p - s;
        } else {
            p = area.getRelativePoint(this.axis) + p0 + area.getMargin().getStart(this.axis);
            if (!cancelAutoMovement) {
                moveAmount = -p0;
            }
        }
        area.setRelativePoint(this.axis, p);
        return moveAmount;
    }

    private int calcSize(Unit s, int parentSize) {
        float val = s.getValue();
        if (s.isRelative()) {
            return (int) (val * parentSize);
        }
        return (int) val;
    }

    public int calcPoint(Unit p, int width, int parentSize) {
        float val = p.getValue();
        if (p.isRelative()) {
            val = parentSize * val;
        }
        float anchor = p.getAnchor();
        if (width > 0 && anchor != 0) {
            val -= width * anchor;
        }
        if (p.getOffset() != 0) {
            val += p.getOffset();
        }
        return (int) val;
    }

    protected Unit getStart() {
        if (start == null) {
            Unit u = null;
            if (p1.type == Unit.UNUSED) u = p1;
            else if (p2.type == Unit.UNUSED) u = p2;
            else if (!defaultMode) {
                if (end.type == Unit.DEFAULT) {
                    u = end;
                    end = null;
                } else if (size.type == Unit.DEFAULT) {
                    u = size;
                    size = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            start = u;
            start.reset();
        }
        start.type = defaultMode ? Unit.DEFAULT : Unit.START;
        return start;
    }

    protected Unit getEnd() {
        if (end == null) {
            Unit u = null;
            if (p1.type == Unit.UNUSED) u = p1;
            else if (p2.type == Unit.UNUSED) u = p2;
            else if (!defaultMode) {
                if (start.type == Unit.DEFAULT) {
                    u = start;
                    start = null;
                } else if (size.type == Unit.DEFAULT) {
                    u = size;
                    size = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            end = u;
            end.reset();
        }
        end.type = defaultMode ? Unit.DEFAULT : Unit.END;
        return end;
    }

    protected Unit getSize() {
        if (size == null) {
            Unit u = null;
            if (p1.type == Unit.UNUSED) u = p1;
            else if (p2.type == Unit.UNUSED) u = p2;
            else if (!defaultMode) {
                if (end.type == Unit.DEFAULT) {
                    u = end;
                    end = null;
                } else if (start.type == Unit.DEFAULT) {
                    u = start;
                    start = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            size = u;
            size.reset();
        }
        size.type = defaultMode ? Unit.DEFAULT : Unit.SIZE;
        return size;
    }
}
