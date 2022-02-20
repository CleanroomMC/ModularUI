package io.github.cleanroommc.modularui.api;

import io.github.cleanroommc.modularui.internal.ModularUI;
import io.github.cleanroommc.modularui.network.CWidgetUpdate;
import io.github.cleanroommc.modularui.network.SWidgetUpdate;
import io.github.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

/**
 * Implement this to let them synchronize data between server and client
 * see also: {@link Interactable}
 */
public interface ISyncedWidget {

    @SideOnly(Side.CLIENT)
    void readServerData(int id, PacketBuffer buf);

    void readClientData(int id, PacketBuffer buf);

    /**
     * Called each tick on server. Use it to detect and sync changes
     */
    default void onServerTick() {
    }

    /**
     * Sends the written data to {@link #readClientData(int, PacketBuffer)}
     *
     * @param id         helper to determine the type
     * @param bufBuilder data builder
     */
    @SideOnly(Side.CLIENT)
    default void syncToServer(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        if (!getGui().isInitialised()) {
            return;
        }
        Consumer<PacketBuffer> buffer = buf -> {
            buf.writeInt(getGui().getSyncId(this));
            bufBuilder.accept(buf);
        };
        CWidgetUpdate packet = new CWidgetUpdate(buffer);
        Minecraft.getMinecraft().player.connection.sendPacket(packet);
    }

    /**
     * Sends the written data to {@link #readServerData(int, PacketBuffer)}
     *
     * @param player     player to send data to. Usually just getGui().player
     * @param id         helper to determine the type
     * @param bufBuilder data builder
     */
    default void syncToClient(EntityPlayer player, int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        if (!(player instanceof EntityPlayerMP) || !getGui().isInitialised()) {
            return;
        }
        Consumer<PacketBuffer> buffer = buf -> {
            buf.writeInt(getGui().getSyncId(this));
            bufBuilder.accept(buf);
        };
        SWidgetUpdate packet = new SWidgetUpdate(buffer);
        ((EntityPlayerMP) player).connection.sendPacket(packet);
    }

    ModularUI getGui();
}
