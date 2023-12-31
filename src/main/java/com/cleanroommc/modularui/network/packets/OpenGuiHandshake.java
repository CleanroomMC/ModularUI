package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.screen.ModularContainer;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class OpenGuiHandshake implements IPacket {

    private int windowId;

    public OpenGuiHandshake() {
    }

    public OpenGuiHandshake(int windowId) {
        this.windowId = windowId;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.windowId);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.windowId = buf.readVarInt();
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        if (handler.player.openContainer instanceof ModularContainer container && container.windowId == this.windowId) {
            container.onHandshake();
        }
        return null;
    }
}
