package com.cleanroommc.modularui.factory.inventory;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import baubles.api.BaublesApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class InventoryTypes {

    @ApiStatus.Internal
    public static void init() {}

    public static final InventoryType PLAYER = new Inventory("player") {
        @Override
        public IInventory getInventory(EntityPlayer player) {
            return player.inventory;
        }
    };

    public static final InventoryType BAUBLES = new ItemHandler("baubles") {

        @Override
        public boolean isActive() {
            return ModularUI.Mods.BAUBLES.isLoaded();
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

    public static @Nullable SlotFindResult findFirstStackable(EntityPlayer player, ItemStack stack) {
        for (InventoryType type : getAll()) {
            int i = type.findFirstStackable(player, stack);
            if (i >= 0) return new SlotFindResult(type, i);
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

    public static class SlotFindResult {
        public final InventoryType type;
        public final int slot;

        public SlotFindResult(InventoryType type, int slot) {
            this.type = type;
            this.slot = slot;
        }
    }
}
