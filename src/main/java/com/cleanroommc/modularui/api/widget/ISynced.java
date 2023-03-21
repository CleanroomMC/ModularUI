package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.MapKey;

public interface ISynced<W extends IWidget> {

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    void initialiseSyncHandler(GuiSyncHandler syncHandler);

    default boolean isValidSyncHandler(SyncHandler syncHandler) {
        return true;
    }

    W setSynced(MapKey key);

    default W setSynced(String name, int id) {
        return setSynced(new MapKey(name, id));
    }

    default W setSynced(String name) {
        return setSynced(new MapKey(name));
    }

    default W setSynced(int id) {
        return setSynced(new MapKey(id));
    }
}
