package com.cleanroommc.modularui.api.future;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemHandlerHelper {

    public ItemHandlerHelper() {}

    @Nullable
    public static ItemStack insertItem(IItemHandler dest, @Nullable ItemStack stack, boolean simulate) {
        if (dest != null && stack != null) {
            for (int i = 0; i < dest.getSlots(); ++i) {
                stack = dest.insertItem(i, stack, simulate);
                if (stack == null) {
                    return null;
                }
            }

            return stack;
        } else {
            return stack;
        }
    }

    public static boolean canItemStacksStack(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a != null && a.isItemEqual(b) && a.hasTagCompound() == b.hasTagCompound()) {
            return (!a.hasTagCompound() || a.getTagCompound().equals(b.getTagCompound()));
        } else {
            return false;
        }
    }

    public static boolean canItemStacksStackRelaxed(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a != null && b != null && a.getItem() == b.getItem()) {
            if (!a.isStackable()) {
                return false;
            } else if (a.getHasSubtypes() && a.getItemDamage() != b.getItemDamage()) {
                return false;
            } else if (a.hasTagCompound() != b.hasTagCompound()) {
                return false;
            } else {
                return (!a.hasTagCompound() || a.getTagCompound().equals(b.getTagCompound()));
            }
        } else {
            return false;
        }
    }

    @Nullable
    public static ItemStack copyStackWithSize(@Nullable ItemStack itemStack, int size) {
        if (size == 0) {
            return null;
        } else {
            ItemStack copy = itemStack.copy();
            copy.stackSize = size;
            return copy;
        }
    }

    @Nullable
    public static ItemStack insertItemStacked(IItemHandler inventory, @Nullable ItemStack stack, boolean simulate) {
        if (inventory != null && stack != null) {
            if (!stack.isStackable()) {
                return insertItem(inventory, stack, simulate);
            } else {
                int sizeInventory = inventory.getSlots();

                int i;
                for (i = 0; i < sizeInventory; ++i) {
                    ItemStack slot = inventory.getStackInSlot(i);
                    if (canItemStacksStackRelaxed(slot, stack)) {
                        stack = inventory.insertItem(i, stack, simulate);
                        if (stack == null) {
                            break;
                        }
                    }
                }

                if (stack != null) {
                    for (i = 0; i < sizeInventory; ++i) {
                        if (inventory.getStackInSlot(i) == null) {
                            stack = inventory.insertItem(i, stack, simulate);
                            if (stack == null) {
                                break;
                            }
                        }
                    }
                }

                return stack;
            }
        } else {
            return stack;
        }
    }

    public static void giveItemToPlayer(EntityPlayer player, @Nullable ItemStack stack) {
        giveItemToPlayer(player, stack, -1);
    }

    public static void giveItemToPlayer(EntityPlayer player, @Nullable ItemStack stack, int preferredSlot) {
        if (stack != null) {
            IItemHandler inventory = new PlayerMainInvWrapper(player.inventory);
            World world = player.worldObj;
            ItemStack remainder = stack;
            if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
                remainder = inventory.insertItem(preferredSlot, stack, false);
            }

            if (remainder != null) {
                remainder = insertItemStacked(inventory, remainder, false);
            }

            if (remainder == null || remainder.stackSize != stack.stackSize) {
                world.playSoundAtEntity(
                        player,
                        "random.pop",
                        0.2F,
                        ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            if (remainder != null && !world.isRemote) {
                EntityItem entityitem = new EntityItem(world, player.posX, player.posY + 0.5D, player.posZ, remainder);
                entityitem.delayBeforeCanPickup = 40;
                entityitem.motionX = 0.0D;
                entityitem.motionZ = 0.0D;
                world.spawnEntityInWorld(entityitem);
            }
        }
    }

    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
        if (inv == null) {
            return 0;
        } else {
            int itemsFound = 0;
            float proportion = 0.0F;

            for (int j = 0; j < inv.getSlots(); ++j) {
                ItemStack itemstack = inv.getStackInSlot(j);
                if (itemstack != null) {
                    proportion += (float) itemstack.stackSize
                            / (float) Math.min(inv.getSlotLimit(j), itemstack.getMaxStackSize());
                    ++itemsFound;
                }
            }

            proportion /= (float) inv.getSlots();
            return MathHelper.floor_float(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }
}
