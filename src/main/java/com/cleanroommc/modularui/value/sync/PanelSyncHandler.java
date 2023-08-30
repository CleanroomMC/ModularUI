package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.WidgetTree;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.Objects;

public abstract class PanelSyncHandler extends SyncHandler {

    private final ModularPanel mainPanel;
    private ModularPanel openedPanel;

    protected PanelSyncHandler(ModularPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public abstract ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager);

    public void openPanel() {
        openPanel(true);
    }

    private void openPanel(boolean syncToServer) {
        boolean client = getSyncManager().isClient();
        if (syncToServer && client) {
            syncToServer(0);
            return;
        }
        ModularPanel panel = Objects.requireNonNull(createUI(this.mainPanel, getSyncManager()));
        if (panel == this.mainPanel) {
            throw new IllegalArgumentException("New panel must not be the main panel!");
        }
        WidgetTree.collectSyncValues(getSyncManager(), panel);
        if (client && !this.mainPanel.getScreen().isPanelOpen(panel.getName())) {
            this.mainPanel.getScreen().openPanel(panel);
            this.openedPanel = panel;
        }
    }

    public void closePanel() {
        if (getSyncManager().isClient()) {
            if (this.openedPanel != null) this.openedPanel.closeIfOpen();
        } else {
            syncToClient(2);
        }
        this.openedPanel = null;
    }

    public boolean isPanelOpen() {
        return this.openedPanel != null && (getSyncManager().isClient() || this.openedPanel.isOpen());
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
