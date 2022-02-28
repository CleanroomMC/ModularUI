package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IWidgetBuilder;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.widget.Widget;
import com.google.common.collect.ImmutableBiMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModularWindow implements IWidgetParent {

    public static Builder builder(Size size) {
        return new Builder(size);
    }

    private ModularUIContext context;
    private final List<Widget> children;
    public final ImmutableBiMap<Integer, ISyncedWidget> syncedWidgets;
    private final List<Interactable> interactionListeners = new ArrayList<>();

    private final Size size;
    private Pos2d pos = Pos2d.ZERO;
    private final Alignment alignment = Alignment.Center;
    private boolean draggable = false;
    private boolean active;

    private boolean needsRebuild = false;

    public ModularWindow(Size size, List<Widget> children) {
        this.size = size;
        this.children = children;

        ImmutableBiMap.Builder<Integer, ISyncedWidget> syncedWidgetBuilder = ImmutableBiMap.builder();
        AtomicInteger i = new AtomicInteger();
        IWidgetParent.forEachByLayer(this, widget -> {
            if (widget instanceof ISyncedWidget) {
                syncedWidgetBuilder.put(i.getAndIncrement(), (ISyncedWidget) widget);
            }
            return false;
        });
        this.syncedWidgets = syncedWidgetBuilder.build();
    }

    protected void initialize(ModularUIContext context) {
        this.context = context;
        for (Widget widget : children) {
            widget.initialize(this, this, 0);
        }
    }

    public void onResize(Size screenSize) {
        this.pos = alignment.getAlignedPos(screenSize, size);
        markNeedsRebuild();
    }

    protected void setActive(boolean active) {
        this.active = active;
    }

    public void update() {
        if (needsRebuild) {
            rebuild();
            needsRebuild = false;
        }
        IWidgetParent.forEachByLayer(this, Widget::onScreenUpdate);
    }

    @SideOnly(Side.CLIENT)
    protected void rebuild() {
        for (Widget child : getChildren()) {
            child.rebuildInternal();
        }
    }

    public void pauseWindow() {
        if (isActive()) {
            setActive(false);
            IWidgetParent.forEachByLayer(this, Widget::onPause);
        }
    }

    public void resumeWindow() {
        if (!isActive()) {
            setActive(true);
            IWidgetParent.forEachByLayer(this, Widget::onResume);
        }
    }

    public void closeWindow() {
        IWidgetParent.forEachByLayer(this, widget -> {
            if (isActive()) {
                widget.onPause();
            }
            widget.onDestroy();
        });
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Pos2d getAbsolutePos() {
        return pos;
    }

    @Override
    public Pos2d getPos() {
        return pos;
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public ModularUIContext getContext() {
        return context;
    }

    public void markNeedsRebuild() {
        this.needsRebuild = true;
    }

    /**
     * The events of the added listeners are always called.
     */
    public void addInteractionListener(Interactable interactable) {
        interactionListeners.add(interactable);
    }

    public List<Interactable> getInteractionListeners() {
        return interactionListeners;
    }

    public static class Builder implements IWidgetBuilder<Builder> {

        private final List<Widget> widgets = new ArrayList<>();
        private Size size = Size.zero();
        private boolean draggable = false;

        private Builder(Size size) {
            this.size = size;
        }

        public Builder setSize(Size size) {
            this.size = size;
            return this;
        }

        public Builder setDraggable(boolean draggable) {
            this.draggable = draggable;
            return this;
        }

        @Override
        public void addWidgetInternal(Widget widget) {
            widgets.add(widget);
        }

        public ModularWindow build() {
            ModularWindow window = new ModularWindow(size, widgets);
            window.draggable = draggable;
            return window;
        }
    }
}
