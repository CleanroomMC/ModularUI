package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class SlotGroup {

    public static final int PLAYER_INVENTORY_PRIO = 0;
    public static final int STORAGE_SLOT_PRIO = 100;

    private final String name;
    private final List<Slot> slots = new ArrayList<>();
    private final int rowSize;
    private final int shiftClickPriority;
    private final boolean allowShiftTransfer;
    private boolean allowSorting = true;

    public SlotGroup(String name, int rowSize, int shiftClickPriority, boolean allowShiftTransfer) {
        this.name = name;
        this.rowSize = rowSize;
        this.shiftClickPriority = shiftClickPriority;
        this.allowShiftTransfer = allowShiftTransfer;
    }

    public void addSlot(Slot slot) {
        this.slots.add(slot);
    }

    public int getShiftClickPriority() {
        return shiftClickPriority;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getRowSize() {
        return rowSize;
    }

    public String getName() {
        return name;
    }

    public boolean allowShiftTransfer() {
        return allowShiftTransfer;
    }

    public boolean isAllowSorting() {
        return allowSorting;
    }

    public SlotGroup setAllowSorting(boolean allowSorting) {
        this.allowSorting = allowSorting;
        return this;
    }
}
