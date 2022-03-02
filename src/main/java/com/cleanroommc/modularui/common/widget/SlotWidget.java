package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.Interactable;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.drawable.IDrawable;
import com.cleanroommc.modularui.common.drawable.UITexture;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

public class SlotWidget extends Widget implements IVanillaSlot, IWidgetDrawable, Interactable, ISyncedWidget {

    public static final Size SIZE = new Size(18, 18);
    public static final UITexture TEXTURE = UITexture.fullImage("modularui", "gui/slot/item");
    private IDrawable[] textures = {TEXTURE};
    private Size textureSize = SIZE;

    private final BaseSlot slot;

    public SlotWidget(BaseSlot slot) {
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
    public void onInit() {
        getContext().getContainer().addSlotToContainer(slot);
    }

    @Override
    public Slot getMcSlot() {
        return slot;
    }

    @Override
    public void drawInBackground(float partialTicks) {
        // draw background
        for (IDrawable drawable : textures) {
            drawable.draw(Pos2d.ZERO, textureSize, partialTicks);
        }
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

    public SlotWidget setTextureSize(Size textureSize) {
        this.textureSize = textureSize;
        return this;
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
