package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;

public class SlotGroup extends ParentWidget<SlotGroup> {

    public static SlotGroup playerInventory() {
        SlotGroup slotGroup = new SlotGroup();
        slotGroup.flex().startDefaultMode()
                .width(9 * 18).height(4 * 18 + 5).left(0.5f).bottom(7)
                .endDefaultMode();
        slotGroup.debugName("player_inventory");
        String key = "player";
        for (int i = 0; i < 9; i++) {
            slotGroup.child(new ItemSlot()
                    .setSynced(key, i)
                    .pos(i * 18, 3 * 18 + 5)
                    .debugName("slot_" + i));
        }
        for (int i = 0; i < 27; i++) {
            slotGroup.child(new ItemSlot()
                    .setSynced(key, i + 9)
                    .pos(i % 9 * 18, i / 9 * 18)
                    .debugName("slot_" + (i + 9)));
        }
        return slotGroup;
    }

    private String slotsKeyName;

    public void setSlotsSynced(String name) {
        this.slotsKeyName = name;
        int i = 0;
        for (IWidget widget : getChildren()) {
            if (widget instanceof Widget) {
                ((Widget<?>) widget).setSynced(name, i);
            }
            i++;
        }
    }

    public static class Builder {

    }
}
