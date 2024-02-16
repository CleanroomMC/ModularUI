package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.IPanelSyncManager;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class PanelSyncManager implements IPanelSyncManager {

    private final Map<String, SyncHandler> syncHandlers = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ObjectOpenHashMap<>();
    private final Map<SyncHandler, String> reverseSyncHandlers = new Object2ObjectOpenHashMap<>();
    private ModularSyncManager modularSyncManager;
    private String panelName;
    private boolean init = true;

    private final List<Consumer<EntityPlayer>> openListener = new ArrayList<>();
    private final List<Consumer<EntityPlayer>> closeListener = new ArrayList<>();

    public PanelSyncManager() {}

    @ApiStatus.Internal
    public void initialize(String panelName, ModularSyncManager msm) {
        this.modularSyncManager = msm;
        this.panelName = panelName;
        this.syncHandlers.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
        this.init = true;
    }

    @ApiStatus.Internal
    public void onOpen() {
        this.openListener.forEach(listener -> listener.accept(getPlayer()));
    }

    @ApiStatus.Internal
    public void onClose() {
        this.closeListener.forEach(listener -> listener.accept(getPlayer()));
    }

    public boolean isInitialised() {
        return this.modularSyncManager != null;
    }

    public void detectAndSendChanges(boolean init) {
        if (!isClient()) {
            for (SyncHandler syncHandler : this.syncHandlers.values()) {
                syncHandler.detectAndSendChanges(init || this.init);
            }
        }
        this.init = false;
    }

    public void receiveWidgetUpdate(String mapKey, int id, PacketBuffer buf) throws IOException {
        SyncHandler syncHandler = this.syncHandlers.get(mapKey);
        if (isClient()) {
            syncHandler.readOnClient(id, buf);
        } else {
            syncHandler.readOnServer(id, buf);
        }
    }

    public ItemStack getCursorItem() {
        return getModularSyncManager().getCursorItem();
    }

    public void setCursorItem(ItemStack stack) {
        getModularSyncManager().setCursorItem(stack);
    }

    public boolean hasSyncHandler(SyncHandler syncHandler) {
        return syncHandler.isValid() && syncHandler.getSyncManager() == this && this.reverseSyncHandlers.containsKey(syncHandler);
    }

    private void putSyncValue(String name, int id, SyncHandler syncHandler) {
        String key = makeSyncKey(name, id);
        String currentKey = this.reverseSyncHandlers.get(syncHandler);
        if (currentKey != null) {
            if (!currentKey.equals(key)) {
                boolean auto = name.startsWith(ModularSyncManager.AUTO_SYNC_PREFIX);
                if (auto != currentKey.startsWith(ModularSyncManager.AUTO_SYNC_PREFIX)) {
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

    public PanelSyncManager syncValue(String name, SyncHandler syncHandler) {
        return syncValue(name, 0, syncHandler);
    }

    public PanelSyncManager syncValue(String name, int id, SyncHandler syncHandler) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(syncHandler, "Sync Handler must not be null");
        putSyncValue(name, id, syncHandler);
        return this;
    }

    public PanelSyncManager syncValue(int id, SyncHandler syncHandler) {
        return syncValue("_", id, syncHandler);
    }

    public PanelSyncManager itemSlot(String key, ModularSlot slot) {
        return itemSlot(key, 0, slot);
    }

    public PanelSyncManager itemSlot(String key, int id, ModularSlot slot) {
        return syncValue(key, id, new ItemSlotSH(slot));
    }

    public PanelSyncManager itemSlot(int id, ModularSlot slot) {
        return itemSlot("_", id, slot);
    }

    public PanelSyncHandler panel(String key, ModularPanel mainPanel, PanelSyncHandler.IPanelBuilder panelBuilder) {
        PanelSyncHandler syncHandler = new PanelSyncHandler(mainPanel, panelBuilder);
        syncValue(key, syncHandler);
        return syncHandler;
    }

    public PanelSyncManager registerSlotGroup(SlotGroup slotGroup) {
        if (!slotGroup.isSingleton()) {
            this.slotGroups.put(slotGroup.getName(), slotGroup);
        }
        return this;
    }

    public PanelSyncManager registerSlotGroup(String name, int rowSize, int shiftClickPriority) {
        return registerSlotGroup(new SlotGroup(name, rowSize, shiftClickPriority, true));
    }

    public PanelSyncManager registerSlotGroup(String name, int rowSize, boolean allowShiftTransfer) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, allowShiftTransfer));
    }

    public PanelSyncManager registerSlotGroup(String name, int rowSize) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, true));
    }

    public PanelSyncManager bindPlayerInventory(EntityPlayer player) {
        return bindPlayerInventory(player, ModularSlot::new);
    }

    public PanelSyncManager bindPlayerInventory(EntityPlayer player, @NotNull SlotFunction slotFunction) {
        if (getSlotGroup(ModularSyncManager.PLAYER_INVENTORY) != null) {
            throw new IllegalStateException("The player slot group is already registered!");
        }
        PlayerMainInvWrapper playerInventory = new PlayerMainInvWrapper(player.inventory);
        String key = "player";
        for (int i = 0; i < 36; i++) {
            itemSlot(key, i, slotFunction.apply(playerInventory, i).slotGroup(ModularSyncManager.PLAYER_INVENTORY));
        }
        // player inv sorting is handled by bogosorter
        registerSlotGroup(new SlotGroup(ModularSyncManager.PLAYER_INVENTORY, 9, SlotGroup.PLAYER_INVENTORY_PRIO, true).setAllowSorting(false));
        return this;
    }

    public interface SlotFunction {

        @NotNull
        ModularSlot apply(@NotNull PlayerMainInvWrapper playerInv, int index);
    }

    public PanelSyncManager addOpenListener(Consumer<EntityPlayer> listener) {
        this.openListener.add(listener);
        return this;
    }

    public PanelSyncManager addCloseListener(Consumer<EntityPlayer> listener) {
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
        return getModularSyncManager().getPlayer();
    }

    public ModularSyncManager getModularSyncManager() {
        if (!isInitialised()) {
            throw new IllegalStateException("PanelSyncManager is not yet initialised!");
        }
        return modularSyncManager;
    }

    public ModularContainer getContainer() {
        return getModularSyncManager().getContainer();
    }

    public String getPanelName() {
        return panelName;
    }

    public boolean isClient() {
        return getModularSyncManager().isClient();
    }

    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
