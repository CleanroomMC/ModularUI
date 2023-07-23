package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.value.sync.GuiSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;

public interface ISynced<W extends IWidget> {

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    void initialiseSyncHandler(GuiSyncHandler syncHandler);

    default boolean isValidSyncHandler(SyncHandler syncHandler) {
        return true;
    }

    boolean isSynced();

    @NotNull
    SyncHandler getSyncHandler();

    W syncHandler(String key);

    default W syncHandler(String name, int id) {
        return syncHandler(GuiSyncHandler.makeSyncKey(name, id));
    }

    default W syncHandler(int id) {
        return syncHandler(GuiSyncHandler.makeSyncKey(id));
    }
}
