package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.Objects;

public abstract class PanelSyncHandler extends SyncHandler {

    private final ModularPanel mainPanel;
    private ModularPanel syncedPanel;

    protected PanelSyncHandler(ModularPanel mainPanel, GuiSyncManager syncManager) {
        this.mainPanel = mainPanel;
        this.syncedPanel = createUI(mainPanel, syncManager);
        WidgetTree.collectSyncValues(syncManager, this.syncedPanel);
    }

    public abstract ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager);

    public void openPanel() {
        openPanel(true);
    }

    private void openPanel(boolean syncToServer) {
        boolean client = getSyncManager().isClient();
        checkPanel();
        if (client && !this.mainPanel.getScreen().isPanelOpen(this.syncedPanel.getName())) {
            this.mainPanel.getScreen().openPanel(this.syncedPanel);
            if (syncToServer) syncToServer(0);
        }
    }

    public void checkPanel() {
        ModularPanel panel = Objects.requireNonNull(createUI(this.mainPanel, getSyncManager()));
        if (panel == this.mainPanel) {
            throw new IllegalArgumentException("New panel must not be the main panel!");
        }

        if (!panel.equals(this.syncedPanel)) {
            this.syncedPanel = panel;
            WidgetTree.collectSyncValues(getSyncManager(), this.syncedPanel);
        }
    }

    public void closePanel() {
        if (getSyncManager().isClient()) {
            this.syncedPanel.closeIfOpen(true);
        } else {
            syncToClient(2);
        }
    }

    public boolean isPanelOpen() {
        return this.syncedPanel != null && (!getSyncManager().isClient() || this.syncedPanel.isOpen());
    }

    @Override
    public void readOnClient(int i, PacketBuffer packetBuffer) throws IOException {
        if (i == 1) {
            openPanel(false);
        } else if (i == 2) {
            closePanel();
        }
    }

    @Override
    public void readOnServer(int i, PacketBuffer packetBuffer) throws IOException {
        if (i == 0) {
            openPanel(false);
            syncToClient(1);
        }
    }
}
