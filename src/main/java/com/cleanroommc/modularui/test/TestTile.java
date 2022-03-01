package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.ITileWithModularUI;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import net.minecraft.network.PacketBuffer;

public class TestTile extends SyncedTileEntityBase implements ITileWithModularUI {
    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(new Size(176, 166))
                .addFromJson("test", buildContext.getPlayer())
                .build();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {

    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {

    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {

    }
}
