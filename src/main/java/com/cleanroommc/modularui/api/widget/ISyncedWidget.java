package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.screen.ModularWindow;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Implement this to let them synchronize data between server and client
 * see also: {@link Interactable}
 */
public interface ISyncedWidget {

    @SideOnly(Side.CLIENT)
    void readOnClient(int id, PacketBuffer buf) throws IOException;

    void readOnServer(int id, PacketBuffer buf) throws IOException;

    /**
     * Called AT LEAST each tick on server. Use it to detect and sync changes
     *
     * @param init true if it is called the first time after init
     */
    default void detectAndSendChanges(boolean init) {
    }

    /**
     * Sends the written data to {@link #readOnServer(int, PacketBuffer)}
     *
     * @param id         helper to determine the type. Must not be -1!
     * @param bufBuilder data builder
     */
    @SideOnly(Side.CLIENT)
    default void syncToServer(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        if (id == -1) {
            throw new IllegalArgumentException("Id -1 is already reserved for syncing!");
        }
        getWindow().getContext().sendClientPacket(id, this, getWindow(), bufBuilder);
    }

    /**
     * Sends the written data to {@link #readOnClient(int, PacketBuffer)}
     *
     * @param id         helper to determine the type. Must not be -1!
     * @param bufBuilder data builder
     */
    default void syncToClient(int id, Consumer<PacketBuffer> bufBuilder) {
        if (!(this instanceof Widget)) {
            throw new IllegalStateException("Tried syncing a non Widget ISyncedWidget");
        }
        if (id == -1) {
            throw new IllegalArgumentException("Id -1 is already reserved for syncing!");
        }
        getWindow().getContext().sendServerPacket(id, this, getWindow(), bufBuilder);
    }

    ModularWindow getWindow();
}
