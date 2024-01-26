package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class PanelSyncHandler extends SyncHandler {

    private ModularPanel mainPanel;
    private final IPanelBuilder panelBuilder;
    private String panelName;
    private ModularPanel openedPanel;
    private PanelSyncManager syncManager;
    private boolean open = false;

    public PanelSyncHandler(ModularPanel mainPanel, IPanelBuilder panelBuilder) {
        this.mainPanel = mainPanel;
        this.panelBuilder = panelBuilder;
    }

    public ModularPanel createUI(PanelSyncManager syncManager) {
        return this.panelBuilder.buildUI(syncManager);
    }

    public void openPanel() {
        openPanel(true);
    }

    private void openPanel(boolean syncToServer) {
        boolean client = getSyncManager().isClient();
        if (syncToServer && client) {
            syncToServer(0);
            return;
        }
        if (this.openedPanel == null) {
            if (this.syncManager != null && this.syncManager.getModularSyncManager() != getSyncManager().getModularSyncManager()) {
                throw new IllegalStateException("Can't reopen synced panel in another screen!");
            } else {
                this.syncManager = new PanelSyncManager();
            }
            this.openedPanel = Objects.requireNonNull(createUI(this.syncManager));
            if (this.openedPanel == this.mainPanel) {
                throw new IllegalArgumentException("New panel must not be the main panel!");
            }
            this.panelName = this.openedPanel.getName();
            this.openedPanel.setSyncHandler(this);
            WidgetTree.collectSyncValues(getSyncManager(), this.openedPanel);
            if (!client) {
                // only keep panel on client
                this.openedPanel = null;
            }
        }
        if (client && !this.mainPanel.getScreen().isPanelOpen(this.openedPanel.getName())) {
            this.mainPanel.getScreen().openPanel(this.openedPanel);
        }
        getSyncManager().getModularSyncManager().open(this.panelName, this.syncManager);
        this.open = true;
    }

    public void closePanel(boolean alreadyClosedOnClient) {
        if (getSyncManager().isClient()) {
            if (this.openedPanel != null && !alreadyClosedOnClient) this.openedPanel.closeIfOpen(true);
        } else {
            syncToClient(2);
        }
        getSyncManager().getModularSyncManager().close(this.panelName);
        this.open = false;
    }

    public boolean isPanelOpen() {
        return this.open;
    }

    @Override
    public void readOnClient(int i, PacketBuffer packetBuffer) throws IOException {
        if (i == 1) {
            openPanel(false);
        } else if (i == 2) {
            closePanel(false);
        }
    }

    @Override
    public void readOnServer(int i, PacketBuffer packetBuffer) throws IOException {
        if (i == 0) {
            openPanel(false);
            syncToClient(1);
        }
    }

    public interface IPanelBuilder {

        @NotNull
        ModularPanel buildUI(@NotNull PanelSyncManager syncManager);
    }
}
