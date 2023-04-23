package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.sync.GuiSyncHandler;

public interface ISynced<W extends IWidget> {

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    void initialiseSyncHandler(GuiSyncHandler syncHandler);

    default boolean isValidSyncHandler(SyncHandler syncHandler) {
        return true;
    }

    W setSynced(String key);

    default W setSynced(String name, int id) {
        return setSynced(GuiSyncHandler.makeSyncKey(name, id));
    }

    default W setSynced(int id) {
        return setSynced(GuiSyncHandler.makeSyncKey(id));
    }
}
