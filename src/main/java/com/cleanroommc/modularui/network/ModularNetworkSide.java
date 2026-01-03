package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.packets.CloseAllGuiPacket;
import com.cleanroommc.modularui.network.packets.CloseGuiPacket;
import com.cleanroommc.modularui.network.packets.PacketSyncHandler;
import com.cleanroommc.modularui.network.packets.ReopenGuiPacket;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

public abstract class ModularNetworkSide {

    private final boolean client;
    private final Int2ReferenceOpenHashMap<ModularSyncManager> activeScreens = new Int2ReferenceOpenHashMap<>();
    private final Reference2IntOpenHashMap<ModularSyncManager> inverseActiveScreens = new Reference2IntOpenHashMap<>();
    // TODO: contextual syncer stack: in game containers shouldn't be closed in closeAll

    ModularNetworkSide(boolean client) {
        this.client = client;
    }

    public boolean isClient() {
        return client;
    }

    abstract void sendPacket(IPacket packet, EntityPlayer player);

    void activateInternal(int networkId, ModularSyncManager manager) {
        if (activeScreens.containsKey(networkId)) throw new IllegalStateException("Network ID " + networkId + " is already active.");
        activeScreens.put(networkId, manager);
        inverseActiveScreens.put(manager, networkId);
    }

    public void closeAll(EntityPlayer player) {
        closeAll(player, true);
    }

    @ApiStatus.Internal
    public void closeAll(EntityPlayer player, boolean sync) {
        if (activeScreens.isEmpty()) return;
        int currentContainer = -1;
        if (player.openContainer instanceof ModularContainer mc && !mc.isClientOnly()) {
            currentContainer = inverseActiveScreens.getInt(mc.getSyncManager());
        }
        var it = activeScreens.int2ReferenceEntrySet().fastIterator();
        while (it.hasNext()) {
            var entry = it.next();
            int nid = entry.getIntKey();
            ModularSyncManager msm = entry.getValue();
            if (nid == currentContainer) closeContainer(player);
            if (!msm.isClosed()) msm.onClose();
            msm.dispose();
        }
        activeScreens.clear();
        inverseActiveScreens.clear();
        if (sync) sendPacket(new CloseAllGuiPacket(), player);
    }

    @ApiStatus.Internal
    public void receivePacket(PacketSyncHandler packet) {
        ModularSyncManager msm = activeScreens.get(packet.networkId);
        if (msm == null) return; // silently discard packets for inactive screens
        try {
            int id = packet.action ? 0 : packet.packet.readVarInt();
            msm.receiveWidgetUpdate(packet.panel, packet.key, packet.action, id, packet.packet);
        } catch (IndexOutOfBoundsException e) {
            ModularUI.LOGGER.error("Failed to read packet for sync handler {} in panel {}", packet.key, packet.panel);
        } catch (IOException e) {
            ModularUI.LOGGER.throwing(e);
        }
    }

    @ApiStatus.Internal
    public void sendSyncHandlerPacket(String panel, SyncHandler syncHandler, PacketBuffer buffer, EntityPlayer player) {
        ModularSyncManager msm = syncHandler.getSyncManager().getModularSyncManager();
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new PacketSyncHandler(id, panel, syncHandler.getKey(), false, buffer), player);
    }

    @ApiStatus.Internal
    public void sendActionPacket(ModularSyncManager msm, String panel, String key, PacketBuffer buffer, EntityPlayer player) {
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new PacketSyncHandler(id, panel, key, true, buffer), player);
    }

    @ApiStatus.Internal
    public void closeContainer(int networkId, boolean dispose, EntityPlayer player, boolean sync) {
        closeContainer(player);
        deactivate(networkId, dispose);
        if (sync) {
            sendPacket(new CloseGuiPacket(networkId, dispose), player);
        }
    }

    abstract void closeContainer(EntityPlayer player);

    void deactivate(int networkId, boolean dispose) {
        ModularSyncManager msm = activeScreens.get(networkId);
        if (msm == null) return;
        if (!msm.isClosed()) msm.onClose();
        if (dispose) {
            activeScreens.remove(networkId);
            inverseActiveScreens.removeInt(msm);
            msm.dispose();
        }
    }

    @ApiStatus.Internal
    public void reopen(EntityPlayer player, ModularSyncManager msm, boolean sync) {
        if (player.openContainer != msm.getContainer()) {
            closeContainer(player);
            player.openContainer = msm.getContainer();
            msm.onOpen();
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, msm.getContainer()));
        }
        if (sync) sendPacket(new ReopenGuiPacket(inverseActiveScreens.getInt(msm)), player);
    }

    @ApiStatus.Internal
    public void reopen(EntityPlayer player, int networkId, boolean sync) {
        reopen(player, activeScreens.get(networkId), sync);
    }
}
