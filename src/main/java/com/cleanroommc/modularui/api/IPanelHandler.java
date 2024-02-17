package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.SecondaryPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;

import org.jetbrains.annotations.ApiStatus;

/**
 * This class can handle opening and closing of a {@link ModularPanel}. It makes sure, that the same panel is not created multiple times and instead reused.
 * Using {@link #openPanel()} is the only way to open multiple panels.
 * Panels can be closed with {@link #closePanel()}, but also with {@link ModularPanel#closeIfOpen(boolean)} and {@link ModularPanel#animateClose()}.
 * They are the same. Synced panels must be registered in the main {@link com.cleanroommc.modularui.value.sync.PanelSyncManager PanelSyncManager}.
 * It is recommended to use {@link com.cleanroommc.modularui.value.sync.PanelSyncManager#panel(String, ModularPanel, PanelSyncHandler.IPanelBuilder) PanelSyncManager#panel(String, ModularPanel, PanelSyncHandler.IPanelBuilder)}.
 */
public interface IPanelHandler {

    /**
     * Creates a synced panel handler. It MUST be registered in a {@link com.cleanroommc.modularui.value.sync.PanelSyncManager PanelSyncManager} on
     * both sides to work.
     *
     * @param mainPanel    the main panel of the GUI
     * @param panelBuilder the panel builder, that will create the new panel. It must not return null or the main panel.
     * @return a synced panel handler.
     * @throws NullPointerException     if the build panel of the builder is null
     * @throws IllegalArgumentException if the build panel of the builder is the main panel
     */
    static IPanelHandler synced(ModularPanel mainPanel, PanelSyncHandler.IPanelBuilder panelBuilder) {
        return new PanelSyncHandler(mainPanel, panelBuilder);
    }

    /**
     * Creates a non synced panel handler. Trying to use synced values anyway will result in a crash.
     *
     * @param mainPanel the main panel of the GUI
     * @param provider  the panel builder, that will create the new panel. It must not return null or the main panel.
     * @return a simple panel handler.
     * @throws NullPointerException     if the build panel of the builder is null
     * @throws IllegalArgumentException if the build panel of the builder is the main panel or there are synced values in the panel
     */
    static IPanelHandler simple(ModularPanel mainPanel, SecondaryPanel.IPanelBuilder provider) {
        return new SecondaryPanel(mainPanel, provider);
    }

    /**
     * Opens the panel. If there is no cached panel, one will be created.
     * Can be called on both sides if this handler is synced.
     */
    void openPanel();

    /**
     * Initiates the closing animation if the panel is open.
     * Can be called on both sides if this handler is synced.
     */
    void closePanel();

    /**
     * Called internally after the panel is closed.
     */
    @ApiStatus.OverrideOnly
    void closePanelInternal();

    /**
     * Deletes the current cached panel. Should not be used frequently.
     * This only works on non synced panels. Otherwise, it crashes.
     *
     * @throws UnsupportedOperationException if this handler is synced
     */
    void deleteCachedPanel();
}
