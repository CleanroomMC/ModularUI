package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * If you want another panel where some widgets may be able to sync data, you will need this.
 * Register it in any {@link PanelSyncManager} (preferably the main one).
 * Then you can call {@link #openPanel()} and {@link #closePanel(boolean)} from any side.
 */
public class PanelSyncHandler extends SyncHandler {

    private ModularPanel mainPanel;
    private final IPanelBuilder panelBuilder;
    private String panelName;
    private ModularPanel openedPanel;
    private PanelSyncManager syncManager;
    private boolean open = false;

    /**
     * Creates a PanelSyncHandler
     *
     * @param mainPanel    the main panel of the current GUI
     * @param panelBuilder a panel builder function
     */
    public PanelSyncHandler(ModularPanel mainPanel, IPanelBuilder panelBuilder) {
        this.mainPanel = mainPanel;
        this.panelBuilder = panelBuilder;
    }

    public ModularPanel createUI(PanelSyncManager syncManager) {
        return this.panelBuilder.buildUI(syncManager, this);
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
        if (this.syncManager != null && this.syncManager.getModularSyncManager() != getSyncManager().getModularSyncManager()) {
            throw new IllegalStateException("Can't reopen synced panel in another screen!");
        } else if (this.syncManager == null) {
            this.syncManager = new PanelSyncManager();
            this.openedPanel = Objects.requireNonNull(createUI(this.syncManager));
            if (this.openedPanel == this.mainPanel) {
                throw new IllegalArgumentException("New panel must not be the main panel!");
            }
            this.panelName = this.openedPanel.getName();
            this.openedPanel.setSyncHandler(this);
            WidgetTree.collectSyncValues(getSyncManager(), this.openedPanel);
            if (!client) {
                this.openedPanel = null;
            }
        }
        if (client && !this.mainPanel.getScreen().isPanelOpen(this.openedPanel.getName())) {
            this.mainPanel.getScreen().openPanel(this.openedPanel);
        }
        getSyncManager().getModularSyncManager().open(this.panelName, this.syncManager);
        this.open = true;
    }

    public void closePanel() {
        if (getSyncManager().isClient()) {
            if (this.openedPanel != null) {
                this.openedPanel.closeIfOpen();
            }
        } else {
            syncToClient(2);
        }
    }

    @ApiStatus.Internal
    public void closePanelInternal() {
        getSyncManager().getModularSyncManager().close(this.panelName);
        this.open = false;
        if (getSyncManager().isClient()) {
            syncToServer(2);
        }
    }

    public boolean isPanelOpen() {
        return this.open;
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
        } else if (i == 2) {
            closePanelInternal();
        }
    }

    /**
     * A function which creates a secondary {@link ModularPanel}
     */
    public interface IPanelBuilder {

        /**
         * Creates a {@link ModularPanel}. It must NOT return null or the main panel.
         *
         * @param syncManager the sync manager for this panel
         * @param syncHandler the sync handler that sync opening and closing of this panel
         * @return the created panel
         */
        @NotNull
        ModularPanel buildUI(@NotNull PanelSyncManager syncManager, @NotNull PanelSyncHandler syncHandler);
    }
}
