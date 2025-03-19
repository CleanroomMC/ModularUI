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

    private IWidgetCreator widgetProvider;
    private Consumer<IWidget> onWidgetUpdate;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            updateWidgets(buf);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            updateWidgets(buf);
        }
    }

    private void updateWidgets(PacketBuffer buf) {
        getSyncManager().allowTemporarySyncHandlerRegistration(true);
        IWidget widget = widgetProvider.createWidget(getSyncManager(), buf);
        getSyncManager().allowTemporarySyncHandlerRegistration(false);
        // collects any unregistered sync handlers
        // since the sync manager is currently locked and we no longer allow bypassing the lock it will crash if it finds any
        WidgetTree.collectSyncValues(getSyncManager(), getSyncManager().getPanelName(), widget, true);
        if (widget != null && this.widgetProvider != null) {
            this.onWidgetUpdate.accept(widget);
        }
    }

    /**
     * Notifies the sync handler to create a new widget.
     *
     * @param packetWriter data to pass to the function
     */
    public void notifyUpdate(IPacketWriter packetWriter) {
        updateWidgets(packetWriter.toPacket());
        sync(0, packetWriter);
    }

    /**
     * Sets a widget creator which is called on client and server. {@link SyncHandler}s can be created here using
     * {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}. Returning null in the function will not update the widget.
     * On client side the result is handed over to a linked {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     *
     * @param widgetProvider the widget creator function
     * @return this
     * @see IWidgetCreator
     */
    public DynamicSyncHandler widgetProvider(IWidgetCreator widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }

    /**
     * An internal function which is used to link the {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     */
    @ApiStatus.Internal
    public DynamicSyncHandler onWidgetUpdate(Consumer<IWidget> onWidgetUpdate) {
        this.onWidgetUpdate = onWidgetUpdate;
        return this;
    }

    /**
     * Sets a notifier to this sync handler.
     *
     * @param notifier notifier object
     * @return this
     * @see Notifier
     */
    public DynamicSyncHandler notifier(Notifier notifier) {
        notifier.setSyncHandler(this);
        return this;
    }

    public interface IWidgetCreator {

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

    /**
     * This exists purely for convenience. This allows creating the sync handler directly in the sync tree.
     * A notifier instance can be created before the tree and is then linked to the sync handler with {@link DynamicSyncHandler#notifier(Notifier)}.
     * The sync handler can then be notified with {@link Notifier#notifyUpdate(IPacketWriter)}.
     */
    public static class Notifier {

        private DynamicSyncHandler syncHandler;

        private void setSyncHandler(DynamicSyncHandler syncHandler) {
            this.syncHandler = syncHandler;
        }

        public void unbindSyncHandler() {
            this.syncHandler = null;
        }

        public void notifyUpdate(IPacketWriter packetWriter) {
            if (syncHandler != null && syncHandler.isValid()) {
                syncHandler.notifyUpdate(packetWriter);
            }
        }
    }
}
