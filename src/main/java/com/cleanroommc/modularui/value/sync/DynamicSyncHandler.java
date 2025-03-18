package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.api.widget.IWidget;

import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

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

    public void notifyUpdate(IPacketWriter packetWriter) {
        updateWidgets(packetWriter.toPacket());
        sync(0, packetWriter);
    }

    public DynamicSyncHandler widgetProvider(IWidgetCreator widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }

    @ApiStatus.Internal
    public DynamicSyncHandler onWidgetUpdate(Consumer<IWidget> onWidgetUpdate) {
        this.onWidgetUpdate = onWidgetUpdate;
        return this;
    }

    public DynamicSyncHandler notifier(Notifier notifier) {
        notifier.setSyncHandler(this);
        return this;
    }

    public interface IWidgetCreator {

        @Nullable IWidget createWidget(PanelSyncManager syncManager, PacketBuffer buf);
    }

    public static class Notifier {

        private DynamicSyncHandler syncHandler;

        private void setSyncHandler(DynamicSyncHandler syncHandler) {
            this.syncHandler = syncHandler;
        }

        public void notifyUpdate(IPacketWriter packetWriter) {
            if (syncHandler != null && syncHandler.isValid()) {
                syncHandler.notifyUpdate(packetWriter);
            }
        }
    }
}
