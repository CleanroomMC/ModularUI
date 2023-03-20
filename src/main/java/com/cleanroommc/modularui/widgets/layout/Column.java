package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.layout.CrossAxisAlignment;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.layout.MainAxisAlignment;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class Column extends ParentWidget<Column> implements ILayoutWidget {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.CENTER;

    public Column() {
        flex().startDefaultMode()
                .size(1f, 1f)
                .endDefaultMode();
    }

    @Override
    public void layoutWidgets() {
        int height = getArea().height;
        int width = getArea().width;
        Box padding = getArea().getPadding();

        int maxWidth = 0;
        int totalHeight = 0;
        int expandedAmount = 0;

        // calculate total height and maximum width
        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasYPos()) continue;
            maxWidth = Math.max(maxWidth, widget.getArea().requestedWidth());
            if (widget.flex().isExpanded()) {
                expandedAmount++;
                totalHeight += widget.getArea().getMargin().vertical();
                continue;
            }
            totalHeight += widget.getArea().requestedHeight();
        }

        if (expandedAmount > 0) {
            int newHeight = (height - totalHeight) / expandedAmount;
            for (IWidget widget : getChildren()) {
                // exclude self positioned (Y) children
                if (widget.flex().hasYPos()) continue;
                if (widget.flex().isExpanded()) {
                    widget.getArea().h(newHeight);
                }
            }
        }

        // calculate start y
        int lastY = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastY = (int) (height / 2f - totalHeight / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastY = height - totalHeight;
        }
        lastY = Math.max(lastY, padding.top) - getArea().getMargin().top;

        for (IWidget widget : getChildren()) {
            // exclude self positioned (Y) children
            if (widget.flex().hasYPos()) continue;
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned (X) children in X
            if (!widget.flex().hasXPos()) {
                int x = 0;
                if (caa == CrossAxisAlignment.CENTER) {
                    x = (int) (width / 2f - widget.getArea().requestedWidth() / 2f);
                } else if (caa == CrossAxisAlignment.END) {
                    x = width - widget.getArea().requestedWidth();
                }
                x = Math.max(x, padding.left + margin.left);
                widget.getArea().rx = x;
            }

            // set calculated relative Y pos and set bottom margin for next widget
            widget.getArea().ry = lastY + margin.top;

            lastY += widget.getArea().requestedHeight();
            if (maa == MainAxisAlignment.SPACE_BETWEEN) {
                lastY += (height - totalHeight) / (getChildren().size() - 1);
            }
        }
    }

    public Column crossAxisAlignment(CrossAxisAlignment caa) {
        this.caa = caa;
        return this;
    }

    public Column mainAxisAlignment(MainAxisAlignment maa) {
        this.maa = maa;
        return this;
    }
}
