package com.cleanroommc.modularui.factory.inventory;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import baubles.api.BaublesApi;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public class InventoryTypes {

    public static final InventoryType PLAYER = new Inventory("player") {
        @Override
        public IInventory getInventory(EntityPlayer player) {
            return player.inventory;
        }
    };

    public static final InventoryType BAUBLES = new ItemHandler("baubles") {

        @Override
        public boolean isActive() {
            return ModularUI.isBaubleLoaded();
        }

        @Override
        public IItemHandlerModifiable getInventory(EntityPlayer player) {
            if (isActive()) {
                return BaublesApi.getBaublesHandler(player);
            }
            throw new IllegalArgumentException("Tried to receive bauble item, but bauble is not loaded");
        }
    };

    public static Collection<InventoryType> getAll() {
        return InventoryType.getAll();
    }

    public static Pair<InventoryType, Integer> findFirstStackable(EntityPlayer player, ItemStack stack) {
        for (InventoryType type : getAll()) {
            int i = type.findFirstStackable(player, stack);
            if (i >= 0) return Pair.of(type, i);
        }
        return null;
    }

    public static void visitAllStackable(EntityPlayer player, ItemStack stack, InventoryVisitor visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAllStackable(player, stack, visitor)) {
                return;
            }
        }
    }

    public static void visitAll(EntityPlayer player, InventoryVisitor visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAll(player, visitor)) {
                return;
            }
        }
    }
}
