package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChangeableWidget extends Widget implements ISyncedWidget, IWidgetParent {

    private final List<Widget> child = new ArrayList<>();
    private final Supplier<Widget> widgetSupplier;

    /**
     * Creates a widget which child can be changed dynamically.
     * Call {@link #notifyChange(boolean, boolean)} to notify the widget for a change.
     *
     * @param widgetSupplier widget to supply. Can return null
     */
    public ChangeableWidget(Supplier<Widget> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
    }

    @Override
    public void onInit() {
        notifyChange(false, false);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (child.isEmpty()) {
            return Size.ZERO;
        }
        return child.get(0).getSize();
    }

    /**
     * Notifies the widget that the child probably changed.
     * Only executed on server and synced to client. This method is preferred!
     */
    public void notifyChangeServer() {
        notifyChange(true, true);
    }

    public void notifyChange(boolean sync) {
        notifyChange(false, sync);
    }

    /**
     * Notifies the widget that the child probably changed.
     *
     * @param checkServer if it should only be executed on server
     * @param sync        if it should notify the other side
     */
    @Contract("true, false -> fail")
    public void notifyChange(boolean checkServer, boolean sync) {
        if (widgetSupplier == null || !isInitialised()) {
            return;
        }
        if (checkServer) {
            if (isClient()) {
                return;
            }
            sync = true;
        }
        if (sync) {
            if (isClient()) {
                syncToServer(0, NetworkUtils.EMPTY_PACKET);
            } else {
                syncToClient(0, NetworkUtils.EMPTY_PACKET);
            }
        }
        boolean wasEmpty = child.isEmpty();
        removeCurrentChild();
        Widget widget = widgetSupplier.get();
        if (widget != null) {
            widget.initChildren();
            AtomicInteger syncId = new AtomicInteger(1);
            if (widget instanceof IWidgetParent) {
                IWidgetParent.forEachByLayer((IWidgetParent) widget, widget1 -> {
                    if (widget1 instanceof ISyncedWidget) {
                        getWindow().addDynamicSyncedWidget(syncId.getAndIncrement(), (ISyncedWidget) widget1, this);
                    }
                    return false;
                });
            }
            widget.initialize(getWindow(), this, getLayer() + 1);
            child.add(widget);
            checkNeedsRebuild();
        } else if (!wasEmpty) {
            checkNeedsRebuild();
        }
    }

    public void removeCurrentChild() {
        if (!child.isEmpty()) {
            Widget widget = child.get(0);
            widget.setEnabled(false);
            widget.onPause();
            widget.onDestroy();
            child.clear();
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer packetBuffer) throws IOException {
        if (id == 0) {
            notifyChange(false, false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer packetBuffer) throws IOException {
        if (id == 0) {
            notifyChange(false, false);
        }
    }

    @Override
    public List<Widget> getChildren() {
        return child;
    }
}
