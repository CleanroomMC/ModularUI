package io.github.cleanroommc.modularui.network;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;
import java.util.function.Consumer;

public class CWidgetUpdate implements Packet<INetHandlerPlayServer> {

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
    public void processPacket(INetHandlerPlayServer handler) {

    }
}
