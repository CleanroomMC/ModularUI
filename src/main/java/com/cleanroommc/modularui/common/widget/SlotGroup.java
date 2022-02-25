package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class SlotGroup extends MultiChildWidget {

    public static SlotGroup playerInventoryGroup(EntityPlayer player, Pos2d pos) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();
        slotGroup.setSize(new Size(18 * 9, 18 + 58));
        slotGroup.setPos(pos);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotGroup.addSlot(new SlotWidget(new BaseSlot(wrapper, col + (row + 1) * 9))
                        .setPos(new Pos2d(col * 18 + 1, row * 18 + 1)));
            }
        }

        for (int i = 0; i < 9; i++) {
            slotGroup.addSlot(new SlotWidget(new BaseSlot(wrapper, i))
                    .setPos(new Pos2d(i * 18 + 1, 58 + 1)));
        }
        return slotGroup;
    }

    public SlotGroup addSlot(SlotWidget slotWidget) {
        addChild(slotWidget);
        return this;
    }

    @Override
    public void onRebuildPre() {
        /*
         if (!getChildren().isEmpty()) {
            float x0 = Float.MAX_VALUE, x1 = 0, y0 = Float.MAX_VALUE, y1 = 0;
            for (Widget widget : getChildren()) {
                widget.determineArea(maxSize);
                x0 = Math.min(x0, widget.getPos().x);
                x1 = Math.max(x1, widget.getPos().x + widget.getSize().width);
                y0 = Math.min(y0, widget.getPos().y);
                y1 = Math.max(y1, widget.getPos().y + widget.getSize().height);
            }
            setSize(new Size(x1 - x0, y1 - y0));
        }
         */
    }
}
