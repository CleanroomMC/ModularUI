package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IValueSyncHandler;
import net.minecraft.network.PacketBuffer;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    private Runnable changeListener;

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        read(buf);
        onValueChanged();
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        read(buf);
        onValueChanged();
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (needsSync(init)) {
            syncToClient(0, this::write);
        }
    }

    protected void onValueChanged() {
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public Runnable getChangeListener() {
        return changeListener;
    }
}
