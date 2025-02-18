package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class ItemSlotSH extends SyncHandler {

    private final ModularSlot slot;
    private ItemStack lastStoredItem;
    private boolean registered = false;

    @ApiStatus.Internal
    public ItemSlotSH(ModularSlot slot) {
        this.slot = slot;
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (!registered) {
            getSyncManager().getContainer().registerSlot(getSyncManager().getPanelName(), this.slot, isPhantom());
            this.registered = true;
        }
        this.lastStoredItem = getSlot().getStack().copy();
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        ItemStack itemStack = getSlot().getStack();
        if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.getCount() != this.lastStoredItem.getCount())) {
            onSlotUpdate(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.setCount(itemStack.getCount());
            } else {
                this.lastStoredItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            syncToClient(1, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                buffer.writeItemStack(itemStack);
                buffer.writeBoolean(init);
            });
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            boolean onlyAmountChanged = buf.readBoolean();
            this.lastStoredItem = NetworkUtils.readItemStack(buf);
            onSlotUpdate(this.lastStoredItem, onlyAmountChanged, true, buf.readBoolean());
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    protected void onSlotUpdate(ItemStack stack, boolean onlyAmountChanged, boolean client, boolean init) {
        getSlot().onSlotChangedReal(stack, onlyAmountChanged, client, init);
    }

    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(4, buffer -> buffer.writeBoolean(enabled));
        }
    }

    public void updateFromClient(ItemStack stack) {
        syncToServer(5, buf -> buf.writeItemStack(stack));
    }

    public ModularSlot getSlot() {
        return this.slot;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().isItemValid(itemStack);
    }

    public boolean isPhantom() {
        return false;
    }

    @Nullable
    public String getSlotGroup() {
        return this.slot.getSlotGroupName();
    }
}
