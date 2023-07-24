package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An interface to implement on {@link net.minecraft.tileentity.TileEntity} or {@link net.minecraft.item.Item}.
 * More {@link com.cleanroommc.modularui.manager.GuiInfo} can be added to work with this interface.
 */
public interface IGuiHolder {

    /**
     * Only called on client side.
     *
     * @param guiCreationContext information about the creation context
     * @param mainPanel          the panel created in {@link #buildUI(GuiCreationContext, GuiSyncManager, boolean)}
     * @return a modular screen instance with the given panel
     */
    @SideOnly(Side.CLIENT)
    default ModularScreen createScreen(GuiCreationContext guiCreationContext, ModularPanel mainPanel) {
        return new ModularScreen(mainPanel);
    }

    /**
     * Called on server and client. Create only the main panel here. Only here you can add sync handlers to widgets directly.
     * If the widget to be synced is not in this panel yet (f.e. in another panel) the sync handler must be registered here
     * with {@link GuiSyncManager}.
     *
     * @param guiCreationContext information about the creation context
     * @param guiSyncManager     sync handler where widget sync handlers should be registered
     * @param isClient           true if the world is a client world
     */
    ModularPanel buildUI(GuiCreationContext guiCreationContext, GuiSyncManager guiSyncManager, boolean isClient);
}
