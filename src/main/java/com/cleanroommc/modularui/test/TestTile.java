package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.ITileWithModularUI;
import com.cleanroommc.modularui.api.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;

public class TestTile extends SyncedTileEntityBase implements ITileWithModularUI, ITickable {

    private int serverValue = 0;
    private int time = 0;
    private FluidTank fluidTank = new FluidTank(10000);
    private String textFieldValue = "";
    private int duration = 60;
    private int progress = 0;
    private static final AdaptableUITexture DISPLAY = AdaptableUITexture.of("modularui:gui/background/display", 143, 75, 2);
    private static final UITexture PROGRESS_BAR = UITexture.fullImage("modularui", "gui/widgets/progress_bar_arrow");

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        Text[] TEXT = {new Text("Blue \u00a7nUnderlined\u00a7rBlue ").color(0x3058B8), new Text("Mint").color(0x469E8F)};
        ModularWindow.Builder builder = ModularWindow.builder(new Size(176, 272))
                .addFromJson("modularui:test", buildContext);
        /*buildContext.applyToWidget("background", DrawableWidget.class, widget -> {
            widget.addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.")
                    .addTooltip("Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                    .addTooltip("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet");
        });*/
        return builder
                .widget(new CycleButtonWidget()
                        .setLength(3)
                        .setGetter(() -> serverValue)
                        .setSetter(val -> this.serverValue = val)
                        .setTexture(UITexture.fullImage("modularui", "gui/widgets/cycle_button_demo"))
                        .setPos(new Pos2d(78, 20))
                        .addTooltip("Test Tooltip")
                        .setTooltipShowUpDelay(10))
                .widget(new TextFieldWidget()
                        .setScale(1f)
                        .setGetter(() -> textFieldValue)
                        .setSetter(val -> textFieldValue = val)
                        .setTextColor(Color.rgb(220, 220, 220))
                        .setTextAlignment(Alignment.Center)
                        .setBackground(DISPLAY.withOffset(-2, -2, 4, 4))
                        .setSize(92, 20)
                        .setPos(20, 45))
                .widget(new ProgressBar()
                        .setProgress(() -> progress * 1f / duration)
                        .setTexture(PROGRESS_BAR, 20)
                        .setPos(5, 5))
                .widget(new FluidSlotWidget(fluidTank).setPos(20, 65))
                .widget(new ButtonWidget()
                        .setOnClick((clickData, widget) -> {
                            if (++serverValue == 3) {
                                serverValue = 0;
                            }
                        })
                        .setSynced(true, false)
                        .setBackground(DISPLAY, new Text("jTest Textg"))
                        .setSize(80, 20)
                        .setPos(10, 80))
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
            /*if (++time == 20) {
                time = 0;
                if (++serverValue == 3) {
                    serverValue = 0;
                }
            }*/
        } else {
            if (++progress == duration) {
                progress = 0;
            }
        }
    }
}
