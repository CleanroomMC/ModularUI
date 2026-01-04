package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.ModularNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CloseGuiPacket implements IPacket {

    private int networkId;
    private boolean dispose;

    public CloseGuiPacket() {}

    public CloseGuiPacket(int networkId, boolean dispose) {
        this.networkId = networkId;
        this.dispose = dispose;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.networkId);
        buf.writeBoolean(this.dispose);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.networkId = buf.readVarInt();
        this.dispose = buf.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularNetwork.CLIENT.closeContainer(this.networkId, this.dispose, Minecraft.getMinecraft().player, false);
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        ModularNetwork.SERVER.closeContainer(this.networkId, this.dispose, handler.player, false);
        return null;
    }
}
