package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.widget.Interactable;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MouseData {

    public final Side side;
    public final int mouseButton;
    //public final boolean doubleClick;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public MouseData(Side side, int mouseButton, boolean shift, boolean ctrl, boolean alt) {
        this.side = side;
        this.mouseButton = mouseButton;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }

    public boolean isClient() {
        return this.side.isClient();
    }

    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeVarInt(this.mouseButton);
        byte data = 0;
        if (this.shift) data |= 1;
        if (this.ctrl) data |= 2;
        if (this.alt) data |= 4;
        buffer.writeByte(data);
    }

    public static MouseData readPacket(PacketBuffer buffer) {
        int button = buffer.readVarInt();
        byte data = buffer.readByte();
        return new MouseData(Side.SERVER, button, (data & 1) != 0, (data & 2) != 0, (data & 4) != 0);
    }

    @SideOnly(Side.CLIENT)
    public static MouseData create(int mouse) {
        return new MouseData(Side.CLIENT, mouse, Interactable.hasShiftDown(), Interactable.hasControlDown(), Interactable.hasAltDown());
    }
}
