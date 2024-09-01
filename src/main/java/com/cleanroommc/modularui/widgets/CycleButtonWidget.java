package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.screen.Tooltip;

import java.util.function.Consumer;

public class CycleButtonWidget extends AbstractCycleButtonWidget<CycleButtonWidget> {

    @Override
    public CycleButtonWidget value(IIntValue<?> value) {
        return super.value(value);
    }

    public CycleButtonWidget stateBackground(int state, IDrawable drawable) {
        this.background = addToArray(this.background, drawable, state);
        return getThis();
    }

    public CycleButtonWidget stateHoverBackground(int state, IDrawable drawable) {
        this.hoverBackground = addToArray(this.hoverBackground, drawable, state);
        return getThis();
    }

    public CycleButtonWidget stateOverlay(int state, IDrawable drawable) {
        this.overlay = addToArray(this.overlay, drawable, state);
        return getThis();
    }

    public CycleButtonWidget stateHoverOverlay(int state, IDrawable drawable) {
        this.hoverOverlay = addToArray(this.hoverOverlay, drawable, state);
        return getThis();
    }

    public CycleButtonWidget stateBackground(boolean state, IDrawable drawable) {
        return stateBackground(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateHoverBackground(boolean state, IDrawable drawable) {
        return stateHoverBackground(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateOverlay(boolean state, IDrawable drawable) {
        return stateOverlay(state ? 1 : 0, drawable);
    }

    public CycleButtonWidget stateHoverOverlay(boolean state, IDrawable drawable) {
        return stateHoverOverlay(state ? 1 : 0, drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateBackground(T state, IDrawable drawable) {
        return stateBackground(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverBackground(T state, IDrawable drawable) {
        return stateHoverBackground(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateOverlay(T state, IDrawable drawable) {
        return stateOverlay(state.ordinal(), drawable);
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverOverlay(T state, IDrawable drawable) {
        return stateHoverOverlay(state.ordinal(), drawable);
    }

    @Override
    public CycleButtonWidget addTooltip(int state, String tooltip) {
        return super.addTooltip(state, tooltip);
    }

    @Override
    public CycleButtonWidget addTooltip(int state, IDrawable tooltip) {
        return super.addTooltip(state, tooltip);
    }

    public CycleButtonWidget length(int length) {
        return stateCount(length);
    }

    @Override
    public CycleButtonWidget stateCount(int stateCount) {
        return super.stateCount(stateCount);
    }

    @Override
    public CycleButtonWidget tooltip(int index, Consumer<Tooltip> builder) {
        return super.tooltip(index, builder);
    }

    @Override
    public CycleButtonWidget tooltipBuilder(int index, Consumer<Tooltip> builder) {
        return super.tooltipBuilder(index, builder);
    }
}
