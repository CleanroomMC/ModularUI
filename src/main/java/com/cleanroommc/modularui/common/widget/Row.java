package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.IWidgetBuilder;
import com.cleanroommc.modularui.api.math.CrossAxisAlignment;
import com.cleanroommc.modularui.api.math.MainAxisAlignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;

public class Row extends MultiChildWidget implements IWidgetBuilder<Row> {

    private MainAxisAlignment maa = MainAxisAlignment.START;
    private CrossAxisAlignment caa = CrossAxisAlignment.START;
    private int maxWidth = -1;

    @Override
    public void addWidgetInternal(Widget widget) {
        addChild(widget);
    }

    @Override
    protected Size getDefaultSize() {
        if (maxWidth > 0) {
            return new Size(maxWidth, getParent().getSize().height);
        }
        return super.getDefaultSize();
    }

    @Override
    public void layoutChildren() {
        int maxHeight = 0;
        int totalWidth = 0;

        for (Widget widget : getChildren()) {
            totalWidth += widget.getSize().width;
            maxHeight = Math.max(maxHeight, widget.getSize().height);
        }

        int lastX = 0;
        if (maa == MainAxisAlignment.CENTER) {
            lastX = (int) (getSize().width / 2f - totalWidth / 2f);
        } else if (maa == MainAxisAlignment.END) {
            lastX = getSize().width - totalWidth;
        }

        for (Widget widget : getChildren()) {
            int y = 0;
            if (caa == CrossAxisAlignment.CENTER) {
                y = (int) (maxHeight / 2f - widget.getSize().height / 2f);
            } else if(caa == CrossAxisAlignment.END) {
                y = maxHeight - widget.getSize().height;
            }
            widget.relativePos = new Pos2d(lastX, y);
            lastX += widget.getSize().width;
            if (maa == MainAxisAlignment.SPACE_BETWEEN) {
                lastX += (getSize().width - totalWidth) / (getChildren().size() - 1);
            }
        }
    }

    public Row setAlignment(MainAxisAlignment maa) {
        return setAlignment(maa, caa);
    }

    public Row setAlignment(CrossAxisAlignment caa) {
        return setAlignment(maa, caa);
    }

    public Row setAlignment(MainAxisAlignment maa, CrossAxisAlignment caa) {
        this.maa = maa;
        this.caa = caa;
        return this;
    }

    public Row setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }
}
