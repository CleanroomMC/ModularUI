package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.UIFactory;
import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class OpenGuiPacket<T extends GuiData> implements IPacket {

    private int windowId;
    private UIFactory<T> factory;
    private PacketBuffer data;

    public OpenGuiPacket() {
    }

    public OpenGuiPacket(int windowId, UIFactory<T> factory, PacketBuffer data) {
        this.windowId = windowId;
        this.factory = factory;
        this.data = data;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.windowId);
        buf.writeString(this.factory.getFactoryName());
        NetworkUtils.writeByteBuf(buf, this.data);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.windowId = buf.readVarInt();
        this.factory = (UIFactory<T>) GuiManager.getFactory(buf.readString(32));
        this.data = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        GuiManager.open(this.windowId, this.factory, this.data, Minecraft.getMinecraft().player);
        return null;
    }
}
