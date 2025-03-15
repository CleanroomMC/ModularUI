package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for UI factories. They are responsible for opening synced GUIs and syncing necessary data.
 *
 * @param <D> gui data type
 */
@ApiStatus.AvailableSince("2.4.0")
public interface UIFactory<D extends GuiData> {

    /**
     * The name of this factory. Must be constant.
     *
     * @return the factory name
     */
    @NotNull
    String getFactoryName();

    /**
     * Creates the main panel for the GUI. Is called on client and server side.
     *
     * @param guiData     gui data
     * @param syncManager sync manager
     * @param settings    ui settings
     * @return new main panel
     */
    @ApiStatus.OverrideOnly
    ModularPanel createPanel(D guiData, PanelSyncManager syncManager, UISettings settings);

    /**
     * Creates the screen for the GUI. Is only called on client side.
     *
     * @param guiData   gui data
     * @param mainPanel main panel created in {@link #createPanel(GuiData, PanelSyncManager, UISettings)}
     * @return new main panel
     */
    @SideOnly(Side.CLIENT)
    @ApiStatus.OverrideOnly
    ModularScreen createScreen(D guiData, ModularPanel mainPanel);

    /**
     * Creates the screen wrapper for the GUI. Is only called on client side.
     *
     * @param container container for the gui
     * @param screen    the screen which was created in {@link #createScreen(GuiData, ModularPanel)}
     * @return new screen wrapper
     * @throws IllegalStateException if the wrapping screen is not a {@link net.minecraft.client.gui.inventory.GuiContainer GuiContainer} or if the
     *                               container inside is not the same as the one passed to this method. This method is not the thrower, but the
     *                               caller of this method.
     */
    @SideOnly(Side.CLIENT)
    @ApiStatus.OverrideOnly
    default IMuiScreen createScreenWrapper(ModularContainer container, ModularScreen screen) {
        return new GuiContainerWrapper(container, screen);
    }

    /**
     * The default container supplier. This is called when no custom container in {@link UISettings} is set.
     *
     * @return new container instance
     */
    default ModularContainer createContainer() {
        return new ModularContainer();
    }

    /**
     * A default function to check if the current interacting player can interact with the ui. If not overridden on {@link UISettings},
     * then this is called every tick while a UI opened by this factory is open. Once this function returns false, the UI is immediately
     * closed.
     *
     * @param player  current interacting player
     * @param guiData gui data of the current ui
     * @return if the player can interact with the player.
     */
    default boolean canInteractWith(EntityPlayer player, D guiData) {
        return player == guiData.getPlayer();
    }

    /**
     * Writes the gui data to a buffer.
     *
     * @param guiData gui data
     * @param buffer  buffer
     */
    @ApiStatus.OverrideOnly
    void writeGuiData(D guiData, PacketBuffer buffer);

    /**
     * Reads and creates the gui data from the buffer.
     *
     * @param player player
     * @param buffer buffer
     * @return new gui data
     */
    @NotNull
    @ApiStatus.OverrideOnly
    D readGuiData(EntityPlayer player, PacketBuffer buffer);
}
