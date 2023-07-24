package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A slot group is a group of slots that can be sorted (via Inventory BogoSorter)
 * and be shift clicked into. The slot group must exist on server and client side.
 * Slot groups must be registered via
 * {@link com.cleanroommc.modularui.value.sync.GuiSyncManager#registerSlotGroup(String, int, boolean)}
 * or overloads of the method.
 */
public class SlotGroup {

    public static final int PLAYER_INVENTORY_PRIO = 0;
    public static final int STORAGE_SLOT_PRIO = 100;

    private final String name;
    private final List<Slot> slots = new ArrayList<>();
    private final int rowSize;
    private final int shiftClickPriority;
    private final boolean allowShiftTransfer;
    private boolean allowSorting = true;

    /**
     * Creates a slot group.
     *
     * @param name               the name of the group
     * @param rowSize            how many slots fit into a row in this group (assumes rectangular shape)
     * @param shiftClickPriority determines in which group a shift clicked item should be inserted first
     * @param allowShiftTransfer true if items can be shift clicked into this group
     */
    public SlotGroup(String name, int rowSize, int shiftClickPriority, boolean allowShiftTransfer) {
        this.name = name;
        this.rowSize = rowSize;
        this.shiftClickPriority = shiftClickPriority;
        this.allowShiftTransfer = allowShiftTransfer;
    }

    /**
     * Is automatically called if setup correctly.
     */
    @ApiStatus.Internal
    public void addSlot(Slot slot) {
        this.slots.add(slot);
    }

    public int getShiftClickPriority() {
        return this.shiftClickPriority;
    }

    public List<Slot> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    public int getRowSize() {
        return this.rowSize;
    }

    public String getName() {
        return this.name;
    }

    public boolean allowShiftTransfer() {
        return this.allowShiftTransfer;
    }

    public boolean isAllowSorting() {
        return this.allowSorting;
    }

    public SlotGroup setAllowSorting(boolean allowSorting) {
        this.allowSorting = allowSorting;
        return this;
    }
}
