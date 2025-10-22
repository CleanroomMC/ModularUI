package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.screen.ModularPanel;

import java.util.function.Consumer;

public class Dialog<T> extends ModularPanel {

    private final Consumer<T> resultConsumer;
    private boolean draggable = false;
    private boolean disablePanelsBelow = true;
    private boolean closeOnOutOfBoundsClick = false;

    public Dialog(String name) {
        this(name, null);
    }

    public Dialog(String name, Consumer<T> resultConsumer) {
        super(name);
        this.resultConsumer = resultConsumer;
    }

    public void closeWith(T result) {
        if (this.resultConsumer != null) {
            this.resultConsumer.accept(result);
        }
        closeIfOpen();
    }

    @Override
    public boolean isDraggable() {
        return this.draggable;
    }

    @Override
    public boolean disablePanelsBelow() {
        return this.disablePanelsBelow;
    }

    @Override
    public boolean closeOnOutOfBoundsClick() {
        return this.closeOnOutOfBoundsClick;
    }

    public Dialog<T> setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public Dialog<T> setDisablePanelsBelow(boolean disablePanelsBelow) {
        this.disablePanelsBelow = disablePanelsBelow;
        return this;
    }

    public Dialog<T> setCloseOnOutOfBoundsClick(boolean closeOnOutOfBoundsClick) {
        this.closeOnOutOfBoundsClick = closeOnOutOfBoundsClick;
        return this;
    }
}
