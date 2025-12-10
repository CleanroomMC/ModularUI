package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.ISyncedAction;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

public class SyncedAction {

    private final ISyncedAction action;
    private final boolean executeClient;
    private final boolean executeServer;

    public SyncedAction(ISyncedAction action, boolean executeClient, boolean executeServer) {
        this.action = action;
        this.executeClient = executeClient;
        this.executeServer = executeServer;
    }

    public boolean invoke(boolean client, @NotNull PacketBuffer packet) {
        if (isExecute(client)) {
            this.action.invoke(packet);
            return true;
        }
        return false;
    }

    public boolean isExecuteClient() {
        return executeClient;
    }

    public boolean isExecuteServer() {
        return executeServer;
    }

    public boolean isExecute(boolean client) {
        return (client && this.executeClient) || (!client && this.executeServer);
    }
}
