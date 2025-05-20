package com.cleanroommc.modularui.factory.inventory;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A way of finding and setting an item in an inventory, that is owned by a player.
 * This includes the normal player inventory with all its slots including main hand, off-hand and armor slots.
 * It can also be used for bauble inventory if baubles is loaded.
 * An inventory type has an id assigned used for syncing. It is crucial, types are created in the same order on client and server.
 * Currently, the amount of types is limited to 16, but I don't think we will ever fill that.
 * @see InventoryTypes InventoryTypes for default implementations
 */
public abstract class InventoryType {

    private static final Map<String, InventoryType> inventoryTypes = new Object2ObjectOpenHashMap<>();

    private final String id;

    public InventoryType(String id) {
        this.id = id;
        if (isActive()) {
            inventoryTypes.put(id, this);
        }
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return true;
    }

    public abstract ItemStack getStackInSlot(EntityPlayer player, int index);

    public abstract void setStackInSlot(EntityPlayer player, int index, ItemStack stack);

    public abstract int getSlotCount(EntityPlayer player);

    public int findFirstStackable(EntityPlayer player, ItemStack stack) {
        for (int i = 0, n = getSlotCount(player); i < n; ++i) {
            ItemStack stackInSlot = getStackInSlot(player, i);
            if (Platform.isStackEmpty(stackInSlot)) {
                if (Platform.isStackEmpty(stack)) {
                    return i;
                }
                continue;
            }
            if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
                return i;
            }
        }
        return -1;
    }

    public boolean visitAllStackable(EntityPlayer player, ItemStack stack, InventoryVisitor visitor) {
        for (int i = 0, n = getSlotCount(player); i < n; ++i) {
            ItemStack stackInSlot = getStackInSlot(player, i);
            if (Platform.isStackEmpty(stackInSlot)) {
                if (Platform.isStackEmpty(stack)) {
                    if (visitor.visit(this, i, stackInSlot)) {
                        return true;
                    }
                }
                continue;
            }
            if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
                if (visitor.visit(this, i, stackInSlot)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean visitAll(EntityPlayer player, InventoryVisitor visitor) {
        for (int i = 0, n = getSlotCount(player); i < n; ++i) {
            ItemStack stackInSlot = getStackInSlot(player, i);
            if (visitor.visit(this, i, stackInSlot)) {
                return true;
            }
        }
        return false;
    }

    public void write(PacketBuffer buf) {
        NetworkUtils.writeStringSafe(buf, id);
    }

    public static InventoryType read(PacketBuffer buf) {
        return getFromId(NetworkUtils.readStringSafe(buf));
    }

    public static InventoryType getFromId(String id) {
        return inventoryTypes.get(id);
    }

    public static Collection<InventoryType> getAll() {
        return Collections.unmodifiableCollection(inventoryTypes.values());
    }

    public static Pair<InventoryType, Integer> findFirstStackableInAll(EntityPlayer player, ItemStack stack) {
        for (InventoryType type : getAll()) {
            int i = type.findFirstStackable(player, stack);
            if (i >= 0) return Pair.of(type, i);
        }
        return null;
    }

    public static void visitAllStackableInAll(EntityPlayer player, ItemStack stack, InventoryVisitor visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAllStackable(player, stack, visitor)) {
                return;
            }
        }
    }

    public static void visitAllInAll(EntityPlayer player, InventoryVisitor visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAll(player, visitor)) {
                return;
            }
        }
    }
}
