package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.MutableSingletonList;
import com.cleanroommc.modularui.widgets.menu.AbstractMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DropdownWidget<T, W extends DropdownWidget<T, W>> extends AbstractMenuButton<W> {

    private final Class<T> valueType;
    private final MutableSingletonList<IWidget> selected = new MutableSingletonList<>();
    private final List<T> values = new ArrayList<>();
    private IValue<T> value;
    private int maxListSize = 100;
    private Function<T, IWidget> toWidget;

    public DropdownWidget(String panelName, Class<T> valueType) {
        super(panelName);
        this.valueType = valueType;
        this.openOnHover = false;
    }

    @Override
    public void onInit() {
        super.onInit();
        setValue(this.value.getValue(), false);
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return selected;
    }

    protected IWidget valueToWidget(T v) {
        if (this.toWidget != null) {
            return this.toWidget.apply(v);
        }
        return IKey.str(String.valueOf(v)).asWidget();
    }

    protected void setValue(T value, boolean updateValue) {
        if (this.selected.hasValue()) {
            this.selected.get().dispose();
        }
        if (updateValue) this.value.setValue(value);
        this.selected.set(valueToWidget(value));
        if (isValid()) {
            this.selected.get().initialise(this, true);
            scheduleResize();
        }
    }

    @Override
    protected Menu<?> createMenu() {
        return new Menu<>()
                .widthRel(1f)
                .coverChildrenHeight()
                .child(new ListWidget<>()
                        .widthRel(1f)
                        .maxSize(this.maxListSize)
                        .children(this.values, v -> new ButtonWidget<>()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .child(valueToWidget(v))
                                .onMousePressed(b -> {
                                    setValue(v, true);
                                    closeMenu(false);
                                    return true;
                                })));
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isValueOfType(this.valueType);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castValueNullable(this.valueType);
    }

    public W value(IValue<T> value) {
        setSyncOrValue(value);
        return getThis();
    }

    public W option(T option) {
        this.values.add(option);
        return getThis();
    }

    public W options(Iterable<T> options) {
        for (T t : options) this.values.add(t);
        return getThis();
    }

    public W options(T... options) {
        this.values.addAll(Arrays.asList(options));
        return getThis();
    }

    public W optionToWidget(Function<T, IWidget> toWidget) {
        this.toWidget = toWidget;
        return getThis();
    }

    public W maxVerticalMenuSize(int maxListSize) {
        this.maxListSize = maxListSize;
        return getThis();
    }

    public W directionUp() {
        this.direction = Direction.UP;
        return getThis();
    }

    public W directionDown() {
        this.direction = Direction.DOWN;
        return getThis();
    }
}
