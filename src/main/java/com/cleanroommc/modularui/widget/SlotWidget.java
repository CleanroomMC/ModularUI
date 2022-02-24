package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.inventory.Slot;
import org.apache.commons.lang3.ArrayUtils;

public class SlotWidget extends Widget implements IVanillaSlot, IWidgetDrawable, Interactable {

    // TODO make my own custom slot widget that does not wrap fucking vanilla slot

    public static final Size TEXTURE_SIZE = new Size(18, 18);
    public static final Size ACTUAL_SLOT_SIZE = new Size(16, 16);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/item");
    private IDrawable[] textures = {TEXTURE};
    private Size textureSize = TEXTURE_SIZE;

    private final BaseSlot slot;

    public SlotWidget(BaseSlot slot) {
        setSize(ACTUAL_SLOT_SIZE);
        this.slot = slot;
    }

    public SlotWidget setTextures(IDrawable... drawables) {
        this.textures = drawables;
        return this;
    }

    public SlotWidget addTextures(IDrawable... drawables) {
        this.textures = ArrayUtils.addAll(textures, drawables);
        return this;
    }

    @Override
    public Slot getMcSlot() {
        return slot;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        // draw background
        Pos2d texturePos = new Pos2d(-1, -1);
        for (IDrawable drawable : textures) {
            drawable.draw(texturePos, textureSize, partialTicks);
        }
    }

    @Override
    public void drawInForeground(float partialTicks) {
        // draw item??
    }

    @Override
    public void onRebuildPre() {

    }

    @Override
    public void onRebuildPost() {
        Pos2d pos = getAbsolutePos().subtract(getGui().getPos());
        slot.xPos = (int) (pos.x + 0.5);
        slot.yPos = (int) (pos.y + 0.5);
    }

    @Override
    public SlotWidget setPos(Pos2d relativePos) {
        return (SlotWidget) super.setPos(new Pos2d((int) relativePos.x, (int) relativePos.y));
    }

    @Override
    public SlotWidget setSize(Size size) {
        return (SlotWidget) super.setSize(size);
    }

    public SlotWidget setTextureSize(Size textureSize) {
        this.textureSize = textureSize;
        return this;
    }
}
