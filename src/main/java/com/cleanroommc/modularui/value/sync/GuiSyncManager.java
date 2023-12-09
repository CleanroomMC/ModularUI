package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
    private final Map<String, SyncHandler> syncHandlers = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ObjectOpenHashMap<>();
    private final Map<SyncHandler, String> reverseSyncHandlers = new Object2ObjectOpenHashMap<>();
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
        this.syncHandlers.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
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
            for (SyncHandler syncHandler : this.syncHandlers.values()) {
                syncHandler.detectAndSendChanges(init);
            }
        }
    }

    public void receiveWidgetUpdate(String mapKey, int id, PacketBuffer buf) throws IOException {
        SyncHandler syncHandler = this.syncHandlers.get(mapKey);
        if (NetworkUtils.isClient(this.player)) {
            syncHandler.readOnClient(id, buf);
        } else {
            syncHandler.readOnServer(id, buf);
        }
    }

    @ApiStatus.Internal
    public void disposeSyncHandler(SyncHandler syncHandler) {
        String key = this.reverseSyncHandlers.remove(syncHandler);
        if (key != null) {
            this.syncHandlers.remove(key);
            syncHandler.dispose();
        }
    }

    public boolean hasSyncHandler(SyncHandler syncHandler) {
        return syncHandler.isValid() && syncHandler.getSyncManager() == this && this.reverseSyncHandlers.containsKey(syncHandler);
    }

    private void putSyncValue(String name, int id, SyncHandler syncHandler) {
        String key = makeSyncKey(name, id);
        String currentKey = this.reverseSyncHandlers.get(syncHandler);
        if (currentKey != null) {
            if (!currentKey.equals(key)) {
                boolean auto = name.startsWith(AUTO_SYNC_PREFIX);
                if (auto != currentKey.startsWith(AUTO_SYNC_PREFIX)) {
                    throw new IllegalStateException("Old and new sync handler must both be either not auto or auto!");
                }
                if (auto && !currentKey.startsWith(name)) {
                    throw new IllegalStateException("Sync Handler was previously added with a different panel!");
                }
            }
            this.syncHandlers.remove(currentKey);
        }
        this.syncHandlers.put(key, syncHandler);
        this.reverseSyncHandlers.put(syncHandler, key);
        if (isInitialised()) {
            syncHandler.init(key, this);
        }
    }

    public GuiSyncManager syncValue(String name, SyncHandler syncHandler) {
        return syncValue(name, 0, syncHandler);
    }

    public GuiSyncManager syncValue(String name, int id, SyncHandler syncHandler) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(syncHandler, "Sync Handler must not be null");
        putSyncValue(name, id, syncHandler);
        return this;
    }

    public GuiSyncManager syncValue(int id, SyncHandler syncHandler) {
        return syncValue("_", id, syncHandler);
    }

    public GuiSyncManager itemSlot(String key, ModularSlot slot) {
        return itemSlot(key, 0, slot);
    }

    public GuiSyncManager itemSlot(String key, int id, ModularSlot slot) {
        return syncValue(key, id, new ItemSlotSH(slot));
    }

    public GuiSyncManager itemSlot(int id, ModularSlot slot) {
        return itemSlot("_", id, slot);
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
        return this.syncHandlers.get(mapKey);
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

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
