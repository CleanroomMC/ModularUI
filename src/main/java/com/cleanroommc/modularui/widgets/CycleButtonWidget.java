package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class CycleButtonWidget extends Widget<CycleButtonWidget> implements Interactable {

    private int length = 1;
    private IIntValue<?> intValue;
    private int lastValue = -1;
    private IDrawable[] background = null;
    private IDrawable[] hoverBackground = null;
    private IDrawable[] overlay = null;
    private IDrawable[] hoverOverlay = null;
    private final List<Tooltip> stateTooltip = new ArrayList<>();

    @Override
    public void onInit() {
        if (this.intValue == null) {
            this.intValue = new IntValue(0);
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.intValue = castIfTypeElseNull(syncHandler, IIntValue.class);
        return this.intValue != null;
    }

    private int getState() {
        int val = this.intValue.getIntValue();
        if (val != this.lastValue) {
            setState(val, false);
        }
        return val;
    }

    public void next() {
        int state = getState();
        if (++state == this.length) {
            state = 0;
        }
        setState(state, true);
    }

    public void prev() {
        int state = getState();
        if (--state == -1) {
            state = this.length - 1;
        }
        setState(state, true);
    }

    public void setState(int state, boolean setSource) {
        if (state < 0 || state >= this.length) {
            throw new IndexOutOfBoundsException("CycleButton state out of bounds");
        }
        if (setSource) {
            this.intValue.setIntValue(state);
        }
        this.lastValue = state;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        switch (mouseButton) {
            case 0:
                next();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
            case 1:
                prev();
                Interactable.playButtonClickSound();
                return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public WidgetTheme getWidgetTheme(ITheme theme) {
        return theme.getButtonTheme();
    }

    @Override
    public IDrawable getCurrentBackground(ITheme theme, WidgetTheme widgetTheme) {
        // make sure texture is up-to-date
        int state = getState();
        if (isHovering()) {
            if (this.hoverBackground != null && this.hoverBackground[state] != null) return this.hoverBackground[state];
            return this.background != null && this.background[state] != null ? this.background[state] : super.getCurrentBackground(theme, widgetTheme);
        }
        return this.background != null && this.background[state] != null ? this.background[state] : super.getCurrentBackground(theme, widgetTheme);
    }

    @Override
    public IDrawable getCurrentOverlay(ITheme theme, WidgetTheme widgetTheme) {
        int state = getState();
        if (isHovering()) {
            if (this.hoverOverlay != null && this.hoverOverlay[state] != null) return this.hoverOverlay[state];
            return this.overlay != null && this.overlay[state] != null ? this.overlay[state] : super.getCurrentBackground(theme, widgetTheme);
        }
        return this.overlay != null && this.overlay[state] != null ? this.overlay[state] : super.getCurrentBackground(theme, widgetTheme);
    }

    @Override
    public boolean hasTooltip() {
        int state = getState();
        return super.hasTooltip() || (this.stateTooltip.size() > state && !this.stateTooltip.get(state).isEmpty());
    }

    @Override
    public void markTooltipDirty() {
        super.markTooltipDirty();
        for (Tooltip tooltip : this.stateTooltip) {
            tooltip.markDirty();
        }
        getState();
    }

    @Override
    public @Nullable Tooltip getTooltip() {
        Tooltip tooltip = super.getTooltip();
        if (tooltip == null || tooltip.isEmpty()) {
            return this.stateTooltip.get(getState());
        }
        return tooltip;
    }

    public CycleButtonWidget value(IIntValue<?> value) {
        this.intValue = value;
        setValue(value);
        if (value instanceof IEnumValue<?> enumValue) {
            length(enumValue.getEnumClass().getEnumConstants().length);
        } else if (value instanceof IBoolValue) {
            length(2);
        }
        return this;
    }

    @Deprecated
    public CycleButtonWidget textureGetter(IntFunction<IDrawable> textureGetter) {
        throw new UnsupportedOperationException("'textureGetter()' is no longer supported in CycleButtonWidget. Use 'stateBackground()'");
    }

    @Deprecated
    public CycleButtonWidget texture(UITexture texture) {
        return stateBackground(texture);
    }

    /**
     * Sets the state dependent background. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public CycleButtonWidget stateBackground(UITexture texture) {
        for (int i = 0; i < this.length; i++) {
            float a = 1f / this.length;
            this.background[i] = texture.getSubArea(0, i * a, 1, i * a + a);
        }
        return this;
    }

    /**
     * Sets the state dependent overlay. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public CycleButtonWidget stateOverlay(UITexture texture) {
        for (int i = 0; i < this.length; i++) {
            float a = 1f / this.length;
            this.overlay[i] = texture.getSubArea(0, i * a, 1, i * a + a);
        }
        return this;
    }

    /**
     * Sets the state dependent hover background. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public CycleButtonWidget stateHoverBackground(UITexture texture) {
        for (int i = 0; i < this.length; i++) {
            float a = 1f / this.length;
            this.hoverBackground[i] = texture.getSubArea(0, i * a, 1, i * a + a);
        }
        return this;
    }

    /**
     * Sets the state dependent hover overlay. The images should be vertically stacked images from top to bottom
     * Note: The length must be already set!
     *
     * @param texture background
     * @return this
     */
    public CycleButtonWidget stateHoverOverlay(UITexture texture) {
        for (int i = 0; i < this.length; i++) {
            float a = 1f / this.length;
            this.hoverOverlay[i] = texture.getSubArea(0, i * a, 1, i * a + a);
        }
        return this;
    }

    public CycleButtonWidget stateBackground(int state, IDrawable drawable) {
        this.background = addToArray(this.background, drawable, state);
        return this;
    }

    public CycleButtonWidget stateHoverBackground(int state, IDrawable drawable) {
        this.hoverBackground = addToArray(this.hoverBackground, drawable, state);
        return this;
    }

    public CycleButtonWidget stateOverlay(int state, IDrawable drawable) {
        this.overlay = addToArray(this.overlay, drawable, state);
        return this;
    }

    public CycleButtonWidget stateHoverOverlay(int state, IDrawable drawable) {
        this.hoverOverlay = addToArray(this.hoverOverlay, drawable, state);
        return this;
    }

    public CycleButtonWidget stateBackground(boolean state, IDrawable drawable) {
        this.background = addToArray(this.background, drawable, state ? 1 : 0);
        return this;
    }

    public CycleButtonWidget stateHoverBackground(boolean state, IDrawable drawable) {
        this.hoverBackground = addToArray(this.hoverBackground, drawable, state ? 1 : 0);
        return this;
    }

    public CycleButtonWidget stateOverlay(boolean state, IDrawable drawable) {
        this.overlay = addToArray(this.overlay, drawable, state ? 1 : 0);
        return this;
    }

    public CycleButtonWidget stateHoverOverlay(boolean state, IDrawable drawable) {
        this.hoverOverlay = addToArray(this.hoverOverlay, drawable, state ? 1 : 0);
        return this;
    }

    public <T extends Enum<T>> CycleButtonWidget stateBackground(T state, IDrawable drawable) {
        this.background = addToArray(this.background, drawable, state.ordinal());
        return this;
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverBackground(T state, IDrawable drawable) {
        this.hoverBackground = addToArray(this.hoverBackground, drawable, state.ordinal());
        return this;
    }

    public <T extends Enum<T>> CycleButtonWidget stateOverlay(T state, IDrawable drawable) {
        this.overlay = addToArray(this.overlay, drawable, state.ordinal());
        return this;
    }

    public <T extends Enum<T>> CycleButtonWidget stateHoverOverlay(T state, IDrawable drawable) {
        this.hoverOverlay = addToArray(this.hoverOverlay, drawable, state.ordinal());
        return this;
    }

    /**
     * Adds a line to the tooltip
     */
    public CycleButtonWidget addTooltip(int state, IDrawable tooltip) {
        if (state >= this.stateTooltip.size() || state < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.stateTooltip.get(state).addLine(tooltip);
        return this;
    }

    /**
     * Adds a line to the tooltip
     */
    public CycleButtonWidget addTooltip(int state, String tooltip) {
        return addTooltip(state, IKey.str(tooltip));
    }

    public CycleButtonWidget length(int length) {
        this.length = length;
        // adjust tooltip buffer size
        while (this.stateTooltip.size() < this.length) {
            this.stateTooltip.add(new Tooltip(this));
        }
        while (this.stateTooltip.size() > this.length) {
            this.stateTooltip.remove(this.stateTooltip.size() - 1);
        }
        this.background = checkArray(this.background, length);
        this.overlay = checkArray(this.overlay, length);
        this.hoverBackground = checkArray(this.hoverBackground, length);
        this.hoverOverlay = checkArray(this.hoverOverlay, length);
        return this;
    }

    private static IDrawable[] checkArray(IDrawable[] array, int length) {
        if (array == null) return new IDrawable[length];
        return array.length < length ? Arrays.copyOf(array, length) : array;
    }

    private IDrawable[] addToArray(IDrawable[] array, IDrawable drawable, int index) {
        if (index < 0) throw new IndexOutOfBoundsException();
        if (array == null || index >= array.length) {
            array = new IDrawable[(int) (Math.ceil((index + 1) / 4.0) * 4)];
        }
        array[index] = drawable;
        return array;
    }

    public CycleButtonWidget tooltip(int index, Consumer<Tooltip> builder) {
        builder.accept(this.stateTooltip.get(index));
        return this;
    }

    public CycleButtonWidget tooltipBuilder(int index, Consumer<Tooltip> builder) {
        this.stateTooltip.get(index).tooltipBuilder(builder);
        return this;
    }
}
