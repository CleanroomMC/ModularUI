package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.AnimatedText;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class TestTile extends TileEntity implements IGuiHolder<PosGuiData>, ITickable {

    private final FluidTank fluidTank = new FluidTank(10000);
    private final FluidTank fluidTankPhantom = new FluidTank(Integer.MAX_VALUE);
    private long time = 0;
    private int val, val2 = 0;
    private String value = "";
    private double doubleValue = 1;
    private final int duration = 80;
    private int progress = 0;
    private int cycleState = 0;
    private final IItemHandlerModifiable inventory = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? Integer.MAX_VALUE : 64;
        }
    };

    private final ItemStackHandler bigInventory = new ItemStackHandler(9);

    private final ItemStackHandler mixerItems = new ItemStackHandler(4);
    private final ItemStackHandler smallInv = new ItemStackHandler(4);
    private final FluidTank mixerFluids1 = new FluidTank(16000);
    private final FluidTank mixerFluids2 = new FluidTank(16000);

    private int num = 2;

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 3);
        guiSyncManager.registerSlotGroup("mixer_items", 2);

        guiSyncManager.syncValue("mixer_fluids", 0, SyncHandlers.fluidSlot(this.mixerFluids1));
        guiSyncManager.syncValue("mixer_fluids", 1, SyncHandlers.fluidSlot(this.mixerFluids2));
        IntSyncValue cycleStateValue = new IntSyncValue(() -> this.cycleState, val -> this.cycleState = val);
        guiSyncManager.syncValue("cycle_state", cycleStateValue);
        guiSyncManager.bindPlayerInventory(guiData.getPlayer());

        Rectangle colorPickerBackground = new Rectangle().setColor(Color.RED.main);
        ModularPanel panel = new ModularPanel("test_tile");
        IPanelHandler panelSyncHandler = guiSyncManager.panel("other_panel", this::openSecondWindow, true);
        IPanelHandler colorPicker = IPanelHandler.simple(panel, (mainPanel, player) -> new ColorPickerDialog(colorPickerBackground::setColor, colorPickerBackground.getColor(), true)
                .setDraggable(true)
                .relative(panel)
                .top(0)
                .rightRel(1f), true);
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        panel.flex()                        // returns object which is responsible for sizing
                .size(176, 220)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel
                .child(new Row()
                        .debugName("Tab row")
                        .coverChildren()
                        .topRel(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .tab(GuiTextures.TAB_TOP, -1))
                        .child(new PageButton(1, tabController)
                                .tab(GuiTextures.TAB_TOP, 0))
                        .child(new PageButton(2, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)))
                .child(new PagedWidget<>()
                        .debugName("root parent")
                        .sizeRel(1f)
                        .controller(tabController)
                        .addPage(new ParentWidget<>()
                                .debugName("page 1 parent")
                                .sizeRel(1f, 1f)
                                .padding(7)
                                .child(new Row()
                                        .debugName("buttons, slots and more tests")
                                        .height(137)
                                        .coverChildrenWidth()
                                        //.padding(7)
                                        .child(new Column()
                                                .debugName("buttons and slots test")
                                                .coverChildren()
                                                .marginRight(8)
                                                //.flex(flex -> flex.height(0.5f))
                                                //.widthRel(0.5f)
                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                .child(new ButtonWidget<>()
                                                        .size(60, 18)
                                                        .overlay(IKey.dynamic(() -> "Button " + this.val)))
                                                .child(new FluidSlot()
                                                        .margin(2)
                                                        .syncHandler(SyncHandlers.fluidSlot(this.fluidTank)))
                                                .child(new ButtonWidget<>()
                                                        .size(60, 18)
                                                        .tooltip(tooltip -> {
                                                            tooltip.showUpTimer(10);
                                                            tooltip.addLine(IKey.str("Test Line g"));
                                                            tooltip.addLine(IKey.str("An image inside of a tooltip:"));
                                                            tooltip.addLine(GuiTextures.MUI_LOGO.asIcon().size(50).alignment(Alignment.TopCenter));
                                                            tooltip.addLine(IKey.str("And here a circle:"));
                                                            tooltip.addLine(new Circle()
                                                                            .setColor(Color.RED.darker(2), Color.RED.brighter(2))
                                                                            .asIcon()
                                                                            .size(20))
                                                                    .addLine(new ItemDrawable(new ItemStack(Items.DIAMOND)).asIcon())
                                                                    .pos(RichTooltip.Pos.LEFT);
                                                        })
                                                        .onMousePressed(mouseButton -> {
                                                            //panel.getScreen().close(true);
                                                            //panel.getScreen().openDialog("dialog", this::buildDialog, ModularUI.LOGGER::info);
                                                            //openSecondWindow(context).openIn(panel.getScreen());
                                                            panelSyncHandler.openPanel();
                                                            return true;
                                                        })
                                                        //.flex(flex -> flex.left(3)) // ?
                                                        .overlay(IKey.str("Button 2")))
                                                .child(new TextFieldWidget()
                                                        .size(60, 20)
                                                        .value(SyncHandlers.string(() -> this.value, val -> this.value = val))
                                                        .margin(0, 3))
                                                .child(new TextFieldWidget()
                                                        .size(60, 20)
                                                        .value(SyncHandlers.doubleNumber(() -> this.doubleValue, val -> this.doubleValue = val))
                                                        .setNumbersDouble(Function.identity()))
                                                .child(IKey.str("Test string").asWidget().padding(2).debugName("test string")))
                                        .child(new Column()
                                                .debugName("button and slots test 2")
                                                .coverChildren()
                                                //.widthRel(0.5f)
                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                .child(new ProgressWidget()
                                                        .progress(() -> this.progress / (double) this.duration)
                                                        .texture(GuiTextures.PROGRESS_ARROW, 20))
                                                .child(new ProgressWidget()
                                                        .progress(() -> this.progress / (double) this.duration)
                                                        .texture(GuiTextures.PROGRESS_CYCLE, 20)
                                                        .direction(ProgressWidget.Direction.CIRCULAR_CW))
                                                .child(new Row().widthRel(1f).height(18)
                                                        .child(new ToggleButton()
                                                                .value(new BoolValue.Dynamic(() -> cycleStateValue.getIntValue() == 0, val -> cycleStateValue.setIntValue(0)))
                                                                .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 0, 1, 1 / 3f)))
                                                        .child(new ToggleButton()
                                                                .value(new BoolValue.Dynamic(() -> cycleStateValue.getIntValue() == 1, val -> cycleStateValue.setIntValue(1)))
                                                                .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 1 / 3f, 1, 2 / 3f)))
                                                        .child(new ToggleButton()
                                                                .value(new BoolValue.Dynamic(() -> cycleStateValue.getIntValue() == 2, val -> cycleStateValue.setIntValue(2)))
                                                                .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 2 / 3f, 1, 1))))
                                                /*.child(new CycleButtonWidget()
                                                        .length(3)
                                                        .texture(GuiTextures.CYCLE_BUTTON_DEMO)
                                                        .addTooltip(0, "State 1")
                                                        .addTooltip(1, "State 2")
                                                        .addTooltip(2, "State 3")
                                                        .background(GuiTextures.BUTTON)
                                                        .value(SyncHandlers.intNumber(() -> this.cycleState, val -> this.cycleState = val)))*/
                                                .child(new ItemSlot()
                                                        .slot(SyncHandlers.itemSlot(this.inventory, 0).ignoreMaxStackSize(true).singletonSlotGroup()))
                                                .child(new FluidSlot()
                                                        .margin(2)
                                                        .width(30)
                                                        .syncHandler(SyncHandlers.fluidSlot(this.fluidTankPhantom).phantom(true)))
                                        )))
                        .addPage(new Column()
                                        .debugName("Slots test page")
                                        .coverChildren()
                                        //.height(120)
                                        .padding(7)
                                        .alignX(0.5f)
                                        .mainAxisAlignment(Alignment.MainAxis.START)
                                        .childPadding(2)
                                        //.child(SlotGroupWidget.playerInventory().left(0))
                                        .child(SlotGroupWidget.builder()
                                                .matrix("III", "III", "III")
                                                .key('I', index -> {
                                                    // 4 is the middle slot with a negative priority -> shift click prioritises middle slot
                                                    if (index == 4) {
                                                        return new ItemSlot().slot(SyncHandlers.itemSlot(this.bigInventory, index).singletonSlotGroup(-100));
                                                    }
                                                    return new ItemSlot().slot(SyncHandlers.itemSlot(this.bigInventory, index).slotGroup("item_inv"));
                                                })
                                                .build()
                                                //.marginBottom(2)
                                                .child(new SortButtons()
                                                        .slotGroup("item_inv")
                                                        .right(0).top(-11)))
                                        .child(SlotGroupWidget.builder()
                                                .row("FII")
                                                .row("FII")
                                                .key('F', index -> new FluidSlot().syncHandler("mixer_fluids", index))
                                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(this.mixerItems, index).slotGroup("mixer_items")))
                                                .build())
                                        .child(new Row()
                                                .coverChildrenHeight()
                                                .child(new CycleButtonWidget()
                                                        .size(14, 14)
                                                        .stateCount(3)
                                                        .stateOverlay(GuiTextures.CYCLE_BUTTON_DEMO)
                                                        .value(new IntSyncValue(() -> this.val2, val -> this.val2 = val))
                                                        .margin(8, 0))
                                                .child(IKey.str("Hello World").asWidget().height(18)))
                                        .child(new SpecialButton(IKey.str("A very long string that looks cool when animated").withAnimation())
                                                .height(14)
                                                .widthRel(1f))
                                /*GuiTextures.LOGO.asIcon()
                                .size(80, 80)
                                .asWidget()
                                .flex(flex -> flex.width(1f).height(1f))*/)
                        .addPage(new ParentWidget<>()
                                .debugName("page 3 parent")
                                .sizeRel(1f, 1f)
                                .padding(7)
                                //.child(SlotGroupWidget.playerInventory())
                                .child(new SliderWidget()
                                        .widthRel(1f).bottom(50).height(16) // test overwriting of units
                                        .top(7)
                                        .stopper(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
                                        .background(GuiTextures.SLOT_FLUID))
                                .child(new ButtonWidget<>()
                                        .debugName("color picker button")
                                        .top(25)
                                        .background(colorPickerBackground)
                                        .disableHoverBackground()
                                        .onMousePressed(mouseButton -> {
                                            colorPicker.openPanel();
                                            return true;
                                        }))
                                .child(new ListWidget<>()
                                        .widthRel(1f).top(50).bottom(2)
                                        /*.child(new Rectangle().setColor(0xFF606060).asWidget()
                                                .top(1)
                                                .left(32)
                                                .size(1, 40))*/
                                        .child(new Row()
                                                .debugName("bogo test config 1")
                                                .widthRel(1f).coverChildrenHeight()
                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                .childPadding(2)
                                                .child(new CycleButtonWidget()
                                                        .value(new BoolValue(false))
                                                        .stateOverlay(GuiTextures.CHECK_BOX)
                                                        .size(14, 14)
                                                        .margin(8, 4))
                                                .child(IKey.lang("bogosort.gui.enable_refill").asWidget()
                                                        .height(14)
                                                        .marginLeft(10)))
                                        .child(new Row()
                                                .debugName("bogo test config 2")
                                                .widthRel(1f).height(14)
                                                .childPadding(2)
                                                .child(new TextFieldWidget()
                                                        .value(new IntValue.Dynamic(() -> this.num, val -> this.num = val))
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
                                                .debugName("bogo test config 3")
                                                .widthRel(1f).height(14)
                                                .childPadding(2)
                                                .child(new CycleButtonWidget()
                                                        .value(new BoolValue(false))
                                                        .stateOverlay(GuiTextures.CHECK_BOX)
                                                        .size(14, 14))
                                                .child(IKey.lang("bogosort.gui.enabled").asWidget()
                                                        .height(14))))))
                .bindPlayerInventory();
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

    public ModularPanel openSecondWindow(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        ModularPanel panel = new Dialog<>("second_window", null)
                .setDisablePanelsBelow(false)
                .setCloseOnOutOfBoundsClick(false)
                .size(100, 100);
        SlotGroup slotGroup = new SlotGroup("small_inv", 2);
        syncManager.registerSlotGroup(slotGroup);
        AtomicInteger number = new AtomicInteger(0);
        syncManager.syncValue("int_value", new IntSyncValue(number::get, number::set));
        IPanelHandler panelSyncHandler = syncManager.panel("other_panel_2", (syncManager1, syncHandler1) ->
                openThirdWindow(syncManager1, syncHandler1, number), true);
        panel.child(ButtonWidget.panelCloseButton())
                .child(new ButtonWidget<>()
                        .size(10).top(14).right(4)
                        .overlay(IKey.str("3"))
                        .onMousePressed(mouseButton -> {
                            panelSyncHandler.openPanel();
                            return true;
                        }))
                .child(IKey.str("2nd Panel")
                        .asWidget()
                        .pos(5, 5))
                .child(SlotGroupWidget.builder()
                        .row("II")
                        .row("II")
                        .key('I', i -> new ItemSlot().slot(new ModularSlot(smallInv, i).slotGroup(slotGroup)))
                        .build()
                        .center())
                .child(new ButtonWidget<>()
                        .bottom(5)
                        .right(5)
                        .tooltip(richTooltip -> richTooltip.textColor(Color.RED.main).add("WARNING! Very Dangerous"))
                        .onMousePressed(mouseButton -> {
                            if (!panelSyncHandler.isPanelOpen()) {
                                panelSyncHandler.deleteCachedPanel();
                                number.incrementAndGet();
                            }
                            return true;
                        }));
        return panel;
    }

    public ModularPanel openThirdWindow(PanelSyncManager syncManager, IPanelHandler syncHandler, AtomicInteger integer) {
        ModularPanel panel = new Dialog<>("third_window", null)
                .setDisablePanelsBelow(false)
                .setCloseOnOutOfBoundsClick(false)
                .setDraggable(true)
                .size(50, 50);
        panel.child(ButtonWidget.panelCloseButton())
                .child(IKey.str("3rd Panel: " + integer.get())
                        .asWidget()
                        .pos(5, 17));
        return panel;
    }

    public void buildDialog(Dialog<String> dialog) {
        AtomicReference<String> value = new AtomicReference<>("");
        dialog.setDraggable(true);
        dialog.child(new TextFieldWidget()
                        .flex(flex -> flex.size(100, 20).align(Alignment.Center))
                        .value(new StringValue.Dynamic(value::get, value::set)))
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
        if (this.world.isRemote) {
            if (this.time++ % 20 == 0) {
                this.val++;
            }
        } else {
            if (this.time++ % 20 == 0 && ++this.val2 == 3) {
                this.val2 = 0;
            }
        }
        if (++this.progress == this.duration) {
            this.progress = 0;
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("item_inv", this.bigInventory.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.bigInventory.deserializeNBT(compound.getCompoundTag("item_inv"));
    }

    private static class SpecialButton extends ButtonWidget<SpecialButton> {

        private final AnimatedText animatedKey;

        private SpecialButton(AnimatedText animatedKey) {
            this.animatedKey = animatedKey.stopAnimation().forward(true);
            this.animatedKey.reset();
        }

        @Override
        public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
            this.animatedKey.draw(context, 0, 0, getArea().w(), getArea().h(), widgetTheme);
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
