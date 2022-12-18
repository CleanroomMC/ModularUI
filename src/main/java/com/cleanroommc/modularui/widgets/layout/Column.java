package com.cleanroommc.modularui.widgets.layout;

import com.cleanroommc.modularui.api.CrossAxisAlignment;
import com.cleanroommc.modularui.api.ILayoutWidget;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.MainAxisAlignment;
import com.cleanroommc.modularui.widget.ParentWidget;

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

        int maxWidth = 0;
        int totalHeight = 0;

        for (IWidget widget : getChildren()) {
            totalHeight += widget.getArea().height;
            maxWidth = Math.max(maxWidth, widget.getArea().width);
        }

        int lastY = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastY = (int) (height / 2f - totalHeight / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastY = height - totalHeight;
        }

        for (IWidget widget : getChildren()) {
            int x = 0;
            if (caa == CrossAxisAlignment.CENTER) {
                x = (int) (width / 2f - widget.getArea().width / 2f);
            } else if (caa == CrossAxisAlignment.END) {
                x = width - widget.getArea().width;
            }
            widget.flex().setRelativePos(x, lastY);
            lastY += widget.getArea().height;
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
