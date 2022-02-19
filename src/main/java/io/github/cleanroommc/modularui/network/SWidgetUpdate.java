package io.github.cleanroommc.modularui.network;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;
import java.util.function.Consumer;

public class SWidgetUpdate implements Packet<INetHandlerPlayClient> {

    public Consumer<PacketBuffer> bufferConsumer;
    public PacketBuffer packet;

    public SWidgetUpdate(Consumer<PacketBuffer> bufferConsumer) {
        this.bufferConsumer = bufferConsumer;
    }

    public SWidgetUpdate() {
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
    public void processPacket(INetHandlerPlayClient handler) {

    }
}
