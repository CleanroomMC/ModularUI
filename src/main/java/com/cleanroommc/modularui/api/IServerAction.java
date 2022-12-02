package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.sync.GuiSyncHandler;
import net.minecraft.network.PacketBuffer;

public interface IServerAction {

    void apply(GuiSyncHandler uiManager, PacketBuffer buffer);

    void writeData(PacketBuffer buffer);
}
