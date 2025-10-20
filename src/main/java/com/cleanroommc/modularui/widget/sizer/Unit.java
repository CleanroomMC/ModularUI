package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.GuiAxis;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.DoubleSupplier;

@ApiStatus.Internal
public class Unit {

    public enum State {
        UNUSED("", ""),
        START("LEFT", "TOP"),
        END("RIGHT", "BOTTOM"),
        SIZE("WIDTH", "HEIGHT");

        public final String xText, yText;

        State(String xText, String yText) {
            this.xText = xText;
            this.yText = yText;
        }

        public String getText(GuiAxis axis) {
            return axis.isHorizontal() ? this.xText : this.yText;
        }
    }

    public static final byte UNUSED = -2;
    public static final byte DEFAULT = -1;
    public static final byte START = 0;
    public static final byte END = 1;
    public static final byte SIZE = 2;

    private boolean autoAnchor = true;
    private float value = 0f;
    private DoubleSupplier valueSupplier = null;
    private Measure measure = Measure.PIXEL;
    private float anchor = 0f;
    private int offset = 0;

    public State state = State.UNUSED;

    public Unit() {}

    public void reset() {
        this.state = State.UNUSED;
        this.autoAnchor = true;
        this.value = 0f;
        this.valueSupplier = null;
        this.measure = Measure.PIXEL;
        this.anchor = 0f;
        this.offset = 0;
    }

    public void setValue(float value) {
        this.value = value;
        this.valueSupplier = null;
    }

    public void setValue(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public float getValue() {
        return this.valueSupplier == null ? this.value : (float) this.valueSupplier.getAsDouble();
    }

    public float getAnchor() {
        float val = getValue();
        return isAutoAnchor() && isRelative() && val < 1 ? val : this.anchor;
    }

    public boolean isAutoAnchor() {
        return this.autoAnchor;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getAbsOffset() {
        return Math.abs(this.offset);
    }

    public Measure getMeasure() {
        return this.measure;
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

    public boolean isCloseToZero() {
        if (isRelative()) {
            return Math.abs(getValue()) <= 0.01f && Math.abs(getOffset()) < 5;
        }
        return Math.abs(getValue() + getOffset()) < 5;
    }

    public boolean isRelative() {
        return this.measure == Measure.RELATIVE;
    }

    public boolean isUnused() {
        return this.state == State.UNUSED;
    }

    public enum Measure {
        PIXEL, RELATIVE
    }
}
