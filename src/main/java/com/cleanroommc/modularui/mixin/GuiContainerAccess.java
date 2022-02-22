package com.cleanroommc.modularui.mixin;

import com.cleanroommc.modularui.api.math.Pos2d;
import net.minecraft.inventory.Slot;

public interface GuiContainerAccess {

    default Slot getSlotAt(Pos2d pos) {
        return getSlotAt(pos.x, pos.y);
    }

    Slot getSlotAt(float x, float y);

    boolean isOverSlot(Slot slot, float x, float y);
}
