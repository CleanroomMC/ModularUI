package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChangeableWidget extends Widget implements ISyncedWidget, IWidgetParent {

    private final List<Widget> child = new ArrayList<>();
    @Nullable
    private Widget queuedChild = null;
    private final Supplier<Widget> widgetSupplier;
    private boolean initialised = false;
    private boolean firstTick = true;

    /**
     * Creates a widget which child can be changed dynamically.
     * Call {@link #notifyChangeServer()} to notify the widget for a change.
     *
     * @param widgetSupplier widget to supply. Can return null
     */
    public ChangeableWidget(Supplier<Widget> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (this.child.isEmpty()) {
            return Size.ZERO;
        }
        return this.child.get(0).getSize();
    }

    /**
     * Notifies the widget that the child probably changed.
     * Only executed on server and synced to client. This method is preferred!
     */
    public void notifyChangeServer() {
        if (!isClient()) {
            notifyChange(true);
        }
    }

    private void notifyChange(boolean sync) {
        if (this.widgetSupplier == null || !isInitialised()) {
            return;
        }
        if (sync && !isClient()) {
            syncToClient(0, NetworkUtils.EMPTY_PACKET);
        }
        removeCurrentChild();
        this.queuedChild = this.widgetSupplier.get();
        this.initialised = false;
    }

    private void initQueuedChild() {
        if (this.queuedChild != null) {
            IWidgetParent.forEachByLayer(this.queuedChild, Widget::initChildren);
            AtomicInteger syncId = new AtomicInteger(1);
            IWidgetParent.forEachByLayer(this.queuedChild, widget1 -> {
                if (widget1 instanceof ISyncedWidget) {
                    getWindow().addDynamicSyncedWidget(syncId.getAndIncrement(), (ISyncedWidget) widget1, this);
                }
                return false;
            });
            this.queuedChild.initialize(getWindow(), this, getLayer() + 1);
            this.child.add(this.queuedChild);
            this.initialised = true;
            this.queuedChild = null;
        }
        checkNeedsRebuild();
    }

    public void removeCurrentChild() {
        if (!this.child.isEmpty()) {
            Widget widget = this.child.get(0);
            widget.setEnabled(false);
            IWidgetParent.forEachByLayer(widget, Widget::onPause);
            IWidgetParent.forEachByLayer(widget, Widget::onDestroy);
            this.child.clear();
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (this.firstTick) {
            notifyChangeServer();
            this.firstTick = false;
        }
        if (this.initialised && !this.child.isEmpty()) {
            IWidgetParent.forEachByLayer(this.child.get(0), widget -> {
                if (widget instanceof ISyncedWidget) {
                    ((ISyncedWidget) widget).detectAndSendChanges();
                }
            });
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer packetBuffer) throws IOException {
        if (id == 0) {
            notifyChange(false);
            initQueuedChild();
            syncToServer(1, NetworkUtils.EMPTY_PACKET);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer packetBuffer) throws IOException {
        if (id == 1) {
            initQueuedChild();
        }
    }

    @Override
    public List<Widget> getChildren() {
        return this.child;
    }
}
