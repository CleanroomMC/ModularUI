package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.CrossAxisAlignment;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IKey;
import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.IntSyncHandler;
import com.cleanroommc.modularui.sync.StringSyncHandler;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;

public class TestTile extends TileEntity implements IGuiHolder, ITickable {

    private long time = 0;
    private int val;
    private final FluidTank fluidTank = new FluidTank(10000);
    private String value = "";

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player) {
        guiSyncHandler.syncValue(0, new IntSyncHandler(() -> val, val -> this.val = val));
        guiSyncHandler.syncValue(1, new StringSyncHandler(() -> this.value, val -> this.value = val));
        guiSyncHandler.syncValue("fluid_slot", new FluidSlotSyncHandler(fluidTank));
    }

    @Override
    public ModularScreen createClientGui(EntityPlayer player) {
        return ModularScreen.simple("test_gui", this::createPanel);
    }

    public ModularPanel createPanel(GuiContext context) {
        ModularPanel panel = new ModularPanel(context);
        panel.flex()                        // returns object which is responsible for sizing
                .size(176, 220)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel.background(GuiTextures.BACKGROUND);
        panel.bindPlayerInventory()
                .child(new Column()
                        .coverChildren()
                        //.flex(flex -> flex.height(0.5f))
                        .padding(7)
                        .crossAxisAlignment(CrossAxisAlignment.CENTER)
                        .child(new ButtonWidget<>()
                                .size(60, 18)
                                .background(GuiTextures.BUTTON, IKey.dynamic(() -> "Button " + this.val)))
                        .child(new FluidSlot()
                                .margin(2)
                                .setSynced("fluid_slot"))
                        .child(new ButtonWidget<>()
                                .size(60, 18)
                                .tooltip(tooltip -> {
                                    tooltip.showUpTimer(10);
                                    tooltip.addLine(IKey.str("Test Line g"));
                                    tooltip.addLine(IKey.str("An image inside of a tooltip:"));
                                    tooltip.addLine(GuiTextures.LOGO.asIcon().size(50).alignment(Alignment.TopCenter));
                                    tooltip.addLine(IKey.str("And here a circle:"));
                                    tooltip.addLine(new Circle()
                                                    .setColor(Color.RED.dark(2), Color.RED.bright(2))
                                                    .asIcon()
                                                    .size(20))
                                            .addLine(new ItemDrawable(new ItemStack(Items.DIAMOND)).asIcon());
                                })
                                //.flex(flex -> flex.left(3)) // ?
                                .background(GuiTextures.BUTTON, IKey.str("Button 2")))
                        .child(new TextFieldWidget()
                                .setTextColor(Color.WHITE.normal)
                                .background(GuiTextures.DISPLAY)
                                .size(60, 20)
                                .setSynced(1)));
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
