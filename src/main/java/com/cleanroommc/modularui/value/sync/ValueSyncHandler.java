package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IValueSyncHandler;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    private Runnable changeListener;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        read(buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        read(buf);
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (updateCacheFromSource(init)) {
            syncToClient(0, this::write);
        }
    }

    /**
     * Called when the cached value of this sync handler updates. Implementations need to call this inside
     * {@link #setValue(Object, boolean, boolean)}.
     */
    protected void onValueChanged() {
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public Runnable getChangeListener() {
        return this.changeListener;
    }
}
