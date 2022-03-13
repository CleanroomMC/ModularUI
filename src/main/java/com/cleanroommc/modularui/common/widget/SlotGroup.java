package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class SlotGroup extends MultiChildWidget {

    public static final int PLAYER_INVENTORY_HEIGHT = 76;

    public static SlotGroup playerInventoryGroup(EntityPlayer player, Pos2d pos) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();
        slotGroup.setPos(pos);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotGroup.addSlot(new SlotWidget(new BaseSlot(wrapper, col + (row + 1) * 9))
                        .setPos(new Pos2d(col * 18, row * 18)));
            }
        }

        for (int i = 0; i < 9; i++) {
            slotGroup.addSlot(new SlotWidget(new BaseSlot(wrapper, i))
                    .setPos(new Pos2d(i * 18, 58)));
        }
        return slotGroup;
    }

    public static SlotGroup ofItemHandler(IItemHandler itemHandler, int slotsWidth, int shiftClickPriority) {
        return ofItemHandler(itemHandler, slotsWidth, shiftClickPriority, 0, itemHandler.getSlots() - 1);
    }

    public static SlotGroup ofItemHandler(IItemHandler itemHandler, int slotsWidth, int shiftClickPriority, int startFromSlot, int endAtSlot) {
        SlotGroup slotGroup = new SlotGroup();
        if (itemHandler.getSlots() >= endAtSlot) {
            endAtSlot = itemHandler.getSlots() - 1;
        }
        startFromSlot = Math.max(startFromSlot, 0);
        if (startFromSlot > endAtSlot) {
            return slotGroup;
        }
        slotsWidth = Math.max(slotsWidth, 1);
        int x = 0, y = 0;
        for (int i = startFromSlot; i < endAtSlot + 1; i++) {
            slotGroup.addSlot(new SlotWidget(new BaseSlot(itemHandler, i).setShiftClickPriority(shiftClickPriority))
                    .setPos(new Pos2d(x * 18, y * 18)));
            if (++x == slotsWidth) {
                x = 0;
                y++;
            }
        }
        return slotGroup;
    }

    public SlotGroup addSlot(SlotWidget slotWidget) {
        addChild(slotWidget);
        return this;
    }
}
