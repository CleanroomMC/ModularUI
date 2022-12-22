package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.CrossAxisAlignment;
import com.cleanroommc.modularui.api.ILayoutWidget;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.MainAxisAlignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Box;

public class Column extends ParentWidget<Column> implements ILayoutWidget {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.START;

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

        for (IWidget widget : getChildren()) {
            totalHeight += widget.getArea().requestedHeight();
            maxWidth = Math.max(maxWidth, widget.getArea().requestedWidth());
        }

        int selfY = 0;
        if (maa == MainAxisAlignment.CENTER) {
            selfY = (int) (height / 2f - totalHeight / 2f);
        } else if (maa == MainAxisAlignment.END) {
            selfY = height - totalHeight;
        }
        int lastY = selfY;
        lastY = Math.max(lastY, padding.top);

        for (IWidget widget : getChildren()) {
            Box margin = widget.getArea().getMargin();
            int x = 0;
            if (caa == CrossAxisAlignment.CENTER) {
                x = (int) (width / 2f - widget.getArea().width / 2f);
            } else if (caa == CrossAxisAlignment.END) {
                x = width - widget.getArea().width;
            }
            x = Math.max(x, padding.left + margin.left);
            widget.flex().setRelativePos(x, lastY + widget.getArea().getMargin().top);
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
