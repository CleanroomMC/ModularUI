package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class SlotWidget extends Widget implements IVanillaSlot, Interactable, ISyncedWidget {

    public static final Size SIZE = new Size(18, 18);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/item");

    private final BaseSlot slot;

    public SlotWidget(BaseSlot slot) {
        this.slot = slot;
    }

    @Override
    public void onInit() {
        getContext().getContainer().addSlotToContainer(slot);
        if (getDrawable() == null) {
            setBackground(TEXTURE);
        }
    }

    @Override
    public Slot getMcSlot() {
        return slot;
    }

    @Nullable
    @Override
    protected Size determineSize() {
        return SIZE;
    }

    @Override
    public void onRebuild() {
        Pos2d pos = getAbsolutePos().subtract(getWindow().getPos()).add(1, 1);
        if (slot.xPos != pos.x || slot.yPos != pos.y) {
            slot.xPos = pos.x;
            slot.yPos = pos.y;
            // widgets are only rebuild on client and mc requires the slot pos on server
            syncToServer(1, buffer -> {
                buffer.writeVarInt(pos.x);
                buffer.writeVarInt(pos.y);
            });
        }
    }

    @Override
    public SlotWidget setPos(Pos2d relativePos) {
        return (SlotWidget) super.setPos(relativePos);
    }

    @Override
    public SlotWidget setSize(Size size) {
        return (SlotWidget) super.setSize(size);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        slot.setEnabled(enabled);
    }

    @Override
    public void readServerData(int id, PacketBuffer buf) {

    }

    @Override
    public void readClientData(int id, PacketBuffer buf) {
        if (id == 1) {
            slot.xPos = buf.readVarInt();
            slot.yPos = buf.readVarInt();
        }
    }
}
