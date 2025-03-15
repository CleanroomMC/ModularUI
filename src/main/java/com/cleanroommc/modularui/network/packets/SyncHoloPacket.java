package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.factory.HoloGuiManager;
import com.cleanroommc.modularui.network.IPacket;

import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SyncHoloPacket implements IPacket {

    String panel;

    public SyncHoloPacket() {}

    public SyncHoloPacket(String panel) {
        this.panel = panel;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        NetworkUtils.writeStringSafe(buf, this.panel);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.panel = NetworkUtils.readStringSafe(buf);
    }

    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        HoloGuiManager.reposition(this.panel, Minecraft.getMinecraft().player);
        return null;
    }
}
