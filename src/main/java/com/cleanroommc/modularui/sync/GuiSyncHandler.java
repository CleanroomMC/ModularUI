package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.SyncHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.widgets.slot.SlotDelegate;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class GuiSyncHandler {

    private static final String PLAYER_INVENTORY = "player_inventory";

    private static final MapKey CURSOR_KEY = new MapKey("cursor_slot", 255255);
    private final CursorSlotSyncHandler cursorSlotSyncHandler = new CursorSlotSyncHandler();
    private final EntityPlayer player;
    private final Map<MapKey, SyncHandler> syncedValues = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ObjectOpenHashMap<>();
    private ModularContainer container;
    private boolean frozen;

    public GuiSyncHandler(EntityPlayer player) {
        this.player = player;
        syncValue(CURSOR_KEY, cursorSlotSyncHandler);
        String key = "player";
        for (int i = 0; i < 9; i++) {
            Slot slot = player.inventoryContainer.getSlot(i + 36);
            syncValue(key, i, SyncHandlers.itemSlot(SlotDelegate.create(slot)).slotGroup(PLAYER_INVENTORY));
        }
        for (int i = 0; i < 27; i++) {
            Slot slot = player.inventoryContainer.getSlot(i + 9);
            syncValue(key, i + 9, SyncHandlers.itemSlot(SlotDelegate.create(slot)).slotGroup(PLAYER_INVENTORY));
        }
        // player inv sorting is handled by bogosorter
        registerSlotGroup(new SlotGroup(PLAYER_INVENTORY, 9, SlotGroup.PLAYER_INVENTORY_PRIO, true).setAllowSorting(false));
    }

    public void construct(ModularContainer container) {
        if (this.container != null) {
            throw new IllegalStateException();
        }
        if (container == null) {
            throw new NullPointerException();
        }
        this.container = container;
        this.syncedValues.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
    }

    public ItemStack getCursorItem() {
        return getPlayer().inventory.getItemStack();
    }

    public void setCursorItem(ItemStack item) {
        getPlayer().inventory.setItemStack(item);
        this.cursorSlotSyncHandler.sync();
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        this.frozen = true;
    }

    public void detectAndSendChanges(boolean init) {
        if (!NetworkUtils.isClient(this.player)) {
            for (SyncHandler syncHandler : this.syncedValues.values()) {
                syncHandler.detectAndSendChanges(init);
            }
        }
    }

    public void receiveWidgetUpdate(MapKey mapKey, int id, PacketBuffer buf) throws IOException {
        SyncHandler syncHandler = syncedValues.get(mapKey);
        if (NetworkUtils.isClient(this.player)) {
            syncHandler.readOnClient(id, buf);
        } else {
            syncHandler.readOnServer(id, buf);
        }
    }

    public GuiSyncHandler syncValue(MapKey key, SyncHandler syncHandler) {
        if (key == null) throw new NullPointerException("Key must not be null");
        if (syncHandler == null) throw new NullPointerException("Sync Handler must not be null");
        if (this.syncedValues.containsKey(key)) {
            throw new IllegalStateException("Sync Handler with key " + key + " already exists!");
        }
        this.syncedValues.put(key, syncHandler);
        return this;
    }

    public GuiSyncHandler syncValue(String name, int id, SyncHandler syncHandler) {
        return syncValue(new MapKey(name, id), syncHandler);
    }

    public GuiSyncHandler syncValue(String name, SyncHandler syncHandler) {
        return syncValue(new MapKey(name), syncHandler);
    }

    public GuiSyncHandler syncValue(int id, SyncHandler syncHandler) {
        return syncValue(new MapKey(id), syncHandler);
    }

    public GuiSyncHandler registerSlotGroup(SlotGroup slotGroup) {
        this.slotGroups.put(slotGroup.getName(), slotGroup);
        return this;
    }

    public GuiSyncHandler registerSlotGroup(String name, int rowSize, int shiftClickPriority) {
        return registerSlotGroup(new SlotGroup(name, rowSize, shiftClickPriority, true));
    }

    public GuiSyncHandler registerSlotGroup(String name, int rowSize, boolean allowShiftTransfer) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, allowShiftTransfer));
    }

    public GuiSyncHandler registerSlotGroup(String name, int rowSize) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, true));
    }

    public SlotGroup getSlotGroup(String name) {
        return this.slotGroups.get(name);
    }

    public Collection<SlotGroup> getSlotGroups() {
        return this.slotGroups.values();
    }

    public SyncHandler getSyncHandler(MapKey mapKey) {
        return this.syncedValues.get(mapKey);
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public ModularContainer getContainer() {
        return container;
    }
}
