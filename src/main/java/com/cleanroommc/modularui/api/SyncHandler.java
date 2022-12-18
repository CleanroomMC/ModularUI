package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.PacketSyncHandler;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.MapKey;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class SyncHandler {

    private GuiSyncHandler syncHandler;
    private MapKey key;

    @MustBeInvokedByOverriders
    public void init(MapKey key, GuiSyncHandler syncHandler) {
        this.key = key;
        this.syncHandler = syncHandler;
    }

    public void syncToClient(int id, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        bufferConsumer.accept(buffer);
        sendToClient(buffer, this);
    }

    @SideOnly(Side.CLIENT)
    public void syncToServer(int id, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(id);
        bufferConsumer.accept(buffer);
        sendToServer(buffer, this);
    }

    @SideOnly(Side.CLIENT)
    public abstract void readOnClient(int id, PacketBuffer buf) throws IOException;

    public abstract void readOnServer(int id, PacketBuffer buf) throws IOException;

    public void detectAndSendChanges(boolean init) {
    }

    public final MapKey getKey() {
        return key;
    }

    public final boolean isValid() {
        return key != null;
    }

    public GuiSyncHandler getSyncHandler() {
        if (!isValid()) {
            throw new IllegalStateException("Sync handler is not yet initialised!");
        }
        return syncHandler;
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
