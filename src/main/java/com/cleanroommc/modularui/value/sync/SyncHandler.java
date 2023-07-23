package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.network.packets.PacketSyncHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for handling syncing of widgets.
 * A sync handler must exist on client and server.
 * It must be configured exactly the same to avoid issues.
 */
public abstract class SyncHandler {

    private GuiSyncHandler syncHandler;
    private String key;

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void init(String key, GuiSyncHandler syncHandler) {
        this.key = key;
        this.syncHandler = syncHandler;
    }

    /**
     * Syncs a custom packet to the client
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    public final void syncToClient(int id, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        bufferConsumer.accept(buffer);
        sendToClient(buffer, this);
    }

    /**
     * Syncs a custom packet to the server
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    @SideOnly(Side.CLIENT)
    public final void syncToServer(int id, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        bufferConsumer.accept(buffer);
        sendToServer(buffer, this);
    }

    /**
     * Sync a custom packet to the other side.
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    public final void sync(int id, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        bufferConsumer.accept(buffer);
        if (NetworkUtils.isClient(getSyncHandler().getPlayer())) {
            sendToServer(buffer, this);
        } else {
            sendToClient(buffer, this);
        }
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
    public void detectAndSendChanges(boolean init) {
    }

    /**
     * @return the key that belongs to this sync handler
     */
    public final String getKey() {
        return this.key;
    }

    /**
     * @return is this sync handler has been initialised yet
     */
    public final boolean isValid() {
        return this.key != null;
    }

    /**
     * @return the sync handler manager handling this sync handler
     */
    public GuiSyncHandler getSyncHandler() {
        if (!isValid()) {
            throw new IllegalStateException("Sync handler is not yet initialised!");
        }
        return this.syncHandler;
    }

    public static void sendToClient(PacketBuffer buffer, SyncHandler syncHandler) {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(syncHandler);
        if (!syncHandler.isValid()) {
            throw new IllegalStateException();
        }
        NetworkHandler.sendToPlayer(new PacketSyncHandler(syncHandler.getKey(), buffer), (EntityPlayerMP) syncHandler.syncHandler.getPlayer());
    }

    public static void sendToServer(PacketBuffer buffer, SyncHandler syncHandler) {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(syncHandler);
        if (!syncHandler.isValid()) {
            throw new IllegalStateException();
        }
        NetworkHandler.sendToServer(new PacketSyncHandler(syncHandler.getKey(), buffer));
    }
}
