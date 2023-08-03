package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class GuiSyncManager {

    public static final String AUTO_SYNC_PREFIX = "auto_sync:";
    private static final String PLAYER_INVENTORY = "player_inventory";

    private static final String CURSOR_KEY = makeSyncKey("cursor_slot", 255255);
    private final CursorSlotSyncHandler cursorSlotSyncHandler = new CursorSlotSyncHandler();
    private final EntityPlayer player;
    private final PlayerMainInvWrapper playerInventory;
    private final Map<String, SyncHandler> syncedValues = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ObjectOpenHashMap<>();
    private ModularContainer container;

    private final List<Consumer<EntityPlayer>> openListener = new ArrayList<>();
    private final List<Consumer<EntityPlayer>> closeListener = new ArrayList<>();

    public GuiSyncManager(EntityPlayer player) {
        this.player = player;
        this.playerInventory = new PlayerMainInvWrapper(player.inventory);
        syncValue(CURSOR_KEY, this.cursorSlotSyncHandler);
        String key = "player";
        for (int i = 0; i < 36; i++) {
            itemSlot(key, i, SyncHandlers.itemSlot(this.playerInventory, i).slotGroup(PLAYER_INVENTORY));
        }
        // player inv sorting is handled by bogosorter
        registerSlotGroup(new SlotGroup(PLAYER_INVENTORY, 9, SlotGroup.PLAYER_INVENTORY_PRIO, true).setAllowSorting(false));
    }

    @ApiStatus.Internal
    public void construct(ModularContainer container) {
        if (this.container != null) {
            throw new IllegalStateException("Tried to initialise GuiSyncManager twice!");
        }
        this.container = Objects.requireNonNull(container, "ModularContainer must not be null!");
        this.syncedValues.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
    }

    @ApiStatus.Internal
    public void onOpen() {
        this.openListener.forEach(listener -> listener.accept(this.player));
    }

    @ApiStatus.Internal
    public void onClose() {
        this.closeListener.forEach(listener -> listener.accept(this.player));
    }

    public boolean isInitialised() {
        return this.container != null;
    }

    public ItemStack getCursorItem() {
        return getPlayer().inventory.getItemStack();
    }

    public void setCursorItem(ItemStack item) {
        getPlayer().inventory.setItemStack(item);
        this.cursorSlotSyncHandler.sync();
    }

    public void detectAndSendChanges(boolean init) {
        if (!NetworkUtils.isClient(this.player)) {
            for (SyncHandler syncHandler : this.syncedValues.values()) {
                syncHandler.detectAndSendChanges(init);
            }
        }
    }

    public void receiveWidgetUpdate(String mapKey, int id, PacketBuffer buf) throws IOException {
        SyncHandler syncHandler = this.syncedValues.get(mapKey);
        if (NetworkUtils.isClient(this.player)) {
            syncHandler.readOnClient(id, buf);
        } else {
            syncHandler.readOnServer(id, buf);
        }
    }

    public GuiSyncManager syncValue(String key, SyncHandler syncHandler) {
        if (key == null) throw new NullPointerException("Key must not be null");
        if (syncHandler == null) throw new NullPointerException("Sync Handler must not be null");
        if (!key.startsWith(AUTO_SYNC_PREFIX) && this.syncedValues.containsKey(key)) {
            throw new IllegalStateException("Sync Handler with key " + key + " already exists!");
        }
        this.syncedValues.put(key, syncHandler);
        if (isInitialised()) {
            syncHandler.init(key, this);
        }
        return this;
    }

    public GuiSyncManager syncValue(String name, int id, SyncHandler syncHandler) {
        return syncValue(makeSyncKey(name, id), syncHandler);
    }

    public GuiSyncManager syncValue(int id, SyncHandler syncHandler) {
        return syncValue(makeSyncKey(id), syncHandler);
    }

    public GuiSyncManager itemSlot(String key, ModularSlot slot) {
        return syncValue(key, new ItemSlotSH(slot));
    }

    public GuiSyncManager itemSlot(String key, int id, ModularSlot slot) {
        return itemSlot(makeSyncKey(key, id), slot);
    }

    public GuiSyncManager itemSlot(int id, ModularSlot slot) {
        return itemSlot(makeSyncKey(id), slot);
    }

    public GuiSyncManager registerSlotGroup(SlotGroup slotGroup) {
        if (!slotGroup.isSingleton()) {
            this.slotGroups.put(slotGroup.getName(), slotGroup);
        }
        return this;
    }

    public GuiSyncManager registerSlotGroup(String name, int rowSize, int shiftClickPriority) {
        return registerSlotGroup(new SlotGroup(name, rowSize, shiftClickPriority, true));
    }

    public GuiSyncManager registerSlotGroup(String name, int rowSize, boolean allowShiftTransfer) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, allowShiftTransfer));
    }

    public GuiSyncManager registerSlotGroup(String name, int rowSize) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, true));
    }

    public GuiSyncManager addOpenListener(Consumer<EntityPlayer> listener) {
        this.openListener.add(listener);
        return this;
    }

    public GuiSyncManager addCloseListener(Consumer<EntityPlayer> listener) {
        this.closeListener.add(listener);
        return this;
    }

    public SlotGroup getSlotGroup(String name) {
        return this.slotGroups.get(name);
    }

    public Collection<SlotGroup> getSlotGroups() {
        return this.slotGroups.values();
    }

    public SyncHandler getSyncHandler(String mapKey) {
        return this.syncedValues.get(mapKey);
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public ModularContainer getContainer() {
        return this.container;
    }

    public PlayerMainInvWrapper getPlayerInventory() {
        return this.playerInventory;
    }

    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }

    public static String makeSyncKey(int id) {
        return "_:" + id;
    }
}
