package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IValueSyncHandler;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    public static final int SYNC_VALUE = 0;

    private Runnable changeListener;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_VALUE) read(buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_VALUE) read(buf);
    }

    protected void sync() {
        sync(SYNC_VALUE, this::write);
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (updateCacheFromSource(init)) sync();
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
