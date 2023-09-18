package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.widget.Interactable;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeyboardData {

    public final Side side;
    public final char character;
    public final int keycode;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public KeyboardData(Side side, char character, int keycode, boolean shift, boolean ctrl, boolean alt) {
        this.side = side;
        this.character = character;
        this.keycode = keycode;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }

    public boolean isClient() {
        return this.side.isClient();
    }

    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeChar(this.character);
        buffer.writeVarInt(this.keycode);
        byte data = 0;
        if (this.shift) data |= 1;
        if (this.ctrl) data |= 2;
        if (this.alt) data |= 4;
        buffer.writeByte(data);
    }

    public static KeyboardData readPacket(PacketBuffer buffer) {
        char character = buffer.readChar();
        int keycode = buffer.readVarInt();
        byte data = buffer.readByte();
        return new KeyboardData(Side.SERVER, character, keycode, (data & 1) != 0, (data & 2) != 0, (data & 4) != 0);
    }

    @SideOnly(Side.CLIENT)
    public static KeyboardData create(char character, int keycode) {
        return new KeyboardData(Side.CLIENT, character, keycode, Interactable.hasShiftDown(), Interactable.hasControlDown(), Interactable.hasAltDown());
    }
}
