package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ModularSyncManager {

    public static final String AUTO_SYNC_PREFIX = "auto_sync:";
    protected static final String PLAYER_INVENTORY = "player_inventory";
    private static final String CURSOR_KEY = makeSyncKey("cursor_slot", 255255);

    private final Map<String, PanelSyncManager> panelSyncManagerMap = new Object2ObjectOpenHashMap<>();
    // A set of all panels which have been opened during the ui. May also contain closed panels.
    // This is used to detect if packets are arriving too late
    private final Set<String> panelHistory = new ObjectOpenHashSet<>();
    private PanelSyncManager mainPSM;
    private final ModularContainer container;
    private final CursorSlotSyncHandler cursorSlotSyncHandler = new CursorSlotSyncHandler();

    public ModularSyncManager(ModularContainer container) {
        this.container = container;
    }

    @ApiStatus.Internal
    public void construct(String mainPanelName, PanelSyncManager mainPSM) {
        this.mainPSM = mainPSM;
        if (this.mainPSM.getSlotGroup(PLAYER_INVENTORY) == null) {
            this.mainPSM.bindPlayerInventory(getPlayer());
        }
        open(mainPanelName, mainPSM);
        mainPSM.syncValue(CURSOR_KEY, this.cursorSlotSyncHandler);
    }

    public PanelSyncManager getMainPSM() {
        return mainPSM;
    }

    public void detectAndSendChanges(boolean init) {
        this.panelSyncManagerMap.values().forEach(psm -> psm.detectAndSendChanges(init));
    }

    public void onClose() {
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onClose);
    }

    public void onOpen() {
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onOpen);
    }

    public PanelSyncManager getPanelSyncManager(String panelName) {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) return psm;
        throw new NullPointerException("No PanelSyncManager found for name '" + panelName + "'!");
    }

    public SyncHandler getSyncHandler(String panelName, String syncKey) {
        return getPanelSyncManager(panelName).getSyncHandler(syncKey);
    }

    public SlotGroup getSlotGroup(String panelName, String slotGroupName) {
        return getPanelSyncManager(panelName).getSlotGroup(slotGroupName);
    }

    public ItemStack getCursorItem() {
        return getPlayer().inventory.getItemStack();
    }

    public void setCursorItem(ItemStack item) {
        getPlayer().inventory.setItemStack(item);
        this.cursorSlotSyncHandler.sync();
    }

    public void open(String name, PanelSyncManager syncManager) {
        this.panelSyncManagerMap.put(name, syncManager);
        this.panelHistory.add(name);
        syncManager.initialize(name, this);
    }

    public void close(String name) {
        PanelSyncManager psm = this.panelSyncManagerMap.remove(name);
        if (psm != null) psm.onClose();
    }

    public boolean isOpen(String panelName) {
        return this.panelSyncManagerMap.containsKey(panelName);
    }

    public void receiveWidgetUpdate(String panelName, String mapKey, int id, PacketBuffer buf) throws IOException {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) {
            psm.receiveWidgetUpdate(mapKey, id, buf);
        } else if (!this.panelHistory.contains(panelName)) {
            ModularUI.LOGGER.throwing(new IllegalStateException("A packet was send to panel '" + panelName + "' which was not opened yet!."));
        }
        // else the panel was open at some point
        // we simply discard the packet silently and assume the packet was correctly send, but the panel closed earlier
    }

    public EntityPlayer getPlayer() {
        return this.container.getPlayer();
    }

    public ModularContainer getContainer() {
        return container;
    }

    public boolean isClient() {
        return this.container.isClient();
    }

    @Optional.Method(modid = ModularUI.BOGO_SORT)
    public void buildSortingContext(ISortingContextBuilder builder) {
        for (PanelSyncManager psm : this.panelSyncManagerMap.values()) {
            for (SlotGroup slotGroup : psm.getSlotGroups()) {
                if (slotGroup.isAllowSorting() && !isPlayerSlot(slotGroup.getSlots().get(0))) {
                    builder.addSlotGroupOf(slotGroup.getSlots(), slotGroup.getRowSize())
                            .buttonPosSetter(null)
                            .priority(slotGroup.getShiftClickPriority());
                }
            }
        }
    }

    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler slotItemHandler) {
            IItemHandler iItemHandler = slotItemHandler.getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper || iItemHandler instanceof PlayerInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }

    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
