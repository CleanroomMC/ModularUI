package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class SlotGroup extends MultiChildWidget {

    public static SlotGroup playerInventoryGroup(EntityPlayer player, Pos2d pos) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();
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
}
