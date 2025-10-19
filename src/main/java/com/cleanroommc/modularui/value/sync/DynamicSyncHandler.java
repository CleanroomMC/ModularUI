package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This sync handler calls a function on client and server which creates a widget after being notified. The widget is then handed over to a
 * linked {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
 */
public class DynamicSyncHandler extends SyncHandler {

    private IWidgetProvider widgetProvider;
    private Consumer<IWidget> onWidgetUpdate;

    private IPacketWriter lastRejectedPacket;
    private IWidget lastRejectedWidget;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            updateWidget(parseWidget(buf));
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            // do nothing with the widget on server side
            parseWidget(buf);
        }
    }

    @Override
    public void init(String key, PanelSyncManager syncManager) {
        super.init(key, syncManager);
        if (this.lastRejectedPacket != null) {
            notifyUpdate(this.lastRejectedPacket);
            this.lastRejectedPacket = null;
        }
    }

    private IWidget parseWidget(PacketBuffer buf) {
        getSyncManager().allowTemporarySyncHandlerRegistration(true);
        IWidget widget = this.widgetProvider.createWidget(getSyncManager(), buf);
        getSyncManager().allowTemporarySyncHandlerRegistration(false);
        // collects any unregistered sync handlers
        // since the sync manager is currently locked and we no longer allow bypassing the lock it will crash if it finds any
        int unregistered = WidgetTree.countUnregisteredSyncHandlers(getSyncManager(), widget);
        if (unregistered > 0) {
            throw new IllegalStateException("Widgets created by DynamicSyncHandler can't have implicitly registered sync handlers. All" +
                    "sync handlers must be registered with a variant of 'PanelSyncManager#getOrCreateSyncHandler(...)'.");
        }
        return widget;
    }

    private void updateWidget(IWidget widget) {
        if (this.onWidgetUpdate == null) {
            // no dynamic widget is yet attached
            // store for later
            // also ignore previous stored widget
            this.lastRejectedWidget = widget;
        } else {
            this.onWidgetUpdate.accept(widget);
        }
    }

    /**
     * Notifies the sync handler to create a new widget. It is allowed to call this method before this sync handler is initialised.
     * The packet will be cached until the sync handler is initialised. Only the last call of this method, while this sync handler is not
     * initialised is effective.
     *
     * @param packetWriter data to pass to the function
     */
    public void notifyUpdate(IPacketWriter packetWriter) {
        if (!isValid()) {
            // sync handler not yet initialised
            // store for later
            // also ignore previous stored packet
            this.lastRejectedPacket = packetWriter;
            return;
        }
        IWidget widget = parseWidget(packetWriter.toPacket());
        if (getSyncManager().isClient()) {
            updateWidget(widget);
        }
        sync(0, packetWriter);
    }

    /**
     * Sets a widget creator which is called on client and server. {@link SyncHandler}s can be created here using
     * {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}. Returning null in the function will not update the widget.
     * On client side the result is handed over to a linked {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     *
     * @param widgetProvider the widget creator function
     * @return this
     * @see IWidgetProvider
     */
    public DynamicSyncHandler widgetProvider(IWidgetProvider widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }

    /**
     * An internal function which is used to link the {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     */
    @ApiStatus.Internal
    public void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate) {
        this.onWidgetUpdate = onWidgetUpdate;
        if (this.onWidgetUpdate != null && this.lastRejectedWidget != null) {
            this.onWidgetUpdate.accept(this.lastRejectedWidget);
            this.lastRejectedWidget = null;
        }
    }

    public interface IWidgetProvider {

        /**
         * This is the function which creates a widget on client and server.
         * In this method sync handlers can only be registered with {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}.
         *
         * @param syncManager the sync manager of the current panel
         * @param buf         data which was passed in the notify method
         * @return a new widget or null if widget shouldn't be updated
         */
        @Nullable IWidget createWidget(PanelSyncManager syncManager, PacketBuffer buf);
    }
}
