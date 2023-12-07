package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An interface to implement on {@link net.minecraft.tileentity.TileEntity} or {@link net.minecraft.item.Item}.
 */
@FunctionalInterface
public interface IGuiHolder<T extends GuiData> {

    /**
     * Only called on client side.
     *
     * @param data      information about the creation context
     * @param mainPanel the panel created in {@link #buildUI(GuiData, GuiSyncManager, boolean)}
     * @return a modular screen instance with the given panel
     */
    @SideOnly(Side.CLIENT)
    default ModularScreen createScreen(T data, ModularPanel mainPanel) {
        return new ModularScreen(mainPanel);
    }

    /**
     * Called on server and client. Create only the main panel here. Only here you can add sync handlers to widgets directly.
     * If the widget to be synced is not in this panel yet (f.e. in another panel) the sync handler must be registered here
     * with {@link GuiSyncManager}.
     *
     * @param data        information about the creation context
     * @param syncManager sync handler where widget sync handlers should be registered
     * @param isClient    true if the world is a client world
     */
    ModularPanel buildUI(T data, GuiSyncManager syncManager, boolean isClient);
}
