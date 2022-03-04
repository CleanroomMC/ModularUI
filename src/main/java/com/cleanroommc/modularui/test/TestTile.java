package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.ITileWithModularUI;
import com.cleanroommc.modularui.api.TooltipContainer;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.DrawableWidget;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ITickable;

public class TestTile extends SyncedTileEntityBase implements ITileWithModularUI, ITickable {

    private int serverValue = 0;
    private int time = 0;

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        Text[] TEXT = {new Text("Blue \u00a7nUnderlined\u00a7rBlue ").color(0x3058B8), new Text("Mint").color(0x469E8F)};
        ModularWindow.Builder builder = ModularWindow.builder(new Size(176, 166))
                .addFromJson("modularui:test", buildContext);
        buildContext.applyToWidget("background", DrawableWidget.class, widget -> {
            widget.getOrCreateTooltip().setScale(0.5f)
                    .addLine("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.")
                    .addLine("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                    .addLine("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet");
        });
        return builder.widget(new CycleButtonWidget()
                        .setLength(3)
                        .setGetter(() -> serverValue)
                        .setSetter(val -> this.serverValue = val)
                        .setTexture(UITexture.fullImage("modularui", "gui/widgets/cycle_button_demo"))
                        .setPos(new Pos2d(78, 20))
                        .setTooltip(new TooltipContainer()
                                .addLine("Test Tooltip")
                                .setShowUpDelay(10)))
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
