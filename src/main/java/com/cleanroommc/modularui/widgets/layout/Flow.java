package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.AbstractParentWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class Flow extends ParentWidget<Flow> implements ILayoutWidget, IExpander {

    public static Flow row() {
        return new Flow(GuiAxis.X);
    }

    public static Flow column() {
        return new Flow(GuiAxis.Y);
    }

    /**
     * The main axis on which to align children.
     */
    private final GuiAxis axis;
    /**
     * How the children should be laid out on the main axis.
     */
    private Alignment.MainAxis maa = Alignment.MainAxis.START;
    /**
     * How the children should be laid out on the cross axis.
     */
    private Alignment.CrossAxis caa = Alignment.CrossAxis.CENTER;
    /**
     * Additional space between each child on main axis.
     * Does not work with {@link Alignment.MainAxis#SPACE_BETWEEN} and {@link Alignment.MainAxis#SPACE_AROUND}.
     */
    private int spaceBetween = 0;

    public Flow(GuiAxis axis) {
        this.axis = axis;
        sizeRel(1f, 1f);
    }

    @Override
    public void layoutWidgets() {
        if (!hasChildren()) return;
        boolean hasSize = resizer().isSizeCalculated(this.axis);
        int size = getArea().getSize(axis);
        Box padding = getArea().getPadding();
        Alignment.MainAxis maa = this.maa;
        if (!hasSize && maa != Alignment.MainAxis.START) {
            if (flex().dependsOnChildren(axis)) {
                maa = Alignment.MainAxis.START;
            } else {
                throw new IllegalStateException("Alignment.MainAxis other than start need the size to be calculated!");
            }
        }
        if (maa == Alignment.MainAxis.SPACE_BETWEEN && getChildren().size() == 1) {
            maa = Alignment.MainAxis.CENTER;
        }
        int space = this.spaceBetween;

        int totalSize = 0;
        int expandedAmount = 0;
        int amount = 0;

        // calculate total size and maximum width
        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasPos(this.axis)) continue;
            amount++;
            if (widget.flex().isExpanded()) {
                expandedAmount++;
                totalSize += widget.getArea().getMargin().getTotal(this.axis);
                continue;
            }
            totalSize += widget.getArea().requestedSize(this.axis);
        }

        if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
            if (expandedAmount > 0) {
                maa = Alignment.MainAxis.START;
            } else {
                space = 0;
            }
        }
        totalSize += space * (getChildren().size() - 1);

        if (expandedAmount > 0 && hasSize) {
            int newSize = (size - totalSize - padding.getTotal(this.axis)) / expandedAmount;
            for (IWidget widget : getChildren()) {
                // exclude self positioned (Y) children
                if (widget.flex().hasPos(this.axis)) continue;
                if (widget.flex().isExpanded()) {
                    widget.getArea().setSize(this.axis, newSize);
                    widget.resizer().setSizeResized(this.axis, true);
                }
            }
        }

        // calculate start pos
        int lastP = padding.getStart(this.axis);
        if (hasSize) {
            if (maa == Alignment.MainAxis.CENTER) {
                lastP = (int) (size / 2f - totalSize / 2f);
            } else if (maa == Alignment.MainAxis.END) {
                lastP = size - totalSize;
            }
        }

        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasPos(this.axis)) continue;
            Box margin = widget.getArea().getMargin();

            // set calculated relative Y pos and set bottom margin for next widget
            widget.getArea().setRelativePoint(this.axis, lastP + margin.getStart(this.axis));
            widget.resizer().setPosResized(this.axis, true);
            widget.resizer().setMarginPaddingApplied(this.axis, true);

            lastP += widget.getArea().requestedSize(this.axis) + space;
            if (hasSize && maa == Alignment.MainAxis.SPACE_BETWEEN) {
                lastP += (size - totalSize) / (getChildren().size() - 1);
            }
        }
    }

    @Override
    public void postLayoutWidgets() {
        GuiAxis other = this.axis.getOther();
        int width = getArea().getSize(other);
        Box padding = getArea().getPadding();
        boolean hasWidth = resizer().isSizeCalculated(other);
        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasPos(this.axis)) continue;
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned (X) children in X
            if (!widget.flex().hasPos(other) && widget.resizer().isSizeCalculated(other)) {
                int x = margin.getStart(other) + padding.getStart(other);
                if (hasWidth) {
                    if (this.caa == Alignment.CrossAxis.CENTER) {
                        x = (int) (width / 2f - widget.getArea().getSize(other) / 2f);
                    } else if (this.caa == Alignment.CrossAxis.END) {
                        x = width - widget.getArea().getSize(other) - margin.getEnd(other) - padding.getStart(other);
                    }
                }
                widget.getArea().setRelativePoint(other, x);
                widget.resizer().setPosResized(other, true);
                widget.resizer().setMarginPaddingApplied(other, true);
            }
        }
    }

    public Flow crossAxisAlignment(Alignment.CrossAxis caa) {
        this.caa = caa;
        return this;
    }

    public Flow mainAxisAlignment(Alignment.MainAxis maa) {
        this.maa = maa;
        return this;
    }

    public Flow childPadding(int spaceBetween) {
        this.spaceBetween = spaceBetween;
        return this;
    }

    public GuiAxis getAxis() {
        return axis;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return this.axis;
    }
}
