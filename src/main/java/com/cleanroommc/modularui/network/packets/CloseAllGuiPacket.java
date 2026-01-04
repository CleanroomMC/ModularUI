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

public class CloseAllGuiPacket implements IPacket {

    public CloseAllGuiPacket() {}

    @Override
    public void write(PacketBuffer buf) throws IOException {}

    @Override
    public void read(PacketBuffer buf) throws IOException {}

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularNetwork.CLIENT.closeAll(Minecraft.getMinecraft().player, false);
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        ModularNetwork.SERVER.closeAll(handler.player, false);
        return null;
    }
}
