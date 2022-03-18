package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetBuilder;
import com.cleanroommc.modularui.api.math.CrossAxisAlignment;
import com.cleanroommc.modularui.api.math.MainAxisAlignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;

public class Column extends MultiChildWidget implements IWidgetBuilder<Column> {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.START;
    private int maxHeight = -1;

    @Override
    public void addWidgetInternal(Widget widget) {
        addChild(widget);
    }

    @Override
    protected Size getDefaultSize() {
        if (maxHeight > 0) {
            return new Size(getParent().getSize().width, maxHeight);
        }
        return super.getDefaultSize();
    }

    @Override
    public void layoutChildren() {
        int maxWidth = 0;
        int totalHeight = 0;

        for (Widget widget : getChildren()) {
            totalHeight += widget.getSize().height;
            maxWidth = Math.max(maxWidth, widget.getSize().width);
        }

        int lastY = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastY = (int) (getSize().height / 2f - totalHeight / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastY = getSize().height - totalHeight;
        }

        for (Widget widget : getChildren()) {
            int x = 0;
            if (caa == CrossAxisAlignment.CENTER) {
                x = (int) (maxWidth / 2f - widget.getSize().width / 2f);
            } else if(caa == CrossAxisAlignment.END) {
                x = maxWidth - widget.getSize().width;
            }
            widget.relativePos = new Pos2d(x, lastY);
            lastY += widget.getSize().height;
            if (maa == MainAxisAlignment.SPACE_BETWEEN) {
                lastY += (getSize().height - totalHeight) / (getChildren().size() - 1);
            }
        }
    }

    public Column setAlignment(MainAxisAlignment maa) {
        return setAlignment(maa, caa);
    }

    public Column setAlignment(CrossAxisAlignment caa) {
        return setAlignment(maa, caa);
    }

    public Column setAlignment(MainAxisAlignment maa, CrossAxisAlignment caa) {
        this.maa = maa;
        this.caa = caa;
        return this;
    }

    public Column setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }
}
