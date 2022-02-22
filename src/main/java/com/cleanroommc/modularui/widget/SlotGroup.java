package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.slot.BaseSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class SlotGroup extends MultiChildWidget {

    public static SlotGroup playerInventoryGroup(EntityPlayer player, Pos2d pos) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotGroup.addSlot(new SlotWidget(new BaseSlot(wrapper, col + (row + 1) * 9))
                        .setPos(pos.add(col * 18, row * 18)));
            }
        }

        for (int i = 0; i < 9; i++) {
            slotGroup.addSlot(new SlotWidget(new BaseSlot(wrapper, i))
                    .setPos(pos.add(i * 18, 58)));
        }
        return slotGroup;
    }

    public SlotGroup addSlot(SlotWidget slotWidget) {
        addChild(slotWidget);
        return this;
    }

    @Override
    protected void determineArea() {
        if (!getChildren().isEmpty()) {
            float x0 = Float.MAX_VALUE, x1 = 0, y0 = Float.MAX_VALUE, y1 = 0;
            for (Widget widget : getChildren()) {
                x0 = Math.min(x0, widget.getPos().x);
                x1 = Math.max(x1, widget.getPos().x + widget.getSize().width);
                y0 = Math.min(y0, widget.getPos().y);
                y1 = Math.max(y1, widget.getPos().y + widget.getSize().height);
            }
            if (getAlignment() == null) {
                setPos(new Pos2d(x0, y0));
                setSize(new Size(x1 - x0, y1 - y0));
            }
        }
    }
}
