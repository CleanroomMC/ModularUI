package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonObject;

/**
 * A optional base class for synced widgets.
 */
public abstract class SyncedWidget extends Widget implements ISyncedWidget {

    private boolean sendChangesToServer = true;
    private boolean detectChangesOnServer = true;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        this.sendChangesToServer = JsonHelper.getBoolean(json, true, "syncToServer", "sendChangesToServer");
        this.detectChangesOnServer = JsonHelper.getBoolean(json, true, "syncToClient", "detectChangesOnServer");
    }

    public boolean detectChangesOnServer() {
        return detectChangesOnServer;
    }

    public boolean sendChangesToServer() {
        return sendChangesToServer;
    }

    /**
     * Determines how this widget should sync values
     *
     * @param sendChangesToServer   if this widget should sync changes to the server
     * @param detectChangesOnServer if this widget should detect changes on server and sync them to client
     */
    public SyncedWidget setSynced(boolean sendChangesToServer, boolean detectChangesOnServer) {
        this.sendChangesToServer = sendChangesToServer;
        this.detectChangesOnServer = detectChangesOnServer;
        return this;
    }
}
