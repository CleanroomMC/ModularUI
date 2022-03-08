package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonObject;

/**
 * A optional base class for synced widgets.
 */
public abstract class SyncedWidget extends Widget implements ISyncedWidget {

    private boolean handlesServer = true;
    private boolean handlesClient = true;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        this.handlesServer = JsonHelper.getBoolean(json, true, "syncToClient", "handlesServer");
        this.handlesClient = JsonHelper.getBoolean(json, true, "syncToServer", "handlesClient");
    }

    /**
     * @return if this widget should operate on the sever side.
     * For example detecting and sending changes to client.
     */
    public boolean handlesServer() {
        return handlesClient;
    }

    /**
     * @return if this widget should operate on the client side.
     * For example, sending a changed value to the server.
     */
    public boolean handlesClient() {
        return handlesServer;
    }

    /**
     * Determines how this widget should sync values
     *
     * @param handlesClient   if this widget should sync changes to the server
     * @param handlesServer if this widget should detect changes on server and sync them to client
     */
    public SyncedWidget setSynced(boolean handlesClient, boolean handlesServer) {
        this.handlesClient = handlesClient;
        this.handlesServer = handlesServer;
        return this;
    }
}
