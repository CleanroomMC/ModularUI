package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.core.mixin.ContainerAccessor;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.*;

import java.util.*;

@Interface(modid = ModularUI.BOGO_SORT, iface = "com.cleanroommc.bogosorter.api.ISortableContainer")
public class ModularContainer extends Container implements ISortableContainer {

    public static ModularContainer getCurrent(EntityPlayer player) {
        if (player.openContainer instanceof ModularContainer container) {
            return container;
        }
        return null;
    }

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private EntityPlayer player;
    private ModularSyncManager syncManager;
    private boolean init = true;
    // all phantom slots (inventory don't contain phantom slots)
    private final List<ModularSlot> phantomSlots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();
    private GuiData guiData;
    private UISettings settings;

    @SideOnly(Side.CLIENT)
    private ModularScreen optionalScreen;

    public ModularContainer() {}

    @ApiStatus.Internal
    public void construct(EntityPlayer player, PanelSyncManager panelSyncManager, UISettings settings, String mainPanelName, GuiData guiData) {
        this.player = player;
        this.syncManager = new ModularSyncManager(this);
        this.syncManager.construct(mainPanelName, panelSyncManager);
        this.settings = settings;
        this.guiData = guiData;
        sortShiftClickSlots();
    }

    @SideOnly(Side.CLIENT)
    void initializeClient(ModularScreen screen) {
        this.optionalScreen = screen;
    }

    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public void constructClientOnly() {
        this.player = Minecraft.getMinecraft().player;
        this.syncManager = null;
    }

    public boolean isInitialized() {
        return this.player != null;
    }

    @SideOnly(Side.CLIENT)
    public ModularScreen getScreen() {
        if (this.optionalScreen == null) throw new NullPointerException("ModularScreen is not yet initialised!");
        return optionalScreen;
    }

    public ContainerAccessor acc() {
        return (ContainerAccessor) this;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onContainerClosed(@NotNull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.syncManager != null) {
            this.syncManager.onClose();
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.syncManager != null) {
            this.syncManager.detectAndSendChanges(this.init);
        }
        this.init = false;
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(Comparator.comparingInt(slot -> Objects.requireNonNull(slot.getSlotGroup()).getShiftClickPriority()));
    }

    @Override
    public void setAll(@NotNull List<ItemStack> items) {
        if (this.inventorySlots.size() != items.size()) {
            ModularUI.LOGGER.error("Here are {} slots, but expected {}", this.inventorySlots.size(), items.size());
        }
        for (int i = 0; i < Math.min(this.inventorySlots.size(), items.size()); ++i) {
            this.getSlot(i).putStack(items.get(i));
        }
    }

    @ApiStatus.Internal
    public void registerSlot(String panelName, ModularSlot slot) {
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException("Tried to register slot which already exists!");
        }
        if (slot.isPhantom()) {
            this.phantomSlots.add(slot);
        } else {
            addSlotToContainer(slot);
        }
        if (slot.getSlotGroupName() != null) {
            SlotGroup slotGroup = getSyncManager().getSlotGroup(panelName, slot.getSlotGroupName());
            if (slotGroup == null) {
                ModularUI.LOGGER.throwing(new IllegalArgumentException("SlotGroup '" + slot.getSlotGroupName() + "' is not registered!"));
                return;
            }
            slot.slotGroup(slotGroup);
        }
        if (slot.getSlotGroup() != null) {
            SlotGroup slotGroup = slot.getSlotGroup();
            if (slotGroup.allowShiftTransfer()) {
                this.shiftClickSlots.add(slot);
                if (!this.init) {
                    sortShiftClickSlots();
                }
            }
        }
    }

    @Contract("_, null, null -> fail")
    @NotNull
    @ApiStatus.Internal
    public SlotGroup validateSlotGroup(String panelName, @Nullable String slotGroupName, @Nullable SlotGroup slotGroup) {
        if (slotGroup != null) {
            if (getSyncManager().getSlotGroup(panelName, slotGroup.getName()) == null) {
                throw new IllegalArgumentException("Slot group is not registered in the GUI.");
            }
            return slotGroup;
        }
        if (slotGroupName != null) {
            slotGroup = getSyncManager().getSlotGroup(panelName, slotGroupName);
            if (slotGroup == null) {
                throw new IllegalArgumentException("Can't find slot group for name " + slotGroupName);
            }
            return slotGroup;
        }
        throw new IllegalArgumentException("Either the slot group or the name must not be null!");
    }

    public ModularSyncManager getSyncManager() {
        if (this.syncManager == null) {
            throw new IllegalStateException("GuiSyncManager is not available for client only GUI's.");
        }
        return this.syncManager;
    }

    public boolean isClient() {
        return this.syncManager == null || NetworkUtils.isClient(this.player);
    }

    public boolean isClientOnly() {
        return this.syncManager == null;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public GuiData getGuiData() {
        return guiData;
    }

    public ModularSlot getModularSlot(int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot instanceof ModularSlot modularSlot) {
            return modularSlot;
        }
        throw new IllegalStateException("A non-ModularSlot was found, but all slots in a ModularContainer must extend ModularSlot.");
    }

    public List<ModularSlot> getShiftClickSlots() {
        return Collections.unmodifiableList(this.shiftClickSlots);
    }

    public void onSlotChanged(ModularSlot slot, ItemStack stack, boolean onlyAmountChanged) {}

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return this.settings.canPlayerInteractWithUI(playerIn);
    }

    @Override
    public @NotNull ItemStack slotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        ItemStack returnable = ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT || acc().getDragEvent() != 0) {
            return superSlotClick(slotId, mouseButton, clickTypeIn, player);
        }

        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) &&
                (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {
            if (slotId == DROP_TO_WORLD) {
                // no dif
                if (!inventoryplayer.getItemStack().isEmpty()) {
                    if (mouseButton == LEFT_MOUSE) {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                    }

                    if (mouseButton == RIGHT_MOUSE) {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
                return inventoryplayer.getItemStack();
            }

            // early return
            if (slotId < 0) return ItemStack.EMPTY;

            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = getSlot(slotId);

                if (!fromSlot.canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }
                // simpler code, but effectivly no difference
                returnable = transferStackInSlot(player, slotId);
            } else {
                Slot clickedSlot = getSlot(slotId);

                ItemStack slotStack = clickedSlot.getStack();
                ItemStack heldStack = inventoryplayer.getItemStack();

                if (slotStack.isEmpty()) {
                    // no dif
                    if (!heldStack.isEmpty() && clickedSlot.isItemValid(heldStack)) {
                        int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                        if (stackCount > clickedSlot.getItemStackLimit(heldStack)) {
                            stackCount = clickedSlot.getItemStackLimit(heldStack);
                        }

                        clickedSlot.putStack(heldStack.splitStack(stackCount));
                    }
                } else if (clickedSlot.canTakeStack(player)) {
                    if (heldStack.isEmpty() && !slotStack.isEmpty()) {
                        int s = Math.min(slotStack.getCount(), slotStack.getMaxStackSize()); // checking max stack size here, probably for oversized slots
                        int toRemove = mouseButton == LEFT_MOUSE ? s : (s + 1) / 2;
                        inventoryplayer.setItemStack(slotStack.splitStack(toRemove));
                        clickedSlot.putStack(slotStack);
                        clickedSlot.onTake(player, inventoryplayer.getItemStack());
                    } else if (clickedSlot.isItemValid(heldStack)) {
                        if (slotStack.getItem() == heldStack.getItem() &&
                                slotStack.getMetadata() == heldStack.getMetadata() &&
                                ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                            if (stackCount > clickedSlot.getItemStackLimit(heldStack) - slotStack.getCount()) {
                                stackCount = clickedSlot.getItemStackLimit(heldStack) - slotStack.getCount();
                            }

                            heldStack.shrink(stackCount);
                            slotStack.grow(stackCount);
                            clickedSlot.putStack(slotStack);

                        } else if (heldStack.getCount() <= clickedSlot.getItemStackLimit(heldStack)) {
                            clickedSlot.putStack(heldStack);
                            inventoryplayer.setItemStack(slotStack);
                        }
                    } else if (slotStack.getItem() == heldStack.getItem() &&
                            heldStack.getMaxStackSize() > 1 &&
                            (!slotStack.getHasSubtypes() || slotStack.getMetadata() == heldStack.getMetadata()) &&
                            ItemStack.areItemStackTagsEqual(slotStack, heldStack) && !slotStack.isEmpty()) {
                        int stackCount = slotStack.getCount();

                        if (stackCount + heldStack.getCount() <= heldStack.getMaxStackSize()) {
                            heldStack.grow(stackCount);
                            slotStack = clickedSlot.decrStackSize(stackCount);

                            if (slotStack.isEmpty()) {
                                clickedSlot.putStack(ItemStack.EMPTY);
                            }

                            clickedSlot.onTake(player, inventoryplayer.getItemStack());
                        }
                    }
                }
                clickedSlot.onSlotChanged();
            }
            detectAndSendChanges();
            return returnable;
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot = inventorySlots.get(slotId);
            ItemStack itemstack1 = inventoryplayer.getItemStack();

            if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                int i = mouseButton == 0 ? 0 : inventorySlots.size() - 1;
                int j = mouseButton == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < inventorySlots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j) {
                        Slot slot1 = inventorySlots.get(l);
                        if (slot1 instanceof ModularSlot modularSlot && modularSlot.isPhantom()) continue;

                        if (slot1.getHasStack() && Container.canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && canMergeSlot(itemstack1, slot1)) {
                            ItemStack itemstack2 = slot1.getStack();

                            if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.grow(i1);

                                if (itemstack3.isEmpty()) {
                                    slot1.putStack(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack3);
                            }
                        }
                    }
                }
            }

            detectAndSendChanges();
            return returnable;
        } else if (clickTypeIn == ClickType.SWAP && mouseButton >= 0 && mouseButton < 9) {
            ModularSlot phantom = getModularSlot(slotId);
            ItemStack hotbarStack = inventoryplayer.getStackInSlot(mouseButton);
            if (phantom.isPhantom()) {
                // insert stack from hotbar slot into phantom slot
                phantom.putStack(hotbarStack.isEmpty() ? ItemStack.EMPTY : hotbarStack.copy());
                detectAndSendChanges();
                return returnable;
            }
        }

        return superSlotClick(slotId, mouseButton, clickTypeIn, player);
    }

    protected final @NotNull ItemStack superSlotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        return super.slotClick(slotId, mouseButton, clickTypeIn, player);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = getModularSlot(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                stack = stack.copy();
                int base = 0;
                if (stack.getCount() > stack.getMaxStackSize()) {
                    base = stack.getCount() - stack.getMaxStackSize();
                    stack.setCount(stack.getMaxStackSize());
                }
                ItemStack remainder = transferItem(slot, stack.copy());
                if (base == 0 && remainder.isEmpty()) stack = ItemStack.EMPTY;
                else stack.setCount(base + remainder.getCount());
                slot.putStack(stack);
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        // in first iteration only insert into non-empty, non-phantom slots
        for (ModularSlot toSlot : getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isEnabled() && toSlot.isItemValid(fromStack)) {
                ItemStack toStack = toSlot.getStack().copy();
                if (!fromSlot.isPhantom() && ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                    int j = toStack.getCount() + fromStack.getCount();
                    int maxSize = toSlot.getItemStackLimit(fromStack);//Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

                    if (j <= maxSize) {
                        fromStack.setCount(0);
                        toStack.setCount(j);
                        toSlot.putStack(toStack);
                    } else if (toStack.getCount() < maxSize) {
                        fromStack.shrink(maxSize - toStack.getCount());
                        toStack.setCount(maxSize);
                        toSlot.putStack(toStack);
                    }

                    if (fromStack.isEmpty()) {
                        return fromStack;
                    }
                }
            }
        }
        boolean hasNonEmptyPhantom = false;
        // now insert into first empty slot (phantom or not) and check if we have any non-empty phantom slots
        for (ModularSlot toSlot : getShiftClickSlots()) {
            ItemStack itemstack = toSlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isEnabled() && toSlot.isItemValid(fromStack)) {
                if (toSlot.isPhantom()) {
                    if (!itemstack.isEmpty()) {
                        // skip non-empty phantom for now
                        hasNonEmptyPhantom = true;
                    } else {
                        toSlot.putStack(fromStack.copy());
                        return fromStack;
                    }
                } else if (itemstack.isEmpty()) {
                    if (fromStack.getCount() > toSlot.getItemStackLimit(fromStack)) {
                        toSlot.putStack(fromStack.splitStack(toSlot.getItemStackLimit(fromStack)));
                    } else {
                        toSlot.putStack(fromStack.splitStack(fromStack.getCount()));
                    }
                    if (fromStack.getCount() < 1) {
                        break;
                    }
                }
            }
        }
        if (!hasNonEmptyPhantom) return fromStack;

        // now insert into the first phantom slot we can find (will be non-empty)
        // unfortunately, when all phantom slots are used it will always overwrite the first one
        for (ModularSlot toSlot : getShiftClickSlots()) {
            ItemStack itemstack = toSlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isPhantom() && toSlot.isEnabled() && toSlot.isItemValid(fromStack)) {
                // don't check for stackable, just overwrite
                toSlot.putStack(fromStack.copy());
                return fromStack;
            }
        }
        return fromStack;
    }

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        if (this.syncManager != null) {
            this.syncManager.buildSortingContext(builder);
        }
    }

    @Override
    public IPosSetter getPlayerButtonPosSetter() {
        return null;
    }
}
