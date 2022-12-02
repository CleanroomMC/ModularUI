package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.SyncHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotDelegate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public class GuiSyncHandler {

    private final EntityPlayer player;
    private final Map<MapKey, SyncHandler> syncedValues = new Object2ObjectOpenHashMap<>();
    private ModularContainer container;
    private boolean frozen;

    private final Map<MapKey, Widget<?>> syncedWidgets = new Object2ObjectOpenHashMap<>();

    public GuiSyncHandler(EntityPlayer player) {
        this.player = player;
        String key = "player";
        for (int i = 0; i < 9; i++) {
            Slot slot = player.inventoryContainer.getSlot(i + 36);
            syncValue(key, i, new ItemSlotSH(SlotDelegate.create(slot)));
        }
        for (int i = 0; i < 27; i++) {
            Slot slot = player.inventoryContainer.getSlot(i + 9);
            syncValue(key, i + 9, new ItemSlotSH(SlotDelegate.create(slot)));
        }
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

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        this.frozen = true;
    }

    public void detectAndSendChanges(boolean init) {
        for (SyncHandler syncHandler : this.syncedValues.values()) {
            syncHandler.detectAndSendChanges(init);
        }
    }

    public void receiveWidgetUpdate(MapKey mapKey, int id, PacketBuffer buf) {
        SyncHandler syncHandler = syncedValues.get(mapKey);
        if (NetworkUtils.isClient(this.player)) {
            syncHandler.readOnClient(id, buf);
        } else {
            syncHandler.readOnServer(id, buf);
        }
    }

    @ApiStatus.Internal
    public void registerSyncedWidget(MapKey mapKey, Widget<?> widget) {
        this.syncedWidgets.put(mapKey, widget);
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
