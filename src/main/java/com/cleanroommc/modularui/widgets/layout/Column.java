package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class Column extends ParentWidget<Column> implements ILayoutWidget, IExpander {

    private Alignment.MainAxis maa = Alignment.MainAxis.START;
    private Alignment.CrossAxis caa = Alignment.CrossAxis.CENTER;

    public Column() {
        sizeRel(1f, 1f);
    }

    @Override
    public void layoutWidgets() {
        boolean hasHeight = resizer().isHeightCalculated();
        int height = getArea().height;
        Box padding = getArea().getPadding();
        Alignment.MainAxis maa = this.maa;
        if (!hasHeight && maa != Alignment.MainAxis.START) {
            if (flex().yAxisDependsOnChildren()) {
                maa = Alignment.MainAxis.START;
            } else {
                throw new IllegalStateException("Alignment.MainAxis other than start need the height to be calculated!");
            }
        }

        int totalHeight = 0;
        int expandedAmount = 0;

        // TODO children needs width calculated
        // calculate total height and maximum width
        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasYPos()) continue;
            if (widget.flex().isExpanded()) {
                expandedAmount++;
                totalHeight += widget.getArea().getMargin().vertical();
                continue;
            }
            totalHeight += widget.getArea().requestedHeight();
        }

        if (expandedAmount > 0 && hasHeight) {
            int newHeight = (height - totalHeight - padding.vertical()) / expandedAmount;
            for (IWidget widget : getChildren()) {
                // exclude self positioned (Y) children
                if (widget.flex().hasYPos()) continue;
                if (widget.flex().isExpanded()) {
                    widget.getArea().height = newHeight;
                    widget.resizer().setHeightResized(true);
                }
            }
            if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
                maa = Alignment.MainAxis.START;
            }
        }

        // calculate start y
        int lastY = padding.top;
        if (hasHeight) {
            if (maa == Alignment.MainAxis.CENTER) {
                lastY = (int) (height / 2f - totalHeight / 2f);
            } else if (maa == Alignment.MainAxis.END) {
                lastY = height - totalHeight;
            }
        }

        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasYPos()) continue;
            Box margin = widget.getArea().getMargin();

            // set calculated relative Y pos and set bottom margin for next widget
            widget.getArea().ry = lastY + margin.top;
            widget.resizer().setYResized(true);
            widget.resizer().setYMarginPaddingApplied(true);

            lastY += widget.getArea().requestedHeight();
            if (hasHeight && maa == Alignment.MainAxis.SPACE_BETWEEN) {
                lastY += (height - totalHeight) / (getChildren().size() - 1);
            }
        }
    }

    @Override
    public void postLayoutWidgets() {
        int width = getArea().width;
        Box padding = getArea().getPadding();
        boolean hasWidth = resizer().isWidthCalculated();
        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasYPos()) continue;
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned (X) children in X
            if (!widget.flex().hasXPos() && widget.resizer().isWidthCalculated()) {
                int x = margin.left + padding.left;
                if (hasWidth) {
                    if (this.caa == Alignment.CrossAxis.CENTER) {
                        x = (int) (width / 2f - widget.getArea().width / 2f);
                    } else if (this.caa == Alignment.CrossAxis.END) {
                        x = width - widget.getArea().width - margin.right - padding.left;
                    }
                }
                widget.getArea().rx = x;
                widget.resizer().setXResized(true);
                widget.resizer().setXMarginPaddingApplied(true);
            }
        }
    }

    public Column crossAxisAlignment(Alignment.CrossAxis caa) {
        this.caa = caa;
        return this;
    }

    public Column mainAxisAlignment(Alignment.MainAxis maa) {
        this.maa = maa;
        return this;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return GuiAxis.Y;
    }
}
