package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.CrossAxisAlignment;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.IntSyncHandler;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.layout.Column;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;

public class TestTile extends TileEntity implements IGuiHolder, ITickable {

    private long time = 0;
    private int val;
    private final FluidTank fluidTank = new FluidTank(10000);

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player) {
        guiSyncHandler.syncValue(0, new IntSyncHandler(() -> val, val -> this.val = val));
        guiSyncHandler.syncValue("fluid_slot", new FluidSlotSyncHandler(fluidTank));
    }

    @Override
    public ModularScreen createClientGui(EntityPlayer player) {
        return ModularScreen.simple("test_gui", this::createPanel);
    }

    public ModularPanel createPanel(GuiContext context) {
        ModularPanel panel = new ModularPanel(context);
        panel.flex()                        // returns object which is responsible for sizing
                .size(176, 166)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel.background(GuiTextures.BACKGROUND);
        panel.bindPlayerInventory()
                .child(new Column()
                        .crossAxisAlignment(CrossAxisAlignment.CENTER)
                        .flex(flex -> flex.top(7).left(7).right(7).bottom(7))
                        .child(new ButtonWidget<>()
                                .flex(flex -> flex.size(60, 20)
                                        .top(7)
                                        .left(0.5f))
                                .background(GuiTextures.BUTTON, IKey.dynamic(() -> "Button " + this.val)))
                        .child(new FluidSlot().flex(flex -> flex
                                        .top(30)
                                        .left(0.5f))
                                .setSynced("fluid_slot")));
        /*panel.child(new ButtonWidget<>()
                        .flex(flex -> flex.size(60, 20)
                                .top(7)
                                .left(0.5f))
                        .background(GuiTextures.BUTTON, IKey.dynamic(() -> "Button " + this.val)))
                .child(SlotGroup.playerInventory())
                .child(new FluidSlot().flex(flex -> flex
                                .top(30)
                                .left(0.5f))
                        .setSynced("fluid_slot"));*/
        return panel;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            if (time++ % 20 == 0) {
                val++;
            }
        }
    }
}
