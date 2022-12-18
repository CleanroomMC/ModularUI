package com.cleanroommc.modularui.api;

import net.minecraft.network.PacketBuffer;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        read(buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        read(buf);
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (needsSync(init)) {
            syncToClient(0, this::updateAndWrite);
        }
    }
}
