package com.cleanroommc.modularui.api;

import net.minecraft.network.PacketBuffer;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    private Runnable changeListener;

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        read(buf);
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        read(buf);
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (needsSync(init)) {
            syncToClient(0, this::updateAndWrite);
        }
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }
}
