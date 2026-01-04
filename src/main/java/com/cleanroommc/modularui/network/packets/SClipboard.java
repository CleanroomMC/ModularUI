package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SClipboard implements IPacket {

    public static void copyToClipboard(EntityPlayer player, String s) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            NetworkHandler.sendToPlayer(new SClipboard(s), (EntityPlayerMP) player);
            return;
        }
        GuiScreen.setClipboardString(s);
    }

    private String s;

    public SClipboard() {
    }

    public SClipboard(String s) {
        this.s = s;
    }

    @Override
    public void write(PacketBuffer buf) {
        NetworkUtils.writeStringSafe(buf, this.s);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.s = NetworkUtils.readStringSafe(buf);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        GuiScreen.setClipboardString(this.s);
        return null;
    }
}
