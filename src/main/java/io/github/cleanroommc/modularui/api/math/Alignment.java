package io.github.cleanroommc.modularui.api.math;

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
        if(parent.width < child.width || parent.height < child.height)
            throw new IllegalArgumentException("Parent size can't be smaller than child size");
        float x = (this.x + 1) / 2, y = (this.y + 1) / 2;
        return new Pos2d(parent.width * x - child.width * x, parent.height * y - child.height * y);
    }

    public Pos2d getAlignedPos(Size parent, Size child, EdgeInset edgeInset) {
        Pos2d pos = getAlignedPos(parent, child);
        float spaceH = parent.width - child.width, spaceV = parent.height - child.height;
        if(edgeInset.left + edgeInset.right > spaceH)
            edgeInset = new EdgeInset(edgeInset.top, edgeInset.bottom, spaceH / 2, spaceH / 2);
        if(edgeInset.top + edgeInset.bottom > spaceV)
            edgeInset = new EdgeInset(spaceV / 2, spaceV / 2, edgeInset.left, edgeInset.right);
        float x = pos.x, y = pos.y;
        if(x < edgeInset.left)
            x = edgeInset.left;
        else if(parent.width - (x + child.width) < edgeInset.right)
            x = parent.width - child.width - edgeInset.right;
        if(y < edgeInset.top)
            y = edgeInset.top;
        else if(parent.height - (y + child.height) < edgeInset.bottom)
            y = parent.height - child.height - edgeInset.bottom;
        return new Pos2d(x, y);
    }
}
