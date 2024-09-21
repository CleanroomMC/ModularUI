package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.Collection;

public interface IPanelSyncManager {

    @ApiStatus.Internal
    void initialize(String panelName, ModularSyncManager msm);

    @ApiStatus.Internal
    void detectAndSendChanges(boolean init);

    void onClose();

    void onOpen();

    String getPanelName();

    IPanelSyncManager syncValue(String key, int id, SyncHandler syncHandler);

    void receiveWidgetUpdate(String mapKey, int id, PacketBuffer buf) throws IOException;

    SlotGroup getSlotGroup(String name);

    Collection<SlotGroup> getSlotGroups();

    SyncHandler getSyncHandler(String mapKey);

    ModularSyncManager getModularSyncManager();

    default EntityPlayer getPlayer() {
        return getModularSyncManager().getPlayer();
    }

    default boolean isClient() {
        return getModularSyncManager().isClient();
    }

}
