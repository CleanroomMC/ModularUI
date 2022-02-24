package com.cleanroommc.modularui.api.math;

public class Alignment {

    public final float x, y;

    public static final Alignment TopLeft       = new Alignment(-1, -1);
    public static final Alignment TopCenter     = new Alignment(0, -1);
    public static final Alignment TopRight      = new Alignment(1, -1);
    public static final Alignment CenterLeft    = new Alignment(-1, 0);
    public static final Alignment Center        = new Alignment(0, 0);
    public static final Alignment CenterRight   = new Alignment(1, 0);
    public static final Alignment BottomLeft    = new Alignment(-1, 1);
    public static final Alignment BottomCenter  = new Alignment(0, 1);
    public static final Alignment BottomRight   = new Alignment(1, 1);

    public static final Alignment[] ALL = {
            TopLeft,    TopCenter,      TopRight,
            CenterLeft, Center,         CenterRight,
            BottomLeft, BottomCenter,   BottomRight
    };

    public static final Alignment[] CORNERS = {
            TopLeft,    TopRight,
            BottomLeft, BottomRight
    };

    public Alignment(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Pos2d getAlignedPos(Size parent, Size child) {
        //if(parent.width < child.width || parent.height < child.height)
        //    throw new IllegalArgumentException("Parent size can't be smaller than child size");
        float x = (this.x + 1) / 2, y = (this.y + 1) / 2;
        return new Pos2d(parent.width * x - child.width * x, parent.height * y - child.height * y);
    }

    public Pos2d getAlignedPos(Size parent, Size child, EdgeOffset edgeOffset) {
        Pos2d pos = getAlignedPos(parent, child);
        float spaceH = parent.width - child.width, spaceV = parent.height - child.height;
        if(edgeOffset.left + edgeOffset.right > spaceH)
            edgeOffset = new EdgeOffset(spaceH / 2, edgeOffset.top, spaceH / 2, edgeOffset.bottom);
        if(edgeOffset.top + edgeOffset.bottom > spaceV)
            edgeOffset = new EdgeOffset(edgeOffset.left, spaceV / 2, edgeOffset.right, spaceV / 2);
        float x = pos.x, y = pos.y;
        if(x < edgeOffset.left)
            x = edgeOffset.left;
        else if(parent.width - (x + child.width) < edgeOffset.right)
            x = parent.width - child.width - edgeOffset.right;
        if(y < edgeOffset.top)
            y = edgeOffset.top;
        else if(parent.height - (y + child.height) < edgeOffset.bottom)
            y = parent.height - child.height - edgeOffset.bottom;
        return new Pos2d(x, y);
    }
}
