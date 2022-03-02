package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.ITileWithModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.drawable.UITexture;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ITickable;

public class TestTile extends SyncedTileEntityBase implements ITileWithModularUI, ITickable {

    private int serverValue = 0;
    private int time = 0;

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return ModularWindow.builder(new Size(176, 166))
                .addFromJson("modularui:test", buildContext)
                .widget(new CycleButtonWidget()
                        .setLength(3)
                        .setGetter(() -> serverValue)
                        .setSetter(val -> this.serverValue = val)
                        .setTexture(UITexture.fullImage("modularui", "gui/widgets/cycle_button_demo"))
                        .setPos(new Pos2d(20, 20)))
                .build();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeVarInt(serverValue);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        serverValue = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Val", serverValue);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.serverValue = nbt.getInteger("Val");
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (++time == 20) {
                time = 0;
                if (++serverValue == 3) {
                    serverValue = 0;
                }
            }
        }
    }
}
