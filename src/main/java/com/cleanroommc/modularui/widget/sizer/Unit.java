package com.cleanroommc.modularui.widget.sizer;

import org.intellij.lang.annotations.MagicConstant;

import java.util.function.DoubleSupplier;

public class Unit {

    public static final byte UNUSED = -2;
    public static final byte DEFAULT = -1;
    public static final byte TOP = 0;
    public static final byte LEFT = 1;
    public static final byte BOTTOM = 2;
    public static final byte RIGHT = 3;
    public static final byte WIDTH = 4;
    public static final byte HEIGHT = 5;

    private boolean autoAnchor = true;
    private float value = 0f;
    private DoubleSupplier valueSupplier = null;
    private Measure measure = Measure.PIXEL;
    private float anchor = 0f;
    //private int max = -1;
    private int offset = 0;

    @MagicConstant(intValues = {UNUSED, DEFAULT, TOP, BOTTOM, LEFT, RIGHT, WIDTH, HEIGHT})
    public byte type = UNUSED;

    public Unit() {
    }

    public void reset() {
        autoAnchor = true;
        value = 0f;
        valueSupplier = null;
        measure = Measure.PIXEL;
        anchor = 0f;
        offset = 0;
    }

    public void setValue(float value) {
        this.value = value;
        this.valueSupplier = null;
    }

    public void setValue(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public float getValue() {
        return valueSupplier == null ? value : (float) valueSupplier.getAsDouble();
    }

    public float getAnchor() {
        return isAutoAnchor() && isRelative() ? getValue() : this.anchor;
    }

    public boolean isAutoAnchor() {
        return autoAnchor;
    }

    public int getOffset() {
        return offset;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setAnchor(float anchor) {
        this.anchor = anchor;
    }

    public void setAutoAnchor(boolean autoAnchor) {
        this.autoAnchor = autoAnchor;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public boolean isRelative() {
        return measure == Measure.RELATIVE;
    }

    public enum Measure {
        PIXEL, RELATIVE
    }
}
