package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.sync.INumberSyncHandler;
import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class CycleButtonWidget extends Widget<CycleButtonWidget> implements Interactable {

    private int length = 1;
    private IntConsumer setter;
    private IntSupplier getter;
    private IntFunction<IDrawable> textureGetter;
    private IDrawable texture = IDrawable.EMPTY;
    private final List<Tooltip> stateTooltip = new ArrayList<>();

    private INumberSyncHandler<?> syncHandler;

    @Override
    public void onInit() {
        if (textureGetter == null) {
            ModularUI.LOGGER.warn("Texture Getter of {} was not set!", this);
            textureGetter = val -> IDrawable.EMPTY;
        }
        this.texture = textureGetter.apply(getState());
        for (Tooltip tooltip : this.stateTooltip) {
            if (tooltip != null && tooltip.getExcludeArea() == null && ModularUIConfig.placeTooltipNextToPanel()) {
                tooltip.excludeArea(getPanel().getArea());
            }
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof INumberSyncHandler) {
            this.syncHandler = (INumberSyncHandler<?>) syncHandler;
            return true;
        }
        return false;
    }

    private int getState() {
        if (this.syncHandler != null) {
            return this.syncHandler.getCacheAsInt();
        }
        if (this.getter != null) {
            return this.getter.getAsInt();
        }
        return 0;
    }

    public void next() {
        int state = getState();
        if (++state == length) {
            state = 0;
        }
        setState(state);
    }

    public void prev() {
        int state = getState();
        if (--state == -1) {
            state = length - 1;
        }
        setState(state);
    }

    public void setState(int state) {
        if (state < 0 || state >= length) {
            throw new IndexOutOfBoundsException("CycleButton state out of bounds");
        }

        if (this.setter != null) {
            this.setter.accept(state);
        }
        if (this.syncHandler != null) {
            this.syncHandler.updateFromClient(state);
        }

        this.texture = textureGetter.apply(state);
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
    public void draw(GuiContext context) {
        //texture.applyThemeColor();
        texture.draw(0, 0, getArea().w(), getArea().h());
    }

    @Override
    public boolean hasTooltip() {
        int state = getState();
        return super.hasTooltip() || (this.stateTooltip.size() > state && !this.stateTooltip.get(state).isEmpty());
    }

    @Override
    public void markDirty() {
        super.markDirty();
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

    public CycleButtonWidget setter(IntConsumer setter) {
        this.setter = setter;
        return this;
    }

    public CycleButtonWidget getter(IntSupplier getter) {
        this.getter = getter;
        return this;
    }

    public <T extends Enum<T>> CycleButtonWidget forEnum(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        return setter(val -> setter.accept(clazz.getEnumConstants()[val]))
                .getter(() -> getter.get().ordinal())
                .length(clazz.getEnumConstants().length);
    }

    public CycleButtonWidget toggle(BooleanSupplier getter, Consumer<Boolean> setter) {
        return setter(val -> setter.accept(val == 1))
                .getter(() -> getter.getAsBoolean() ? 1 : 0)
                .length(2);
    }

    public CycleButtonWidget textureGetter(IntFunction<IDrawable> textureGetter) {
        this.textureGetter = textureGetter;
        return this;
    }

    public CycleButtonWidget texture(UITexture texture) {
        return textureGetter(val -> {
            float a = 1f / length;
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
            Tooltip tooltip = new Tooltip();
            if (!ModularUIConfig.placeTooltipNextToPanel()) {
                tooltip.excludeArea(getArea());
            }
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
