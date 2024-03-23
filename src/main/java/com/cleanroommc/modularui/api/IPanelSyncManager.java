package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.entity.player.EntityPlayer;

import org.jetbrains.annotations.ApiStatus;

public interface IPanelSyncManager {

    @ApiStatus.Internal
    void detectAndSendChanges(boolean init);

    void onClose();

    void onOpen();

    EntityPlayer getPlayer();

    IPanelSyncManager syncValue(String key, int id, SyncHandler syncHandler);

}
