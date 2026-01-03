package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class ModularNetwork {

    // These need to be separate instances, otherwise they would access the same maps in singleplayer.
    // You have to make sure you are choosing the logical side you are currently on otherwise you can mess things badly, since
    // there is no validation.
    public static final Client CLIENT = new Client();
    public static final Server SERVER = new Server();

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

        private Client() {
            super(true);
        }

        public void activate(int nid, ModularSyncManager msm) {
            activateInternal(nid, msm);
        }

        @Override
        void sendPacket(IPacket packet, EntityPlayer player) {
            NetworkHandler.sendToServer(packet);
        }

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

    public static final class Server extends ModularNetworkSide {

        private int nextId = -1;

        private Server() {
            super(false);
        }

        public int activate(ModularSyncManager msm) {
            if (++nextId > 100_000) nextId = 0;
            activateInternal(nextId, msm);
            return nextId;
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
