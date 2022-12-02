package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.SyncHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public class ItemSlotSH extends SyncHandler {

    private final Slot slot;

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
        syncHandler.getContainer().addSlotToContainer(this.slot);
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
}
