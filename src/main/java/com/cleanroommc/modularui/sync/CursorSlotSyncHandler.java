package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.SyncHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CursorSlotSyncHandler extends SyncHandler {

    public void sync() {
        if (NetworkUtils.isClient(getSyncHandler().getPlayer())) {
            syncToServer(0, buffer -> buffer.writeItemStack(getSyncHandler().getPlayer().inventory.getItemStack()));
        } else {
            syncToClient(0, buffer -> buffer.writeItemStack(getSyncHandler().getPlayer().inventory.getItemStack()));
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        getSyncHandler().getPlayer().inventory.setItemStack(buf.readItemStack());
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        getSyncHandler().getPlayer().inventory.setItemStack(buf.readItemStack());
    }
}
