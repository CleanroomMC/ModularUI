package com.cleanroommc.modularui.common.internal.network;

import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

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
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof ModularGui) {
            ModularUIContext context = ((ModularGui) screen).getContext();
            context.readServerPacket(packet);
        }
    }
}
