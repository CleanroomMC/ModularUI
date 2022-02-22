package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.slot.BaseSlot;
import net.minecraft.inventory.Slot;

public class SlotWidget extends Widget implements IVanillaSlot, IWidgetDrawable, Interactable {

    public static final Size DEFAULT_SIZE = new Size(18, 18);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/item");

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
        TEXTURE.draw(Pos2d.zero(), getSize(), partialTicks);
    }

    @Override
    public void drawInForeground(float partialTicks) {
        // draw item??
    }

    @Override
    public void onRebuild() {
        Pos2d pos = getAbsolutePos().subtract(getGui().getPos());
        slot.xPos = (int) pos.x;
        slot.yPos = (int) pos.y;
    }

    @Override
    public SlotWidget setPos(Pos2d relativePos) {
        return (SlotWidget) super.setPos(relativePos);
    }

    @Override
    public SlotWidget setSize(Size size) {
        return (SlotWidget) super.setSize(size);
    }
}
