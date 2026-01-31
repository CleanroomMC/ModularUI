package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.FluidDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.AnimatedText;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.fakeworld.ArraySchema;
import com.cleanroommc.modularui.utils.fakeworld.BlockHighlight;
import com.cleanroommc.modularui.utils.fakeworld.SchemaRenderer;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.DynamicLinkedSyncHandler;
import com.cleanroommc.modularui.value.sync.DynamicSyncHandler;
import com.cleanroommc.modularui.value.sync.GenericListSyncHandler;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.EmptyWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.DynamicSyncedWidget;
import com.cleanroommc.modularui.widgets.Expandable;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SchemaWidget;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class TestTile extends TileEntity implements IGuiHolder<PosGuiData>, ITickable {

    private static final Object2IntMap<Item> handlerSizeMap = new Object2IntOpenHashMap<>() {{
        put(Items.DIAMOND, 9);
        put(Items.EMERALD, 9);
        put(Items.GOLD_INGOT, 7);
        put(Items.IRON_INGOT, 6);
        put(Items.CLAY_BALL, 2);
        defaultReturnValue(3);
    }};

    private final FluidTank fluidTank = new FluidTank(10000);
    private final FluidTank fluidTankPhantom = new FluidTank(500000);
    private long time = 0;
    private int val, val2 = 0;
    private String value = "";
    private int intValue = 1234567;
    private double doubleValue = 1;
    private final int duration = 80;
    private int progress = 0;
    private int cycleState = 0;
    private List<Integer> serverInts = new ArrayList<>();
    private ItemStack displayItem = new ItemStack(Items.DIAMOND);
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
    private final ItemStackHandler craftingInventory = new ItemStackHandler(10);
    private final ItemStackHandler storageInventory0 = new ItemStackHandler(1);
    private final Map<Item, ItemStackHandler> stackHandlerMap = new Object2ObjectOpenHashMap<>();

    private int num = 2;

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        settings.customContainer(() -> new CraftingModularContainer(3, 3, this.craftingInventory));
        settings.customGui(() -> TestGuiContainer::new);

        syncManager.registerSlotGroup("item_inv", 3);
        syncManager.registerSlotGroup("mixer_items", 2);

        syncManager.syncValue("mixer_fluids", 0, SyncHandlers.fluidSlot(this.mixerFluids1));
        syncManager.syncValue("mixer_fluids", 1, SyncHandlers.fluidSlot(this.mixerFluids2));
        IntSyncValue cycleStateValue = new IntSyncValue(() -> this.cycleState, val -> this.cycleState = val);
        syncManager.getHyperVisor().syncValue("cycle_state", cycleStateValue);
        syncManager.syncValue("display_item", GenericSyncValue.forItem(() -> this.displayItem, null));
        GenericListSyncHandler<Integer> numberListSyncHandler = GenericListSyncHandler.<Integer>builder()
                .getter(() -> this.serverInts)
                .setter(v -> this.serverInts = v)
                .serializer(PacketBuffer::writeVarInt)
                .deserializer(PacketBuffer::readVarInt)
                .immutableCopy()
                .build();
        syncManager.syncValue("number_list", numberListSyncHandler);
        syncManager.bindPlayerInventory(guiData.getPlayer());

        DynamicSyncHandler dynamicSyncHandler = new DynamicSyncHandler()
                .widgetProvider((syncManager1, packet) -> {
                    ItemStack itemStack = NetworkUtils.readItemStack(packet);
                    if (itemStack.isEmpty()) return new EmptyWidget();
                    Item item = itemStack.getItem();
                    ItemStackHandler handler = stackHandlerMap.computeIfAbsent(item, k -> new ItemStackHandler(handlerSizeMap.getInt(k)));
                    String name = item.getRegistryName().toString();
                    Flow flow = Flow.row();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        int finalI = i;
                        flow.child(new ItemSlot()
                                .syncHandler(syncManager1.getOrCreateSyncHandler(name, i, ItemSlotSH.class, () -> new ItemSlotSH(new ModularSlot(handler, finalI)))));
                    }
                    return flow;
                });

        DynamicLinkedSyncHandler<GenericListSyncHandler<Integer>> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(numberListSyncHandler)
                .widgetProvider((syncManager1, value1) -> {
                    List<Integer> vals = value1.getValue();
                    return new Column()
                            .widthRel(1f)
                            .coverChildrenHeight()
                            .children(vals.size(), i -> IKey.str(String.valueOf(vals.get(i))).asWidget().padding(2))
                            .name("synced number col");
                });

        Rectangle colorPickerBackground = new Rectangle().color(Color.RED.main);
        ModularPanel panel = new ModularPanel("test_tile");
        IPanelHandler panelSyncHandler = syncManager.syncedPanel("other_panel", true, this::openSecondWindow);
        IPanelHandler colorPicker = IPanelHandler.simple(panel, (mainPanel, player) -> new ColorPickerDialog(colorPickerBackground::color, colorPickerBackground.getColor(), true)
                .setDraggable(true)
                .relative(panel)
                .top(0)
                .rightRel(1f), true);
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        panel.resizer()                        // returns object which is responsible for sizing
                .size(176, 220)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel
                .child(new Row()
                        .name("Tab row")
                        .coverChildren()
                        .topRel(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .tab(GuiTextures.TAB_TOP, -1))
                        .child(new PageButton(1, tabController)
                                .tab(GuiTextures.TAB_TOP, 0))
                        .child(new PageButton(2, tabController)
                                .tab(GuiTextures.TAB_TOP, 0))
                        .child(new PageButton(3, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon()))
                        .child(new PageButton(4, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Items.ENDER_EYE).asIcon())))
                .child(new Expandable()
                        .name("expandable")
                        .top(0)
                        .leftRelOffset(1f, 1)
                        .background(GuiTextures.MC_BACKGROUND)
                        .excludeAreaInRecipeViewer()
                        .stencilTransform((r, expanded) -> {
                            r.width = Math.max(20, r.width - 5);
                            r.height = Math.max(20, r.height - 5);
                        })
                        .animationDuration(500)
                        .interpolation(Interpolation.BOUNCE_OUT)
                        .collapsedView(new ItemDrawable(Blocks.CRAFTING_TABLE).asIcon().asWidget().size(20).pos(0, 0))
                        .expandedView(new ParentWidget<>()
                                .name("crafting tab")
                                .coverChildren()
                                .child(new ItemDrawable(Blocks.CRAFTING_TABLE).asIcon().asWidget().size(20).pos(0, 0))
                                .child(SlotGroupWidget.builder()
                                        .row("III  D")
                                        .row("III  O")
                                        .row("III   ")
                                        .key('I', i -> new ItemSlot().slot(new ModularSlot(this.craftingInventory, i))
                                                .addTooltipLine("This slot is empty"))
                                        .key('O', new ItemSlot().slot(new ModularCraftingSlot(this.craftingInventory, 9)))
                                        .key('D', new ItemDisplayWidget().syncHandler("display_item").displayAmount(true))
                                        .build()
                                        .margin(5, 5, 20, 5).name("crafting"))))
                .child(Flow.column()
                        .name("main col")
                        .sizeRel(1f)
                        .paddingBottom(7)
                        .child(new ParentWidget<>()
                                .expanded()
                                .widthRel(1f)
                                .child(new PagedWidget<>()
                                        .name("root parent")
                                        .sizeRel(1f)
                                        .controller(tabController)
                                        .addPage(new ParentWidget<>()
                                                .name("page 1 parent")
                                                .sizeRel(1f, 1f)
                                                .padding(7, 0)
                                                .child(new Row()
                                                        .name("buttons, slots and more tests")
                                                        .height(137)
                                                        .coverChildrenWidth()
                                                        .verticalCenter()
                                                        //.padding(7)
                                                        .child(new Column()
                                                                        .name("buttons and slots test")
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
                                                                                .addTooltipLine("this tooltip is overridden")
                                                                                .size(60, 18)
                                                                                .setTextAlignment(Alignment.Center)
                                                                                .value(SyncHandlers.string(() -> this.value, val -> this.value = val))
                                                                                .margin(0, 2)
                                                                                .hintText("hint"))
                                                                        .child(new TextFieldWidget()
                                                                                .size(60, 18)
                                                                                .paddingTop(1)
                                                                                .value(SyncHandlers.doubleNumber(() -> this.doubleValue, val -> this.doubleValue = val))
                                                                                .setNumbersDouble(Function.identity())
                                                                                .hintText("number"))
                                                                        //.child(IKey.str("Test string").asWidget().padding(2).debugName("test string"))
                                                                        .child(new ScrollingTextWidget(IKey.str("Very very long test string")).widthRel(1f).height(16))
                                                                //.child(IKey.EMPTY.asWidget().debugName("Empty IKey"))
                                                        )
                                                        .child(new Column()
                                                                .name("button and slots test 2")
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
                                                                .child(new Row().coverChildrenWidth().height(18)
                                                                        .reverseLayout(false)
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
                                                                        .alwaysShowFull(false)
                                                                        .syncHandler(SyncHandlers.fluidSlot(this.fluidTankPhantom).phantom(true)))
                                                                .child(new Column()
                                                                        .name("button and slots test 3")
                                                                        .coverChildren()
                                                                        .child(new TextFieldWidget()
                                                                                .size(60, 20)
                                                                                .value(SyncHandlers.intNumber(() -> this.intValue, val -> this.intValue = val))
                                                                                .setNumbers(0, 9999999)
                                                                                .hintText("integer")))
                                                        )))
                                        .addPage(new Column()
                                                        .name("Slots test page")
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
                                                                        .build().name("9 slot inv")
                                                                        .placeSortButtonsTopRightVertical()
                                                                //.marginBottom(2)
                                                        )
                                                        .child(SlotGroupWidget.builder()
                                                                .row("FII")
                                                                .row("FII")
                                                                .key('F', index -> new FluidSlot().syncHandler("mixer_fluids", index))
                                                                .key('I', index -> ItemSlot.create(index >= 2).slot(new ModularSlot(this.mixerItems, index).slotGroup("mixer_items")))
                                                                .build().name("mixer inv")
                                                                .disableSortButtons())
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
                                                .name("page 3 parent")
                                                .sizeRel(1f, 1f)
                                                .padding(7)
                                                //.child(SlotGroupWidget.playerInventory())
                                                .child(new SliderWidget()
                                                        .widthRel(1f).bottom(50).height(16) // test overwriting of units
                                                        .top(7)
                                                        .stopper(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
                                                        .background(GuiTextures.SLOT_FLUID))
                                                .child(new ButtonWidget<>()
                                                        .name("color picker button")
                                                        .top(25)
                                                        .background(colorPickerBackground)
                                                        .disableHoverBackground()
                                                        .onMousePressed(mouseButton -> {
                                                            colorPicker.openPanel();
                                                            return true;
                                                        }))
                                                .child(new ListWidget<>()
                                                        .name("test config list")
                                                        .widthRel(1f).top(50).bottom(2)
                                                        /*.child(new Rectangle().setColor(0xFF606060).asWidget()
                                                                .top(1)
                                                                .left(32)
                                                                .size(1, 40))*/
                                                        .child(new Row()
                                                                .name("test config 1")
                                                                .widthRel(1f).coverChildrenHeight()
                                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                                .childPadding(2)
                                                                .child(new CycleButtonWidget()
                                                                        .value(new BoolValue(false))
                                                                        .stateOverlay(GuiTextures.CHECK_BOX)
                                                                        .size(14, 14)
                                                                        .margin(8, 4))
                                                                .child(IKey.str("Boolean config").asWidget()
                                                                        .height(14)))
                                                        .child(new Row()
                                                                .name("test config 2")
                                                                .widthRel(1f).height(14)
                                                                .childPadding(2)
                                                                .child(new TextFieldWidget()
                                                                        .value(new IntValue.Dynamic(() -> this.num, val -> this.num = val))
                                                                        .disableHoverBackground()
                                                                        .setNumbers(1, Short.MAX_VALUE)
                                                                        .setTextAlignment(Alignment.Center)
                                                                        .background(new Rectangle().color(0xFFb1b1b1))
                                                                        .setTextColor(IKey.TEXT_COLOR)
                                                                        .size(20, 14))
                                                                .child(IKey.str("Number config").asWidget()
                                                                        .height(14)))
                                                        .child(IKey.str("Config title").asWidget()
                                                                .color(0xFF404040)
                                                                .alignment(Alignment.CenterLeft)
                                                                .left(5).height(14)
                                                                .tooltip(tooltip -> tooltip.showUpTimer(10)
                                                                        .addLine(IKey.str("Config title tooltip"))))
                                                        .child(new Row()
                                                                .name("test config 3")
                                                                .widthRel(1f).height(14)
                                                                .childPadding(2)
                                                                .child(new CycleButtonWidget()
                                                                        .value(new BoolValue(false))
                                                                        .stateOverlay(GuiTextures.CHECK_BOX)
                                                                        .size(14, 14))
                                                                .child(IKey.str("Boolean config 3").asWidget()
                                                                        .height(14)))))
                                        .addPage(new ParentWidget<>()
                                                .name("page 4 storage")
                                                .sizeRel(1f)
                                                .child(new Column()
                                                                .name("page 4 col, dynamic widgets")
                                                                .padding(7)
                                                                .child(new ItemSlot()
                                                                        .slot(new ModularSlot(this.storageInventory0, 0)
                                                                                .changeListener(((newItem, onlyAmountChanged, client, init) -> {
                                                                                    if (client && !onlyAmountChanged) {
                                                                                        dynamicSyncHandler.notifyUpdate(packet -> NetworkUtils.writeItemStack(packet, newItem));
                                                                                    }
                                                                                }))))
                                                                .child(new DynamicSyncedWidget<>()
                                                                        .widthRel(1f)
                                                                        .syncHandler(dynamicSyncHandler))
                                                        /*.child(new DynamicSyncedWidget<>()
                                                                .widthRel(1f)
                                                                .coverChildrenHeight()
                                                                .syncHandler(dynamicLinkedSyncHandler))*/)
                                        )
                                        .addPage(createSchemaPage(guiData))))
                        .child(SlotGroupWidget.playerInventory(false))
                );
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

    private IWidget createSchemaPage(GuiData data) {
        ParentWidget<?> page = new ParentWidget<>();
        page.name("page 5 schema");
        page.sizeRel(1f);
        page.child(IKey.str("schema").asWidget());
        if (world.isRemote)
            page.child(new SchemaWidget(new SchemaRenderer(ArraySchema.of(data.getPlayer(), 5))
                    .highlightRenderer(new BlockHighlight(Color.withAlpha(Color.GREEN.brighter(1), 0.9f), 1 / 32f)))
                    .pos(20, 20)
                    .size(100, 100));
        return page;
    }

    public ModularPanel openSecondWindow(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        ModularPanel panel = new Dialog<>("second_window", null)
                .setDisablePanelsBelow(false)
                .setCloseOnOutOfBoundsClick(false)
                .setDraggable(true)
                .size(100, 100);
        SlotGroup slotGroup = new SlotGroup("small_inv", 2);
        IntSyncValue timeSync = new IntSyncValue(() -> (int) java.lang.System.currentTimeMillis());
        syncManager.syncValue(123456, timeSync);
        syncManager.registerSlotGroup(slotGroup);
        AtomicInteger number = new AtomicInteger(0);
        syncManager.syncValue("int_value", new IntSyncValue(number::get, number::set));
        IPanelHandler panelSyncHandler = syncManager.syncedPanel("other_panel_2", true, (syncManager1, syncHandler1) ->
                openThirdWindow(syncManager1, syncHandler1, number));
        IntSyncValue num = syncManager.getHyperVisor().findSyncHandler("cycle_state", IntSyncValue.class);
        panel.child(ButtonWidget.panelCloseButton())
                .child(new ButtonWidget<>()
                        .size(10).top(14).right(4)
                        .overlay((new FluidDrawable().setFluid(new FluidStack(FluidRegistry.WATER, 100))), IKey.str("3"))
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
                .child(new CycleButtonWidget()
                        .size(16).pos(5, 5 + 11)
                        .value(num)
                        .stateOverlay(0, IKey.str("1"))
                        .stateOverlay(1, IKey.str("2"))
                        .stateOverlay(2, IKey.str("3"))
                        .addTooltipLine(IKey.str("Hyper Visor test")))
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
                        .resizer(flex -> flex.size(100, 20).align(Alignment.Center))
                        .value(new StringValue.Dynamic(value::get, value::set)))
                .child(new ButtonWidget<>()
                        .resizer(flex -> flex.size(8, 8).top(5).right(5))
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
            if (this.time++ % 20 == 0) {
                if (++this.val2 == 3) this.val2 = 0;
                Collection<Item> vals = ForgeRegistries.ITEMS.getValuesCollection();
                Item item = vals.stream().skip(new Random().nextInt(vals.size())).findFirst().orElse(Items.DIAMOND);
                this.displayItem = new ItemStack(item, 26735987);

                Random rnd = new Random();
                this.serverInts.clear();
                for (int i = 0; i < 5; i++) {
                    this.serverInts.add(rnd.nextInt(100));
                }
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
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            this.animatedKey.draw(context, 0, 0, getArea().w(), getArea().h(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }

        @Override
        public void onMouseStartHover() {
            super.onMouseStartHover();
            this.animatedKey.startAnimation().forward(true);
        }

        @Override
        public void onMouseEndHover() {
            super.onMouseEndHover();
            this.animatedKey.forward(false);
        }
    }
}
