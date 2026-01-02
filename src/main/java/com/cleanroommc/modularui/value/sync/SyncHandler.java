package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.network.ModularNetwork;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * Base class for handling syncing of widgets.
 * A sync handler must exist on client and server.
 * It must be configured exactly the same to avoid issues.
 */
public abstract class SyncHandler implements ISyncOrValue {

    private PanelSyncManager syncManager;
    private String key;

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void init(String key, PanelSyncManager syncManager) {
        this.key = key;
        this.syncManager = syncManager;
    }

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void dispose() {
        this.key = null;
        this.syncManager = null;
    }

    /**
     * Syncs a custom packet to the client
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    public final void syncToClient(int id, @NotNull IPacketWriter bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        try {
            bufferConsumer.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendToClient(getSyncManager().getPanelName(), buffer, this);
    }

    /**
     * Syncs a custom packet to the server
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    @SideOnly(Side.CLIENT)
    public final void syncToServer(int id, @NotNull IPacketWriter bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        try {
            bufferConsumer.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendToServer(getSyncManager().getPanelName(), buffer, this);
    }

    /**
     * Sync a custom packet to the other side.
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    public final void sync(int id, @NotNull IPacketWriter bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        try {
            bufferConsumer.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        send(ModularNetwork.get(getSyncManager().isClient()), getSyncManager().getPanelName(), buffer, this);
    }

    /**
     * Sends an empty packet to the client with an id.
     *
     * @param id identifier
     */
    public final void syncToClient(int id) {
        syncToClient(id, buf -> {});
    }

    /**
     * Sends an empty packet to the server with an id.
     *
     * @param id identifier
     */
    public final void syncToServer(int id) {
        syncToServer(id, buf -> {});
    }

    /**
     * Sends an empty packet to the other side with an id.
     *
     * @param id identifier
     */
    public final void sync(int id) {
        sync(id, buf -> {});
    }

    /**
     * Called when this sync handler receives a packet on client.
     *
     * @param id  an internal denominator to identify this package
     * @param buf package
     * @throws IOException package read error
     */
    @ApiStatus.OverrideOnly
    @SideOnly(Side.CLIENT)
    public abstract void readOnClient(int id, PacketBuffer buf) throws IOException;

    /**
     * Called when this sync handler receives a packet on server.
     *
     * @param id  an internal denominator to identify this package
     * @param buf package
     * @throws IOException package read error
     */
    @ApiStatus.OverrideOnly
    public abstract void readOnServer(int id, PacketBuffer buf) throws IOException;

    /**
     * Called at least every tick. Use it to compare a cached value to its original and sync it.
     * This is only called on the server side.
     *
     * @param init if this method is being called the first time.
     */
    public void detectAndSendChanges(boolean init) {}

    /**
     * @return the key that belongs to this sync handler
     */
    public final String getKey() {
        return this.key;
    }

    /**
     * @return is this sync handler has been initialized yet
     */
    public final boolean isValid() {
        return this.key != null && this.syncManager != null;
    }

    /**
     * @return the sync handler manager handling this sync handler
     */
    public PanelSyncManager getSyncManager() {
        if (!isValid()) {
            throw new IllegalStateException("Sync handler is not yet initialised!");
        }
        return this.syncManager;
    }

    public final boolean isRegistered() {
        return isValid() && this.syncManager.hasSyncHandler(this);
    }

    @Override
    public boolean isSyncHandler() {
        return true;
    }

    private static void send(ModularNetwork network, String panel, PacketBuffer buffer, SyncHandler syncHandler) {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(syncHandler);
        if (!syncHandler.isValid()) {
            throw new IllegalStateException("Not initialized sync handlers can't send packets!");
        }
        network.sendSyncHandlerPacket(panel, syncHandler, buffer, syncHandler.syncManager.getPlayer());
    }

    public static void sendToClient(String panel, PacketBuffer buffer, SyncHandler syncHandler) {
        send(ModularNetwork.SERVER, panel, buffer, syncHandler);
    }

    @SideOnly(Side.CLIENT)
    public static void sendToServer(String panel, PacketBuffer buffer, SyncHandler syncHandler) {
        send(ModularNetwork.CLIENT, panel, buffer, syncHandler);
    }
}
