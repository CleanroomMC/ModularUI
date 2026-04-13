package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.network.packets.PacketSyncHandler;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public abstract class ModularNetwork {

    // These need to be separate instances, otherwise they would access the same maps in singleplayer.
    // You have to make sure you are choosing the logical side you are currently on otherwise you can mess things badly, since
    // there is no validation.
    public static final Client CLIENT = new Client();
    public static final ServerManager SERVER = new ServerManager();

    public static ModularNetworkSide get(boolean client) {
        return client ? CLIENT : SERVER;
    }

    public static ModularNetworkSide get(Side side) {
        return side.isClient() ? CLIENT : SERVER;
    }

    public static ModularNetworkSide get(EntityPlayer player) {
        return get(NetworkUtils.isClient(player));
    }

    public static final class Client extends ModularNetworkSide {

        @Override
        public boolean isClient() {
            return true;
        }

        public void activate(int nid, ModularSyncManager msm) {
            activateInternal(nid, msm);
        }

        @SideOnly(Side.CLIENT)
        @Override
        void sendPacket(IPacket packet, EntityPlayer player) {
            NetworkHandler.sendToServer(packet);
        }

        @SideOnly(Side.CLIENT)
        @Override
        void closeContainer(EntityPlayer player) {
            // mimics EntityPlayerSP.closeScreenAndDropStack() but without closing the screen
            player.inventory.setItemStack(ItemStack.EMPTY);
            player.openContainer = player.inventoryContainer;
        }

        @SideOnly(Side.CLIENT)
        public void closeContainer(int networkId, boolean dispose, EntityPlayerSP player) {
            closeContainer(networkId, dispose, player, true);
        }

        @SideOnly(Side.CLIENT)
        public void closeAll() {
            closeAll(Minecraft.getMinecraft().player);
        }

        @SideOnly(Side.CLIENT)
        public void reopenSyncerOf(GuiScreen guiScreen) {
            if (guiScreen instanceof IMuiScreen ms && !ms.getScreen().isClientOnly()) {
                ModularSyncManager msm = ms.getScreen().getSyncManager();
                reopen(Minecraft.getMinecraft().player, msm, true);
            }
        }
    }

    public static final class ServerManager extends Server {

        private final Map<UUID, Server> playerHandlers = new Object2ObjectOpenHashMap<>();

        public Server get(EntityPlayer player) {
            return playerHandlers.computeIfAbsent(player.getUniqueID(), k -> new Server());
        }

        public int activate(EntityPlayer player, ModularSyncManager msm) {
            return get(player).activate(msm);
        }

        @Override
        public void onPlayerLeave(EntityPlayer player) {
            get(player).onPlayerLeave(player);
            this.playerHandlers.remove(player.getUniqueID());
        }

        @Override
        public void closeAll(EntityPlayer player) {
            get(player).closeAll(player);
        }

        @Override
        public void closeAll(EntityPlayer player, boolean sync) {
            get(player).closeAll(player, sync);
        }

        @Override
        public void receivePacket(EntityPlayer player, PacketSyncHandler packet) {
            get(player).receivePacket(player, packet);
        }

        @Override
        public void sendSyncHandlerPacket(String panel, SyncHandler syncHandler, PacketBuffer buffer, EntityPlayer player) {
            get(player).sendSyncHandlerPacket(panel, syncHandler, buffer, player);
        }

        @Override
        public void sendActionPacket(ModularSyncManager msm, String panel, String key, PacketBuffer buffer, EntityPlayer player) {
            get(player).sendActionPacket(msm, panel, key, buffer, player);
        }

        @Override
        public void closeContainer(int networkId, boolean dispose, EntityPlayer player, boolean sync) {
            get(player).closeContainer(networkId, dispose, player, sync);
        }

        @Override
        public void reopen(EntityPlayer player, int networkId, boolean sync) {
            get(player).reopen(player, networkId, sync);
        }

        @Override
        public void reopen(EntityPlayer player, ModularSyncManager msm, boolean sync) {
            get(player).reopen(player, msm, sync);
        }
    }

    @ApiStatus.NonExtendable
    public static class Server extends ModularNetworkSide {

        private int nextId = -1;

        protected int activate(ModularSyncManager msm) {
            if (++nextId > 100_000) nextId = 0;
            activateInternal(nextId, msm);
            return nextId;
        }

        @Override
        public boolean isClient() {
            return false;
        }

        @Override
        protected void sendPacket(IPacket packet, EntityPlayer player) {
            NetworkHandler.sendToPlayer(packet, (EntityPlayerMP) player);
        }

        @Override
        void closeContainer(EntityPlayer player) {
            ((EntityPlayerMP) player).closeContainer();
        }

        public void closeContainer(int networkId, boolean dispose, EntityPlayerMP player) {
            closeContainer(networkId, dispose, player, true);
        }
    }
}
