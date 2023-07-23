package com.cleanroommc.modularui.value.sync;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CursorSlotSyncHandler extends SyncHandler {

    public void sync() {
        sync(0, buffer -> buffer.writeItemStack(getSyncHandler().getPlayer().inventory.getItemStack()));
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
