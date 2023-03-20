package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.GuiAxis;
import com.cleanroommc.modularui.widget.sizer.Unit;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class SliderWidget extends Widget<SliderWidget> implements Interactable {

    private IDrawable stopperDrawable = IDrawable.EMPTY;
    private IDrawable handleDrawable = GuiTextures.BUTTON;
    private GuiAxis axis = GuiAxis.X;
    private DoubleList stopper;
    private final Unit sliderWidth = new Unit(), sliderHeight = new Unit();
    private final Area sliderArea = new Area();
    private double min, max;
    private double value;
    private boolean dragging = false;

    public SliderWidget() {
        sliderHeight(1f).sliderWidth(6);
        listenGuiAction((IGuiAction.MouseReleased) button -> {
            boolean val = this.dragging;
            this.dragging = false;
            return val;
        });
        bounds(0, 100);
        this.value = 50;
    }

    @Override
    public void draw(GuiContext context) {
        if (this.handleDrawable != null) {
            this.handleDrawable.draw(this.sliderArea);
        }
    }

    @Override
    public void resize() {
        super.resize();
        float sw = this.sliderWidth.getValue();
        if (this.sliderWidth.isRelative()) sw *= getArea().width;
        float sh = this.sliderHeight.getValue();
        if (this.sliderHeight.isRelative()) sh *= getArea().height;
        this.sliderArea.setSize((int) sw, (int) sh);
        this.sliderArea.setPoint(this.axis.getOther(), 0);
        setValue(this.value);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        int p = getContext().getMouse(this.axis) - getArea().getPoint(this.axis);
        setValue(posToValue(p));
        this.dragging = true;
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        if (this.dragging) {
            onMousePressed(mouseButton);
        }
    }

    public double posToValue(int p) {
        double v = (p - this.sliderArea.getSize(this.axis) / 2D) / (double) (getArea().getSize(this.axis) - this.sliderArea.getSize(this.axis));
        return v * (this.max - this.min) + this.min;
    }

    public int valueToSliderPos(double value) {
        value -= min;
        value /= (max - min);
        int ss = this.sliderArea.getSize(this.axis);
        return (int) (value * (getArea().getSize(this.axis) - ss));
    }

    public void setValue(double value) {
        if (this.stopper != null && !this.stopper.isEmpty()) {
            double lastDistance = Double.MAX_VALUE;
            boolean found = false;
            for (int i = 0; i < this.stopper.size(); i++) {
                double dist = Math.abs(this.stopper.get(i) - value);
                if (dist < lastDistance) {
                    lastDistance = dist;
                } else if (dist > lastDistance) {
                    value = this.stopper.get(i - 1);
                    found = true;
                    break;
                }
            }
            if (!found && lastDistance < Double.MAX_VALUE) {
                value = this.stopper.get(this.stopper.size() - 1);
            }
        }
        this.value = MathHelper.clamp(value, this.min, this.max);
        this.sliderArea.setPoint(this.axis, valueToSliderPos(this.value));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isDragging() {
        return dragging;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + " # " + this.value;
    }

    public SliderWidget bounds(double min, double max) {
        this.max = Math.max(min, max);
        this.min = Math.min(min, max);
        return this;
    }

    public SliderWidget stopper(DoubleList stopper) {
        if (this.stopper == null) this.stopper = new DoubleArrayList();
        this.stopper.addAll(stopper);
        this.stopper.sort(Double::compare);
        return this;
    }

    public SliderWidget stopper(double... stopper) {
        if (this.stopper == null) this.stopper = new DoubleArrayList();
        for (double stop : stopper) {
            this.stopper.add(stop);
        }
        this.stopper.sort(Double::compare);
        return this;
    }

    public SliderWidget setAxis(GuiAxis axis) {
        this.axis = axis;
        return this;
    }

    public SliderWidget sliderWidth(int w) {
        this.sliderWidth.setValue(w);
        this.sliderWidth.setMeasure(Unit.Measure.PIXEL);
        return this;
    }

    public SliderWidget sliderWidth(float w) {
        this.sliderWidth.setValue(w);
        this.sliderWidth.setMeasure(Unit.Measure.RELATIVE);
        return this;
    }

    public SliderWidget sliderHeight(int h) {
        this.sliderHeight.setValue(h);
        this.sliderHeight.setMeasure(Unit.Measure.PIXEL);
        return this;
    }

    public SliderWidget sliderHeight(float h) {
        this.sliderHeight.setValue(h);
        this.sliderHeight.setMeasure(Unit.Measure.RELATIVE);
        return this;
    }

    public SliderWidget sliderSize(int w, int h) {
        return sliderWidth(w).sliderHeight(h);
    }

    public SliderWidget sliderSize(float w, float h) {
        return sliderWidth(w).sliderHeight(h);
    }
}
