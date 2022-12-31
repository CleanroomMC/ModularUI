package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.SyncHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ItemSlotSH extends SyncHandler {

    public static final int PLAYER_HOTBAR_PRIO = 0;
    public static final int PLAYER_INVENTORY_PRIO = 100;
    public static final int STORAGE_SLOT_PRIO = 200;

    private final Slot slot;
    private int shiftClickPriority = 100;
    private boolean allowShiftClick = true;
    private Predicate<ItemStack> filter;

    public ItemSlotSH(Slot slot) {
        this.slot = slot;
    }

    public ItemSlotSH(IItemHandlerModifiable itemHandler, int index) {
        this(new SlotItemHandler(itemHandler, index, 0, 0));
    }

    public ItemSlotSH(IInventory itemHandler, int index) {
        this(new Slot(itemHandler, index, 0, 0));
    }

    @Override
    public void init(MapKey key, GuiSyncHandler syncHandler) {
        super.init(key, syncHandler);
        syncHandler.getContainer().registerSlot(this);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {

    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {

    }

    public Slot getSlot() {
        return slot;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().isItemValid(itemStack) && (this.filter == null || this.filter.test(itemStack));
    }

    public int getShiftClickPriority() {
        return shiftClickPriority;
    }

    public boolean isAllowShiftClick() {
        return allowShiftClick;
    }

    public boolean isPhantom() {
        return false;
    }

    public ItemSlotSH allowShiftClick(boolean allowShiftClick) {
        this.allowShiftClick = allowShiftClick;
        return this;
    }

    public ItemSlotSH shiftClickPriority(int shiftClickPriority) {
        this.shiftClickPriority = shiftClickPriority;
        return this;
    }

    public ItemSlotSH filter(Predicate<ItemStack> filter) {
        this.filter = filter;
        return this;
    }
}
