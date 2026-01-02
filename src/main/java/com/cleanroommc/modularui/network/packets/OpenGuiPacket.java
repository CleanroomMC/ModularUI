package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class OpenGuiPacket<T extends GuiData> implements IPacket {

    private int networkId;
    private UIFactory<T> factory;
    private PacketBuffer data;

    public OpenGuiPacket() {}

    public OpenGuiPacket(int networkId, UIFactory<T> factory, PacketBuffer data) {
        this.networkId = networkId;
        this.factory = factory;
        this.data = data;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.networkId);
        buf.writeString(this.factory.getFactoryName());
        NetworkUtils.writeByteBuf(buf, this.data);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.networkId = buf.readVarInt();
        this.factory = (UIFactory<T>) GuiManager.getFactory(buf.readString(32));
        this.data = NetworkUtils.readPacketBuffer(buf);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        GuiManager.openFromClient(this.networkId, this.factory, this.data, Platform.getClientPlayer());
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        T guiData = this.factory.readGuiData(handler.player, this.data);
        GuiManager.open(this.factory, guiData, handler.player);
        return null;
    }
}
