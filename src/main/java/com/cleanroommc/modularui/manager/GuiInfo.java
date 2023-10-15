package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * This class handles opening synced GUIs.
 *
 * @see GuiInfos default instances
 */
public class GuiInfo {

    /**
     * Creates a GuiInfo builder
     *
     * @return new GuiInfo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private static int nextId = 0;
    private final BiFunction<GuiCreationContext, GuiSyncManager, ModularPanel> mainPanelCreator;
    private final BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator;
    private final int id;

    /**
     * Can be called directly, but using {@link #builder()} is recommended.
     * The first parameter is a function which is invoked on client and server to collect sync handlers, but the widget
     * tree is only kept on client side.
     * The second parameter is function which creates a new {@link ModularScreen} (it says Object because it would crash
     * on server side otherwise) with the {@link ModularPanel} created in the first parameters function. It is only
     * called on client side.
     *
     * @param mainPanelCreator main panel function
     * @param clientGuiCreator modular screen function
     */
    public GuiInfo(BiFunction<GuiCreationContext, GuiSyncManager, ModularPanel> mainPanelCreator, BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator) {
        this.mainPanelCreator = Objects.requireNonNull(mainPanelCreator);
        this.clientGuiCreator = Objects.requireNonNull(clientGuiCreator);
        this.id = nextId++;
        GuiManager.INSTANCE.register(this);
    }

    public int getId() {
        return this.id;
    }

    /**
     * Opens the GUI with the players world and position.
     * This method is preferred for GUIs bound to items.
     *
     * @param player player who opens the GUI
     */
    public void open(EntityPlayer player) {
        Objects.requireNonNull(player, "Player must not be null!");
        if (NetworkUtils.isClient(player)) {
            throw new IllegalStateException("Synced GUIs must be opened on server side only!");
        }
        FMLNetworkHandler.openGui(player, ModularUI.ID, this.id, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
    }

    /**
     * Opens a GUI with a world and a block pos.
     * This method is preferred for GUIs bound to TileEntities.
     *
     * @param player player who opens the GUI
     * @param world  world of the GUI
     * @param pos    pos of the GUI
     */
    public void open(EntityPlayer player, World world, BlockPos pos) {
        Objects.requireNonNull(player, "Player must not be null!");
        Objects.requireNonNull(world, "World must not be null!");
        Objects.requireNonNull(pos, "BlockPos must not be null!");
        if (NetworkUtils.isClient(player)) {
            throw new IllegalStateException("Synced GUIs must be opened on server side only!");
        }
        if (world.isRemote) {
            throw new IllegalStateException("EntityPlayer is server side, but World isn't!");
        }
        FMLNetworkHandler.openGui(player, ModularUI.ID, this.id, world, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Opens a GUI with a world and a pos.
     * This method is preferred for GUIs bound to TileEntities.
     *
     * @param player player who opens the GUI
     * @param world  world of the GUI
     * @param x      x pos of the GUI
     * @param y      y pos of the GUI
     * @param z      z pos of the GUI
     */
    public void open(EntityPlayer player, World world, int x, int y, int z) {
        Objects.requireNonNull(player, "Player must not be null!");
        Objects.requireNonNull(world, "World must not be null!");
        if (NetworkUtils.isClient(player)) {
            throw new IllegalStateException("Synced GUIs must be opened on server side only!");
        }
        if (world.isRemote) {
            throw new IllegalStateException("EntityPlayer is server side, but World isn't!");
        }
        FMLNetworkHandler.openGui(player, ModularUI.ID, this.id, world, x, y, z);
    }

    ModularPanel createCommonGui(GuiCreationContext context, GuiSyncManager guiSyncManager) {
        ModularPanel panel = Objects.requireNonNull(this.mainPanelCreator.apply(context, guiSyncManager), "The main panel must not be null!");
        WidgetTree.collectSyncValues(guiSyncManager, panel);
        return panel;
    }

    @SideOnly(Side.CLIENT)
    ModularScreen createClientGui(GuiCreationContext context, ModularPanel panel) {
        Object screen = Objects.requireNonNull(this.clientGuiCreator.apply(context, panel), "The modular screen must not be null!");
        if (!(screen instanceof ModularScreen)) {
            throw new IllegalStateException("Client screen must be an instance of ModularScreen!");
        }
        return (ModularScreen) screen;
    }

    /**
     * Builder class for {@link GuiInfo}.
     */
    public static class Builder {

        private BiFunction<GuiCreationContext, GuiSyncManager, ModularPanel> mainPanelCreator;
        private BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator;

        /**
         * Use {@link GuiInfo#builder()}
         */
        private Builder() {
        }

        /**
         * Sets the main panel function. It is called on client and server side to collect sync handlers.
         * The widget tree is only kept on server side. The function must return a new {@link ModularPanel} instance.
         *
         * @param mainPanelCreator main panel function
         * @return this
         */
        public Builder commonGui(@NotNull BiFunction<GuiCreationContext, GuiSyncManager, ModularPanel> mainPanelCreator) {
            this.mainPanelCreator = mainPanelCreator;
            return this;
        }

        /**
         * Sets the main panel function. It is only called on client side with the {@link ModularPanel} created in the
         * main panel function. The function must return a new {@link ModularScreen} instance.
         *
         * @param clientGuiCreator main panel function
         * @return this
         */
        public Builder clientGui(@NotNull BiFunction<GuiCreationContext, ModularPanel, Object> clientGuiCreator) {
            this.clientGuiCreator = clientGuiCreator;
            return this;
        }

        /**
         * Creates a new {@link GuiInfo}.
         *
         * @return new gui info
         */
        public GuiInfo build() {
            Objects.requireNonNull(this.mainPanelCreator, "MainPanel function must be added!");
            Objects.requireNonNull(this.clientGuiCreator, "ModularScreen function must be added!");
            return new GuiInfo(this.mainPanelCreator, this.clientGuiCreator);
        }
    }
}
