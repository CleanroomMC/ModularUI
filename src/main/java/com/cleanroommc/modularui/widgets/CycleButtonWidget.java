package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class CycleButtonWidget extends Widget<CycleButtonWidget> implements Interactable {

    private int length = 1;
    private IIntValue<?> intValue;
    private int lastValue = -1;
    private IntFunction<IDrawable> textureGetter;
    private IDrawable texture = IDrawable.EMPTY;
    private final List<Tooltip> stateTooltip = new ArrayList<>();

    @Override
    public void onInit() {
        if (this.intValue == null) {
            this.intValue = new IntValue(0);
        }
        if (this.textureGetter == null) {
            ModularUI.LOGGER.warn("Texture Getter of {} was not set!", this);
            this.textureGetter = val -> IDrawable.EMPTY;
        }
        this.texture = this.textureGetter.apply(getState());
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IIntValue) {
            this.intValue = (IIntValue<?>) syncHandler;
            return true;
        }
        return false;
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
        this.texture = this.textureGetter.apply(state);
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
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        super.draw(context, widgetTheme);
        // make sure texture is up-to-date
        getState();
        // draw state texture after background, but before overlay
        this.texture.applyThemeColor(context.getTheme(), getWidgetTheme(context.getTheme()));
        this.texture.draw(context, 0, 0, getArea().w(), getArea().h());
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
        this.texture = this.textureGetter.apply(getState());
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
        if (value instanceof IEnumValue) {
            length(((IEnumValue<?>) value).getEnumClass().getEnumConstants().length);
        } else if (value instanceof IBoolValue) {
            length(2);
        }
        return this;
    }

    public CycleButtonWidget textureGetter(IntFunction<IDrawable> textureGetter) {
        this.textureGetter = textureGetter;
        return this;
    }

    public CycleButtonWidget texture(UITexture texture) {
        return textureGetter(val -> {
            float a = 1f / this.length;
            return texture.getSubArea(0, val * a, 1, val * a + a);
        });
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
        while (this.stateTooltip.size() < this.length) {
            Tooltip tooltip = new Tooltip().excludeArea(getArea());
            this.stateTooltip.add(tooltip);
        }
        while (this.stateTooltip.size() > this.length) {
            this.stateTooltip.remove(this.stateTooltip.size() - 1);
        }
        return this;
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
