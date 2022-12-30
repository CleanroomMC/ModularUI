package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.CrossAxisAlignment;
import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;

import java.util.function.Function;

public class TestTile extends TileEntity implements IGuiHolder, ITickable {

    private final FluidTank fluidTank = new FluidTank(10000);
    private long time = 0;
    private int val;
    private String value = "";
    private double doubleValue = 1;
    private int duration = 80, progress = 0;
    private int cycleState = 0;

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player) {
        guiSyncHandler.syncValue(0, SyncHandlers.intNumber(() -> val, val -> this.val = val));
        guiSyncHandler.syncValue(1, SyncHandlers.string(() -> this.value, val -> this.value = val));
        guiSyncHandler.syncValue(2, SyncHandlers.doubleNumber(() -> this.doubleValue, val -> this.doubleValue = val));
        guiSyncHandler.syncValue(3, SyncHandlers.intNumber(() -> this.cycleState, val -> this.cycleState = val));
        guiSyncHandler.syncValue("fluid_slot", SyncHandlers.fluidSlot(fluidTank));
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
                .child(new Row()
                        .flex(flex -> flex.height(137))
                        .padding(7)
                        .child(new Column()
                                .coverChildren()
                                //.flex(flex -> flex.height(0.5f))
                                .flex(flex -> flex.width(0.5f))
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
                                        .setSynced(1)
                                        .margin(0, 3))
                                .child(new TextFieldWidget()
                                        .setTextColor(Color.WHITE.normal)
                                        .background(GuiTextures.DISPLAY)
                                        .size(60, 20)
                                        .setSynced(2)
                                        .setNumbersDouble(Function.identity()))
                                .child(IKey.str("Test string").asWidget().padding(2)))
                        .child(new Column()
                                .coverChildren()
                                .flex(flex -> flex.width(0.5f))
                                .crossAxisAlignment(CrossAxisAlignment.CENTER)
                                .child(new ProgressWidget()
                                        .progress(() -> progress / (double) duration)
                                        .texture(GuiTextures.PROGRESS_ARROW, 20))
                                .child(new ProgressWidget()
                                        .progress(() -> progress / (double) duration)
                                        .texture(GuiTextures.PROGRESS_CYCLE, 20)
                                        .direction(ProgressWidget.Direction.CIRCULAR_CW))
                                .child(new CycleButtonWidget()
                                        .length(3)
                                        .texture(GuiTextures.CYCLE_BUTTON_DEMO)
                                        .addTooltip(0, "State 1")
                                        .addTooltip(1, "State 2")
                                        .addTooltip(2, "State 3")
                                        .background(GuiTextures.BUTTON)
                                        .setSynced(3))
                        ));
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
        if (++progress == duration) {
            progress = 0;
        }
    }
}
