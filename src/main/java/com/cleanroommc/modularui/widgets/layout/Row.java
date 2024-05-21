package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class Row extends ParentWidget<Row> implements ILayoutWidget, IExpander {

    private Alignment.MainAxis maa = Alignment.MainAxis.START;
    private Alignment.CrossAxis caa = Alignment.CrossAxis.START;

    public Row() {
        sizeRel(1f, 1f);
    }

    @Override
    public void layoutWidgets() {
        boolean hasWidth = resizer().isWidthCalculated();
        int width = getArea().width;
        Box padding = getArea().getPadding();
        Alignment.MainAxis maa = this.maa;
        if (!hasWidth && maa != Alignment.MainAxis.START) {
            if (flex().yAxisDependsOnChildren()) {
                maa = Alignment.MainAxis.START;
            } else {
                throw new IllegalStateException("Alignment.MainAxis other than start need the width to be calculated!");
            }
        }

        int totalWidth = 0;
        int expandedAmount = 0;

        for (IWidget widget : getChildren()) {
            // exclude self positioned (X) children
            if (widget.flex().hasXPos()) continue;
            if (widget.flex().isExpanded()) {
                expandedAmount++;
                totalWidth += widget.getArea().getMargin().horizontal();
                continue;
            }
            totalWidth += widget.getArea().requestedWidth();
        }

        if (expandedAmount > 0 && hasWidth) {
            int newWidth = (width - totalWidth - padding.horizontal()) / expandedAmount;
            for (IWidget widget : getChildren()) {
                // exclude self positioned (X) children
                if (widget.flex().hasXPos()) continue;
                if (widget.flex().isExpanded()) {
                    widget.getArea().width = newWidth;
                    widget.resizer().setWidthResized(true);
                }
            }
            if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
                maa = Alignment.MainAxis.START;
            }
        }

        // calculate start y
        int lastX = padding.left;
        if (hasWidth) {
            if (maa == Alignment.MainAxis.CENTER) {
                lastX = (int) (width / 2f - totalWidth / 2f);
            } else if (maa == Alignment.MainAxis.END) {
                lastX = width - totalWidth;
            }
        }

        for (IWidget widget : getChildren()) {
            // exclude self positioned (X) children
            if (widget.flex().hasXPos()) continue;
            Box margin = widget.getArea().getMargin();

            // set calculated relative Y pos and set bottom margin for next widget
            widget.getArea().rx = lastX + margin.left;
            widget.resizer().setXResized(true);
            widget.resizer().setXMarginPaddingApplied(true);

            lastX += widget.getArea().requestedWidth();
            if (hasWidth && maa == Alignment.MainAxis.SPACE_BETWEEN) {
                lastX += (width - totalWidth) / (getChildren().size() - 1);
            }
        }
    }

    @Override
    public void postLayoutWidgets() {
        int height = getArea().height;
        Box padding = getArea().getPadding();
        boolean hasHeight = resizer().isWidthCalculated();
        for (IWidget widget : getChildren()) {
            // exclude self positioned (X) children
            if (widget.flex().hasXPos()) continue;
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned (Y) children in Y
            if (!widget.flex().hasYPos()) {
                int y = margin.top + padding.top;
                if (hasHeight) {
                    if (this.caa == Alignment.CrossAxis.CENTER) {
                        y = (int) (height / 2f - widget.getArea().height / 2f);
                    } else if (this.caa == Alignment.CrossAxis.END) {
                        y = height - widget.getArea().height - margin.bottom - padding.bottom;
                    }
                }
                widget.getArea().ry = y;
                widget.resizer().setYResized(true);
                widget.resizer().setYMarginPaddingApplied(true);
            }
        }
    }

    public Row crossAxisAlignment(Alignment.CrossAxis caa) {
        this.caa = caa;
        return this;
    }

    public Row mainAxisAlignment(Alignment.MainAxis maa) {
        this.maa = maa;
        return this;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return GuiAxis.X;
    }
}
