package com.cleanroommc.modularui.screen;

import net.minecraft.inventory.Slot;

public interface IClickableGuiContainer {

    void modularUI$setClickedSlot(Slot slot);

    Slot modularUI$getClickedSlot();
}
