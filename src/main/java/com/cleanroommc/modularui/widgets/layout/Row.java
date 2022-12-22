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
            totalWidth += widget.getArea().requestedWidth();
            maxHeight = Math.max(maxHeight, widget.getArea().requestedHeight());
        }

        int lastX = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastX = (int) (width / 2f - totalWidth / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastX = width - totalWidth;
        }
        lastX = Math.max(lastX, padding.left);

        for (IWidget widget : getChildren()) {
            int y = 0;
            if (caa == CrossAxisAlignment.CENTER) {
                y = (int) (height / 2f - widget.getArea().requestedHeight() / 2f);
            } else if (caa == CrossAxisAlignment.END) {
                y = height - widget.getArea().requestedHeight();
            }
            y = Math.max(y, padding.top);
            widget.flex().setRelativePos(lastX + widget.getArea().getMargin().left, y);
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
