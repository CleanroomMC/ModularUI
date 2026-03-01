package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.MultiFluidTankHandler;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.DynamicLinkedSyncHandler;
import com.cleanroommc.modularui.value.sync.DynamicSyncHandler;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GenericListSyncHandler;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.EmptyWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.DynamicSyncedWidget;
import com.cleanroommc.modularui.widgets.Expandable;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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

/**
 * Various test and demos for synced widgets, slots and JEI interactions. Anything that doesn't fall into any of those categories goes into
 * {@link TestGuis}.
 */
public class TestTile extends TileEntity implements IGuiHolder<PosGuiData>, ITickable {

    private static final Object2IntMap<Item> handlerSizeMap = new Object2IntOpenHashMap<>() {{
        put(Items.DIAMOND, 9);
        put(Items.EMERALD, 9);
        put(Items.GOLD_INGOT, 7);
        put(Items.IRON_INGOT, 6);
        put(Items.CLAY_BALL, 2);
        defaultReturnValue(3);
    }};

    private long time = 0;
    private final int duration = 80;
    private int progress = 0;
    private int cycleState = 0;
    private List<Integer> serverInts = new ArrayList<>();
    private ItemStack displayItem = new ItemStack(Items.DIAMOND);
    private final ItemStackHandler storage = new ItemStackHandler(9);
    private final ItemStackHandler smallStorage = new ItemStackHandler(9);
    private final ItemStackHandler oversizedStorage = new ItemStackHandler(3) {
        @Override
        public int getSlotLimit(int slot) {
            return 10000;
        }
    };
    private final ItemStackHandler phantomStorage = new ItemStackHandler(3);
    private final MultiFluidTankHandler fluidStorage = new MultiFluidTankHandler(3, 10000);
    private final MultiFluidTankHandler phantomFluidStorage = new MultiFluidTankHandler(3, 500000);
    private final ItemStackHandler craftingInventory = new ItemStackHandler(10);
    private final ItemStackHandler storageInventory0 = new ItemStackHandler(1);
    private final Map<Item, ItemStackHandler> stackHandlerMap = new Object2ObjectOpenHashMap<>();

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        settings.customContainer(() -> new CraftingModularContainer(3, 3, this.craftingInventory));
        settings.customGui(() -> TestGuiContainer::new);

        syncManager.registerSlotGroup("item_inv", 3);
        IntSyncValue cycleStateValue = new IntSyncValue(() -> this.cycleState, val -> this.cycleState = val);
        syncManager.getHyperVisor().syncValue("cycle_state", cycleStateValue);
        syncManager.syncValue("progress", new DoubleSyncValue(() -> (double) this.progress / this.duration));
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
                    return Flow.row()
                            .widthRel(1f)
                            .coverChildrenHeight()
                            .mainAxisAlignment(Alignment.MainAxis.SPACE_AROUND)
                            .children(vals.size(), i -> IKey.str(String.valueOf(vals.get(i))).asWidget().padding(2))
                            .name("synced number col");
                });

        ModularPanel panel = new ModularPanel("test_tile");
        IPanelHandler panelSyncHandler = syncManager.syncedPanel("other_panel", true, this::openSecondWindow);

        PagedWidget.Controller tabController = new PagedWidget.Controller();
        panel.resizer()                        // returns object which is responsible for sizing
                .size(176, 210)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel
                .child(Flow.row()
                        .name("Tab row")
                        .coverChildren()
                        .topRel(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .tab(GuiTextures.TAB_TOP, -1)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon()))
                        .child(new PageButton(1, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Blocks.ENDER_CHEST).asIcon())))
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
                                .child(IKey.str("Expandable & Crafting Demo").asWidget().scale(0.7f).pos(20, 7))
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
                .child(Flow.col()
                        .name("main_col")
                        .sizeRel(1f)
                        .padding(7)
                        .childPadding(5)
                        .child(new PagedWidget<>()
                                .name("paged")
                                .widthRel(1f)
                                .expanded()
                                .controller(tabController)
                                .addPage(new ParentWidget<>()
                                        .name("page 1")
                                        .sizeRel(1f)
                                        .child(Flow.row()
                                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                                .child(Flow.col()
                                                        .name("buttons_and_values_col")
                                                        .widthRel(0.5f)
                                                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                        .child(IKey.str("Storage slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("III", "III")
                                                                .key('I', i -> new ItemSlot().slot(new ModularSlot(this.storage, i)))
                                                                .slotGroup("item_inv")
                                                                .build()
                                                                .placeSortButtonsTopRightVertical())
                                                        .child(new ButtonWidget<>()
                                                                .height(16)
                                                                .width(3 * 18)
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
                                                                    panelSyncHandler.openPanel();
                                                                    return true;
                                                                })
                                                                .overlay(IKey.str("Open Sub Panel").scale(0.75f)))
                                                        .child(Flow.row()
                                                                .name("cycle_button_row")
                                                                .coverChildrenWidth().height(18)
                                                                .reverseLayout(false)
                                                                .child(new ToggleButton()
                                                                        .valueWrapped(cycleStateValue, 0)
                                                                        .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 0, 1, 1 / 3f)))
                                                                .child(new ToggleButton()
                                                                        .valueWrapped(cycleStateValue, 1)
                                                                        .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 1 / 3f, 1, 2 / 3f)))
                                                                .child(new ToggleButton()
                                                                        .valueWrapped(cycleStateValue, 2)
                                                                        .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 2 / 3f, 1, 1))))
                                                        .child(Flow.row()
                                                                .name("progress_row")
                                                                .height(18)
                                                                .mainAxisAlignment(Alignment.MainAxis.SPACE_AROUND)
                                                                .child(new ProgressWidget()
                                                                        .syncHandler("progress")
                                                                        .texture(GuiTextures.PROGRESS_ARROW, 20))
                                                                .child(new ProgressWidget()
                                                                        .syncHandler("progress")
                                                                        .texture(GuiTextures.PROGRESS_CYCLE, 20)
                                                                        .direction(ProgressWidget.Direction.CIRCULAR_CW))
                                                        )
                                                )
                                                .child(Flow.col()
                                                        .name("slots_col")
                                                        .widthRel(0.5f)
                                                        .heightRelOffset(1f, -6) // space for player sort buttons
                                                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                        .child(IKey.str("Oversized slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("III")
                                                                .key('I', i -> new ItemSlot().slot(new ModularSlot(this.oversizedStorage, i).ignoreMaxStackSize(true)))
                                                                .build())
                                                        .child(IKey.str("Phantom slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("III")
                                                                .key('I', i -> new PhantomItemSlot().slot(new ModularSlot(this.phantomStorage, i).ignoreMaxStackSize(true)))
                                                                .build())
                                                        .child(IKey.str("Fluid slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("FFF")
                                                                .key('F', i -> new FluidSlot().syncHandler(new FluidSlotSyncHandler(this.fluidStorage, i)))
                                                                .build())
                                                        .child(IKey.str("Phantom Fluid slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("FFF")
                                                                .key('F', i -> new FluidSlot().syncHandler(new FluidSlotSyncHandler(this.phantomFluidStorage, i).phantom(true)))
                                                                .build())
                                                )))
                                .addPage(new ParentWidget<>()
                                        .name("dynamic_sync_page")
                                        .sizeRel(1f)
                                        .child(Flow.col()
                                                .name("page 4 col, dynamic widgets")
                                                .child(IKey.str("Dynamic synced widget demo. Items act as keys to a unique storage with different amount of slots.").asWidget().scale(0.7f))
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
                                                .child(IKey.str("Dynamic linked sync handler demo.").asWidget().scale(0.7f).marginTop(6))
                                                .child(new DynamicSyncedWidget<>()
                                                        .widthRel(1f)
                                                        .coverChildrenHeight()
                                                        .syncHandler(dynamicLinkedSyncHandler))
                                        )))
                        .child(SlotGroupWidget.playerInventory(false)));
        return panel;
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
                        .overlay(IKey.str("O"))
                        .addTooltipLine("Opens another sub panel")
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
                        .key('I', i -> new ItemSlot().slot(new ModularSlot(smallStorage, i).slotGroup(slotGroup)))
                        .build()
                        .center())
                .child(new CycleButtonWidget()
                        .size(16).pos(5, 5 + 11)
                        .value(num)
                        .stateOverlay(0, IKey.str("1"))
                        .stateOverlay(1, IKey.str("2"))
                        .stateOverlay(2, IKey.str("3"))
                        .addTooltipLine(IKey.str("Hyper Visor test")));
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

    @Override
    public void update() {
        if (!this.world.isRemote) {
            if (this.time++ % 20 == 0) {
                Collection<Item> vals = ForgeRegistries.ITEMS.getValuesCollection();
                Item item = vals.stream().skip(new Random().nextInt(vals.size())).findFirst().orElse(Items.DIAMOND);
                this.displayItem = new ItemStack(item, 26735987);
            }
            if (++this.time % 60 == 0) {
                Random rnd = new Random();
                this.serverInts.clear();
                for (int i = 0; i < 5; i++) {
                    this.serverInts.add(rnd.nextInt(100));
                }
            }
            if (++this.progress == this.duration) {
                this.progress = 0;
            }
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("item_inv", this.storage.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.storage.deserializeNBT(compound.getCompoundTag("item_inv"));
    }
}
