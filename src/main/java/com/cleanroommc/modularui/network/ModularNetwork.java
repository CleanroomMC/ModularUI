package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.packets.PacketSyncHandler;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.io.IOException;

public abstract class ModularNetwork {

    // These need to be separate instances, otherwise they would access the same maps in singleplayer.
    @SideOnly(Side.CLIENT)
    public static final ModularNetwork CLIENT = new ModularNetwork(true) {
        @Override
        protected void sendPacket(IPacket packet, EntityPlayer player) {
            NetworkHandler.sendToServer(packet);
        }
    };
    public static final ModularNetwork SERVER = new ModularNetwork(false) {
        @Override
        protected void sendPacket(IPacket packet, EntityPlayer player) {
            NetworkHandler.sendToPlayer(packet, (EntityPlayerMP) player);
        }
    };

    public static ModularNetwork get(boolean client) {
        return client ? CLIENT : SERVER;
    }

    public static ModularNetwork get(Side side) {
        return side.isClient() ? CLIENT : SERVER;
    }

    public static ModularNetwork get(EntityPlayer player) {
        return get(NetworkUtils.isClient(player));
    }

    private final boolean client;
    private final Int2ReferenceOpenHashMap<ModularSyncManager> activeScreens = new Int2ReferenceOpenHashMap<>();
    private final Reference2IntOpenHashMap<ModularSyncManager> inverseActiveScreens = new Reference2IntOpenHashMap<>();

    private ModularNetwork(boolean client) {
        this.client = client;
    }

    public boolean isClient() {
        return client;
    }

    protected abstract void sendPacket(IPacket packet, EntityPlayer player);

    public void activate(int networkId, ModularSyncManager manager) {
        if (activeScreens.containsKey(networkId)) throw new IllegalStateException("Network ID " + networkId + " is already active.");
        activeScreens.put(networkId, manager);
        inverseActiveScreens.put(manager, networkId);
    }

    public void deactivate(ModularSyncManager manager) {
        int id = inverseActiveScreens.removeInt(manager);
        activeScreens.remove(id);
    }

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

    public void sendSyncHandlerPacket(String panel, SyncHandler syncHandler, PacketBuffer buffer, EntityPlayer player) {
        ModularSyncManager msm = syncHandler.getSyncManager().getModularSyncManager();
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new PacketSyncHandler(id, panel, syncHandler.getKey(), false, buffer), player);
    }

    public void sendActionPacket(ModularSyncManager msm, String panel, String key, PacketBuffer buffer, EntityPlayer player) {
        if (!inverseActiveScreens.containsKey(msm)) return;
        int id = inverseActiveScreens.getInt(msm);
        sendPacket(new PacketSyncHandler(id, panel, key, true, buffer), player);
    }
}
