package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.widget.Interactable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.network.PacketBuffer;

public class ClickData {

    public final int mouseButton;
    //public final boolean doubleClick;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public ClickData(int mouseButton, boolean shift, boolean ctrl, boolean alt) {
        this.mouseButton = mouseButton;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }

    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(mouseButton);
        byte data = 0;
        if (shift) data |= 1;
        if (ctrl) data |= 2;
        if (alt) data |= 4;
        buffer.writeByte(data);
    }

    public static ClickData readPacket(PacketBuffer buffer) {
        int button = buffer.readVarIntFromBuffer();
        byte data = buffer.readByte();
        return new ClickData(button, (data & 1) != 0, (data & 2) != 0, (data & 4) != 0);
    }

    @SideOnly(Side.CLIENT)
    public static ClickData create(int mouse) {
        return new ClickData(mouse, Interactable.hasShiftDown(), Interactable.hasControlDown(), Interactable.hasAltDown());
    }
}
