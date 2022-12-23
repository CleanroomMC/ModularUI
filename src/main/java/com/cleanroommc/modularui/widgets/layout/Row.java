package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.CrossAxisAlignment;
import com.cleanroommc.modularui.api.ILayoutWidget;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.MainAxisAlignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class Row extends ParentWidget<Row> implements ILayoutWidget {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.START;

    public Row() {
        flex().startDefaultMode()
                .size(1f, 1f)
                .endDefaultMode();
    }

    @Override
    public void layoutWidgets() {
        int width = getArea().width;
        int height = getArea().height;
        Box padding = getArea().getPadding();

        int maxHeight = 0;
        int totalWidth = 0;

        for (IWidget widget : getChildren()) {
            // exclude self positioned (X) children
            if (widget.flex().hasXPos()) continue;
            totalWidth += widget.getArea().requestedWidth();
            maxHeight = Math.max(maxHeight, widget.getArea().requestedHeight());
        }

        // calculate start y
        int lastX = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastX = (int) (width / 2f - totalWidth / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastX = width - totalWidth;
        }
        lastX = Math.max(lastX, padding.left) - getArea().getMargin().left;

        for (IWidget widget : getChildren()) {
            // exclude self positioned (X) children
            if (widget.flex().hasXPos()) continue;
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned (Y) children in Y
            if (!widget.flex().hasYPos()) {
                int y = 0;
                if (caa == CrossAxisAlignment.CENTER) {
                    y = (int) (height / 2f - widget.getArea().height / 2f);
                } else if (caa == CrossAxisAlignment.END) {
                    y = height - widget.getArea().height;
                }
                y = Math.max(y, padding.top);
                widget.flex().setRelativeY(y);
            }

            // set calculated relative Y pos and set bottom margin for next widget
            widget.flex().setRelativeX(lastX + margin.left);

            lastX += widget.getArea().requestedWidth();
            if (maa == MainAxisAlignment.SPACE_BETWEEN) {
                lastX += (width - totalWidth) / (getChildren().size() - 1);
            }
        }
    }

    public Row crossAxisAlignment(CrossAxisAlignment caa) {
        this.caa = caa;
        return this;
    }

    public Row mainAxisAlignment(MainAxisAlignment maa) {
        this.maa = maa;
        return this;
    }
}
