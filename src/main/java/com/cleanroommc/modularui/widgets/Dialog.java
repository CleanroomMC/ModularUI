package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;

import java.util.function.Consumer;

public class Dialog<T> extends ModularPanel {

    private final Consumer<T> resultConsumer;
    private boolean draggable = false;
    private boolean disablePanelsBelow = true;
    private boolean closeOnOutOfBoundsClick = false;

    public Dialog(GuiContext context, Consumer<T> resultConsumer) {
        super(context);
        this.resultConsumer = resultConsumer;
    }

    public void closeWith(T result) {
        if (this.resultConsumer != null) {
            this.resultConsumer.accept(result);
        }
        animateClose();
    }

    @Override
    public boolean isDraggable() {
        return draggable;
    }

    @Override
    public boolean disablePanelsBelow() {
        return disablePanelsBelow;
    }

    @Override
    public boolean closeOnOutOfBoundsClick() {
        return closeOnOutOfBoundsClick;
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
