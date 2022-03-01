package com.cleanroommc.modularui.common.internal.network;

import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.function.Consumer;

public class CWidgetUpdate implements Packet<NetHandlerPlayServer> {

    public Consumer<PacketBuffer> bufferConsumer;
    public PacketBuffer packet;

    public CWidgetUpdate(Consumer<PacketBuffer> bufferConsumer) {
        this.bufferConsumer = bufferConsumer;
    }

    public CWidgetUpdate() {
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.packet = buf;
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        bufferConsumer.accept(buf);
    }

    @Override
    public void processPacket(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container instanceof ModularUIContainer) {
            ModularUIContext context = ((ModularUIContainer) container).getContext();
            context.readClientPacket(packet);
        }
    }
}