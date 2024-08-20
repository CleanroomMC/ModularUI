package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.packets.*;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModularUI.MOD_ID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int packetId = 0;

    public static void init() {
        registerS2C(SClipboard.class);
        registerS2C(PacketSyncHandler.class);
        registerC2S(PacketSyncHandler.class);
        registerC2S(SyncConfig.class);
        registerS2C(OpenGuiPacket.class);
        //registerC2S(OpenGuiHandshake.class);
    }

    private static void registerC2S(Class<? extends IPacket> clazz) {
        CHANNEL.registerMessage(packetId++, C2SHandler, clazz, , NetworkDirection.);
    }

    private static void registerS2C(Class<? extends IPacket> clazz) {
        CHANNEL.registerMessage(S2CHandler, clazz, packetId++, Side.CLIENT);
    }

    public static void sendToServer(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToWorld(IPacket packet, Level world) {
        CHANNEL.sendToDimension(packet, world.provider.getDimension());
    }

    public static void sendToPlayer(IPacket packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player);
    }

    final static IMessageHandler<IPacket, IPacket> S2CHandler = (message, ctx) -> {
        NetHandlerPlayClient handler = ctx.getClientHandler();
        IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
        if (threadListener.isCallingFromMinecraftThread()) {
            return message.executeClient(handler);
        } else {
            threadListener.addScheduledTask(() -> message.executeClient(handler));
        }
        return null;
    };
    final static IMessageHandler<IPacket, IPacket> C2SHandler = (message, ctx) -> {
        NetHandlerPlayServer handler = ctx.getServerHandler();
        IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
        if (threadListener.isCallingFromMinecraftThread()) {
            return message.executeServer(handler);
        } else {
            threadListener.addScheduledTask(() -> message.executeServer(handler));
        }
        return null;
    };
}
