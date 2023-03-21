package com.cleanroommc.modularui.utils;

public class Alignment {

    public final float x, y;

    public static final Alignment TopLeft       = new Alignment(0, 0);
    public static final Alignment TopCenter     = new Alignment(0.5f, 0);
    public static final Alignment TopRight      = new Alignment(1, 0);
    public static final Alignment CenterLeft    = new Alignment(0, 0.5f);
    public static final Alignment Center        = new Alignment(0.5f, 0.5f);
    public static final Alignment CenterRight   = new Alignment(1, 0.5f);
    public static final Alignment BottomLeft    = new Alignment(0, 1);
    public static final Alignment BottomCenter  = new Alignment(0.5f, 1);
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
}
