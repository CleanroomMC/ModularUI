package com.cleanroommc.modularui.terminal.app;

import com.cleanroommc.modularui.sync.GuiSyncHandler;

public abstract class ServerTabletApp {

    private final String name;

    protected ServerTabletApp(String name) {
        this.name = name;
    }

    public abstract void buildSyncHandlers(GuiSyncHandler syncHandler);
}
