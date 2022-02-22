package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.slot.BaseSlot;
import net.minecraft.inventory.Slot;

public class SlotWidget extends Widget implements IVanillaSlot, IWidgetDrawable, Interactable {

    public static final Size DEFAULT_SIZE = new Size(18, 18);

    private final BaseSlot slot;

    public SlotWidget(BaseSlot slot) {
        setSize(DEFAULT_SIZE);
        this.slot = slot;
    }

    @Override
    public Slot getMcSlot() {
        return slot;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        // draw background
    }

    @Override
    public void drawInForeground(float partialTicks) {
        // draw item??
    }

    @Override
    public void onRebuild() {
        slot.xPos = (int) getAbsolutePos().x;
        slot.yPos = (int) getAbsolutePos().y;
    }
}
