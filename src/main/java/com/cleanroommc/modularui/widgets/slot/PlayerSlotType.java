package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

public enum PlayerSlotType {
    HOTBAR, MAIN_INVENTORY, OFFHAND, ARMOR;

    public static PlayerSlotType getPlayerSlotType(Slot slot) {
        int index = slot.getSlotIndex();
        if (index < 0 || index > 40) return null;
        if (slot instanceof SlotItemHandler slotitemhandler) {
            if (slotitemhandler.getItemHandler() instanceof PlayerMainInvWrapper) {
                return index < 9 ? HOTBAR : MAIN_INVENTORY;
            }
            if (slotitemhandler.getItemHandler() instanceof PlayerArmorInvWrapper) {
                return ARMOR;
            }
            if (slotitemhandler.getItemHandler() instanceof PlayerOffhandInvWrapper) {
                return OFFHAND;
            }
            if (!(slotitemhandler.getItemHandler() instanceof PlayerInvWrapper) &&
                    !(slotitemhandler.getItemHandler() instanceof InvWrapper invWrapper && invWrapper.getInv() instanceof InventoryPlayer)) {
                return null;
            }
        } else if (!(slot.inventory instanceof InventoryPlayer)) {
            return null;
        }
        if (index < 9) return HOTBAR;
        if (index < 36) return MAIN_INVENTORY;
        if (index < 40) return ARMOR;
        return OFFHAND;
    }
}
