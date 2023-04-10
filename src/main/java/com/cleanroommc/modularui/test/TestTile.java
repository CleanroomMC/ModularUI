package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.CrossAxisAlignment;
import com.cleanroommc.modularui.drawable.*;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class TestTile extends TileEntity implements IGuiHolder, ITickable {

    private final FluidTank fluidTank = new FluidTank(10000);
    private final FluidTank fluidTankPhantom = new FluidTank(10000);
    private long time = 0;
    private int val, val2 = 0;
    private String value = "";
    private double doubleValue = 1;
    private int duration = 80, progress = 0;
    private int cycleState = 0;
    private IItemHandlerModifiable inventory = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? Integer.MAX_VALUE : 64;
        }
    };

    private final ItemStackHandler bigInventory = new ItemStackHandler(9);

    private final ItemStackHandler mixerItems = new ItemStackHandler(4);
    private final FluidTank mixerFluids1 = new FluidTank(16000);
    private final FluidTank mixerFluids2 = new FluidTank(16000);

    private boolean bool = false, bool2 = true;
    private int num = 2;

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player) {
        guiSyncHandler.syncValue(0, SyncHandlers.intNumber(() -> val, val -> this.val = val));
        guiSyncHandler.syncValue(1, SyncHandlers.string(() -> this.value, val -> this.value = val));
        guiSyncHandler.syncValue(2, SyncHandlers.doubleNumber(() -> this.doubleValue, val -> this.doubleValue = val));
        guiSyncHandler.syncValue(3, SyncHandlers.intNumber(() -> this.cycleState, val -> this.cycleState = val));
        guiSyncHandler.syncValue("phantom_item_slot", SyncHandlers.phantomItemSlot(this.inventory, 0).ignoreMaxStackSize(true));
        guiSyncHandler.syncValue("fluid_slot", SyncHandlers.fluidSlot(fluidTank));
        guiSyncHandler.syncValue("fluid_slot", 1, SyncHandlers.fluidSlot(fluidTankPhantom).phantom(true));

        for (int i = 0; i < bigInventory.getSlots(); i++) {
            guiSyncHandler.syncValue("item_inv", i, SyncHandlers.itemSlot(bigInventory, i).slotGroup("item_inv"));
        }

        guiSyncHandler.registerSlotGroup("item_inv", 3);

        // mixer
        guiSyncHandler.registerSlotGroup("mixer_items", 2);
        for (int i = 0; i < 4; i++) {
            guiSyncHandler.syncValue("mixer_items", i, SyncHandlers.itemSlot(mixerItems, i).slotGroup("mixer_items"));
        }
        guiSyncHandler.syncValue("mixer_fluids", 0, SyncHandlers.fluidSlot(mixerFluids1));
        guiSyncHandler.syncValue("mixer_fluids", 1, SyncHandlers.fluidSlot(mixerFluids2));

    }

    @Override
    public ModularScreen createClientGui(EntityPlayer player) {
        return ModularScreen.simple("test_gui", this::createPanel);
    }

    public ModularPanel createPanel(GuiContext context) {
        context.enableJei();
        Rectangle colorPickerBackground = new Rectangle().setColor(Color.RED.normal);
        ModularPanel panel = new ModularPanel(context);
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        panel.flex()                        // returns object which is responsible for sizing
                .size(176, 220)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel.bindPlayerInventory()
                .child(new Row()
                        .coverChildren()
                        .top(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .tab(GuiTextures.TAB_TOP, -1))
                        .child(new PageButton(1, tabController)
                                .tab(GuiTextures.TAB_TOP, 0))
                        .child(new PageButton(2, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)))
                .child(new PagedWidget<>()
                        .size(1f)
                        .controller(tabController)
                        .addPage(new ParentWidget<>()
                                .size(1f, 1f)
                                .child(SlotGroupWidget.playerInventory())
                                .child(new Row()
                                        .height(137)
                                        .padding(7)
                                        .child(new Column()
                                                .coverChildren()
                                                //.flex(flex -> flex.height(0.5f))
                                                .width(0.5f)
                                                .crossAxisAlignment(CrossAxisAlignment.CENTER)
                                                .child(new ButtonWidget<>()
                                                        .size(60, 18)
                                                        .overlay(IKey.dynamic(() -> "Button " + this.val)))
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
                                                                    .addLine(new ItemDrawable(new ItemStack(Items.DIAMOND)).asIcon())
                                                                    .pos(Tooltip.Pos.LEFT);
                                                        })
                                                        .onMousePressed(mouseButton -> {
                                                            panel.getScreen().openDialog(this::buildDialog, ModularUI.LOGGER::info);
                                                            //openSecondWindow(context).openIn(panel.getScreen());
                                                            return true;
                                                        })
                                                        //.flex(flex -> flex.left(3)) // ?
                                                        .overlay(IKey.str("Button 2")))
                                                .child(new TextFieldWidget()
                                                        .size(60, 20)
                                                        .setSynced(1)
                                                        .margin(0, 3))
                                                .child(new TextFieldWidget()
                                                        .size(60, 20)
                                                        .setSynced(2)
                                                        .setNumbersDouble(Function.identity()))
                                                .child(IKey.str("Test string").asWidget().padding(2)))
                                        .child(new Column()
                                                .coverChildren()
                                                .width(0.5f)
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
                                                .child(new ItemSlot()
                                                        .setSynced("phantom_item_slot"))
                                                .child(new FluidSlot()
                                                        .margin(2)
                                                        .setSynced("fluid_slot", 1))
                                        )))
                        .addPage(new Column()
                                        //.coverChildren()
                                        .padding(7)
                                        .child(SlotGroupWidget.playerInventory())
                                        .child(SlotGroupWidget.builder()
                                                .matrix("III", "III", "III")
                                                .key('I', index -> new ItemSlot())
                                                .synced("item_inv")
                                                .build()
                                                .marginBottom(2))
                                        .child(SlotGroupWidget.builder()
                                                .row("FII")
                                                .row("FII")
                                                .key('F', index -> new FluidSlot().setSynced("mixer_fluids", index))
                                                .key('I', index -> new ItemSlot().setSynced("mixer_items", index))
                                                .build())
                                        .child(new Row()
                                                .coverChildrenHeight()
                                                .child(new CycleButtonWidget()
                                                        .size(14, 14)
                                                        .length(3)
                                                        .texture(GuiTextures.CYCLE_BUTTON_DEMO)
                                                        .getter(() -> val2)
                                                        .setter(val -> this.val2 = val)
                                                        .margin(8, 0))
                                                .child(IKey.str("Hello World").asWidget().height(18)))
                                        .child(new SpecialButton(IKey.str("A very long string that looks cool when animated").withAnimation())
                                                .height(14)
                                                .width(1f))
                                /*GuiTextures.LOGO.asIcon()
                                .size(80, 80)
                                .asWidget()
                                .flex(flex -> flex.width(1f).height(1f))*/)
                        .addPage(new ParentWidget<>()
                                .size(1f, 1f)
                                .padding(7)
                                .child(SlotGroupWidget.playerInventory())
                                .child(new SliderWidget()
                                        .width(1f).height(16)
                                        .top(7)
                                        .stopper(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
                                        .background(GuiTextures.SLOT_DARK))
                                .child(new ButtonWidget<>()
                                        .top(25)
                                        .background(colorPickerBackground)
                                        .onMousePressed(mouseButton -> {
                                            panel.getScreen().openPanel(new ColorPickerDialog(context, colorPickerBackground::setColor, colorPickerBackground.getColor(), true));
                                            return true;
                                        }))
                                .child(new ListWidget<>()
                                        .width(1f).top(50).bottom(2)
                                        /*.child(new Rectangle().setColor(0xFF606060).asWidget()
                                                .top(1)
                                                .left(32)
                                                .size(1, 40))*/
                                        .child(new Row()
                                                .width(1f).height(14)
                                                .child(new CycleButtonWidget()
                                                        .toggle(() -> bool, val -> bool = val)
                                                        .texture(GuiTextures.CHECK_BOX)
                                                        .size(14, 14)
                                                        .margin(8, 4))
                                                .child(IKey.lang("bogosort.gui.enable_refill").asWidget()
                                                        .height(14)
                                                        .marginLeft(10)))
                                        .child(new Row()
                                                .width(1f).height(14)
                                                .child(new TextFieldWidget()
                                                        .getterLong(() -> num)
                                                        .setterLong(val -> num = (int) val)
                                                        .setNumbers(1, Short.MAX_VALUE)
                                                        .setTextAlignment(Alignment.Center)
                                                        .background(new Rectangle().setColor(0xFFb1b1b1))
                                                        .setTextColor(IKey.TEXT_COLOR)
                                                        .size(30, 14))
                                                .child(IKey.lang("bogosort.gui.refill_threshold").asWidget()
                                                        .height(14)))
                                        .child(IKey.lang("bogosort.gui.hotbar_scrolling").asWidget()
                                                .color(0xFF404040)
                                                .alignment(Alignment.CenterLeft)
                                                .left(5).height(14)
                                                .tooltip(tooltip -> tooltip.showUpTimer(10)
                                                        .addLine(IKey.lang("bogosort.gui.hotbar_scrolling.tooltip"))))
                                        .child(new Row()
                                                .width(1f).height(14)
                                                .child(new CycleButtonWidget()
                                                        .toggle(() -> bool2, val -> bool2 = val)
                                                        .texture(GuiTextures.CHECK_BOX)
                                                        .size(14, 14))
                                                .child(IKey.lang("bogosort.gui.enabled").asWidget()
                                                        .height(14))))));
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

    public ModularPanel openSecondWindow(GuiContext context) {
        ModularPanel panel = new ModularPanel(context) {
            @Override
            public boolean disablePanelsBelow() {
                return true;
            }

            @Override
            public boolean closeOnOutOfBoundsClick() {
                return true;
            }
        }.flex(flex -> flex.size(100, 100).align(Alignment.Center))
                .background(GuiTextures.BACKGROUND);
        panel.child(new ButtonWidget<>()
                        .flex(flex -> flex.size(8, 8).top(5).right(5))
                        .overlay(IKey.str("x"))
                        .onMousePressed(mouseButton -> {
                            panel.animateClose();
                            return true;
                        }))
                .child(IKey.str("2nd Panel")
                        .asWidget()
                        .flex(flex -> flex.align(Alignment.Center)));
        return panel;
    }

    public void buildDialog(Dialog<String> dialog) {
        AtomicReference<String> value = new AtomicReference<>("");
        dialog.name("dialog");
        dialog.setDraggable(true);
        dialog.child(new TextFieldWidget()
                        .flex(flex -> flex.size(100, 20).align(Alignment.Center))
                        .getter(value::get)
                        .setter(value::set))
                .child(new ButtonWidget<>()
                        .flex(flex -> flex.size(8, 8).top(5).right(5))
                        .overlay(IKey.str("x"))
                        .onMousePressed(mouseButton -> {
                            dialog.closeWith(value.get());
                            return true;
                        }));
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

    private static class SpecialButton extends ButtonWidget<SpecialButton> {

        private final AnimatedText animatedKey;

        private SpecialButton(AnimatedText animatedKey) {
            this.animatedKey = animatedKey.stopAnimation().forward(true);
            this.animatedKey.reset();
        }

        @Override
        public void draw(GuiContext context) {
            animatedKey.draw(context, 0, 0, getArea().w(), getArea().h());
        }

        @Override
        public void onMouseStartHover() {
            this.animatedKey.startAnimation().forward(true);
        }

        @Override
        public void onMouseEndHover() {
            this.animatedKey.forward(false);
        }
    }
}
