package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.animation.IAnimatable;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * A box with four edges.
 * Used for margins and paddings.
 */
public class Box implements IAnimatable<Box> {

    public static final Box SHARED = new Box();
    public static final Box ZERO = new Box();

    protected int left;
    protected int top;
    protected int right;
    protected int bottom;

    public Box all(int all) {
        return this.all(all, all);
    }

    public Box all(int horizontal, int vertical) {
        return this.all(horizontal, horizontal, vertical, vertical);
    }

    public Box all(int left, int right, int top, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        return this;
    }

    public Box left(int left) {
        this.left = left;
        return this;
    }

    public Box top(int top) {
        this.top = top;
        return this;
    }

    public Box right(int right) {
        this.right = right;
        return this;
    }

    public Box bottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public Box set(GuiAxis axis, boolean start, int val) {
        if (axis.isVertical()) {
            if (start) {
                top(val);
            } else {
                bottom(val);
            }
        } else {
            if (start) {
                left(val);
            } else {
                right(val);
            }
        }
        return this;
    }

    public Box set(Box box) {
        return all(box.left, box.right, box.top, box.bottom);
    }

    public int getLeft() {
        return this.left;
    }

    public int getRight() {
        return this.right;
    }

    public int getTop() {
        return this.top;
    }

    public int getBottom() {
        return this.bottom;
    }

    public int vertical() {
        return this.top + this.bottom;
    }

    public int horizontal() {
        return this.left + this.right;
    }

    public int getTotal(GuiAxis axis) {
        return axis.isHorizontal() ? horizontal() : vertical();
    }

    public int getStart(GuiAxis axis) {
        return axis.isHorizontal() ? this.left : this.top;
    }

    public int getEnd(GuiAxis axis) {
        return axis.isHorizontal() ? this.right : this.bottom;
    }

    public void fromJson(JsonObject json) {
        all(JsonHelper.getInt(json, 0, "margin"));
        if (json.has("marginHorizontal")) {
            this.left = json.get("marginHorizontal").getAsInt();
            this.right = this.left;
        }
        if (json.has("marginVertical")) {
            this.top = json.get("marginVertical").getAsInt();
            this.bottom = this.top;
        }
        this.top = JsonHelper.getInt(json, this.top, "marginTop");
        this.bottom = JsonHelper.getInt(json, this.bottom, "marginBottom");
        this.left = JsonHelper.getInt(json, this.left, "marginLeft");
        this.right = JsonHelper.getInt(json, this.right, "marginRight");
    }

    public void toJson(JsonObject json) {
        json.addProperty("marginTop", this.top);
        json.addProperty("marginBottom", this.bottom);
        json.addProperty("marginLeft", this.left);
        json.addProperty("marginRight", this.right);
    }

    @Override
    public Box interpolate(Box start, Box end, float t) {
        this.left = Interpolations.lerp(start.left, end.left, t);
        this.top = Interpolations.lerp(start.top, end.top, t);
        this.right = Interpolations.lerp(start.right, end.right, t);
        this.bottom = Interpolations.lerp(start.bottom, end.bottom, t);
        return this;
    }

    @Override
    public Box copyOrImmutable() {
        return new Box().set(this);
    }

    @Override
    public String toString() {
        return "Box{" +
                "left=" + this.left +
                ", top=" + this.top +
                ", right=" + this.right +
                ", bottom=" + this.bottom +
                '}';
    }

    public boolean isEqual(Box box) {
        return this.left == box.left && this.top == box.top && this.right == box.right && this.bottom == box.bottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return isEqual((Box) o);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.top, this.right, this.bottom);
    }
}