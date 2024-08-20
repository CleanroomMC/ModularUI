package com.cleanroommc.modularui.value.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CursorSlotSyncHandler extends SyncHandler {

    public void sync() {
        sync(0, buffer -> buffer.writeItemStack(getSyncManager().getPlayer().inventory.getItemStack()));
    }

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) throws IOException {
        getSyncManager().getPlayer().getInventory().setPickedItem(buf.readItem());
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) throws IOException {
        getSyncManager().getPlayer().getInventory().setPickedItem(buf.readItem());
    }
}
