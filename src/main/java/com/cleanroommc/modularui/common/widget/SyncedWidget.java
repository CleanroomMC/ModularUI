package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonObject;

/**
 * A optional base class for synced widgets.
 */
public abstract class SyncedWidget extends Widget implements ISyncedWidget {

    private boolean syncsToServer = true;
    private boolean syncsToClient = true;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        this.syncsToServer = JsonHelper.getBoolean(json, true, "syncToClient", "handlesServer");
        this.syncsToClient = JsonHelper.getBoolean(json, true, "syncToServer", "handlesClient");
    }

    /**
     * @return if this widget should operate on the sever side.
     * For example detecting and sending changes to client.
     */
    public boolean syncsToClient() {
        return syncsToClient;
    }

    /**
     * @return if this widget should operate on the client side.
     * For example, sending a changed value to the server.
     */
    public boolean syncsToServer() {
        return syncsToServer;
    }

    /**
     * Determines how this widget should sync values
     *
     * @param syncsToClient if this widget should sync changes to the server
     * @param syncsToServer if this widget should detect changes on server and sync them to client
     */
    public SyncedWidget setSynced(boolean syncsToClient, boolean syncsToServer) {
        this.syncsToClient = syncsToClient;
        this.syncsToServer = syncsToServer;
        return this;
    }
}
