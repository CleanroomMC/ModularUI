package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Basically a copy of {@link net.minecraft.inventory.SlotCrafting} for {@link ModularSlot}.
 */
public class ModularCraftingSlot extends ModularSlot {

    private InventoryCraftingWrapper craftMatrix;
    private IRecipe recipeUsed;
    private int amountCrafted;

    public ModularCraftingSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    public boolean isItemValid(@NotNull ItemStack stack) {
        return false;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    public @NotNull ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) {
            this.amountCrafted += Math.min(amount, this.getStack().getCount());
        }

        return super.decrStackSize(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    protected void onCrafting(@NotNull ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    protected void onSwapCraft(int p_190900_1_) {
        this.amountCrafted += p_190900_1_;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    protected void onCrafting(@NotNull ItemStack stack) {
        if (this.amountCrafted > 0) {
            stack.onCrafting(getPlayer().world, getPlayer(), this.amountCrafted);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(getPlayer(), stack, craftMatrix);
        }

        this.amountCrafted = 0;

        if (this.recipeUsed != null && !this.recipeUsed.isDynamic()) {
            getPlayer().unlockRecipes(Collections.singletonList(this.recipeUsed));
            this.recipeUsed = null;
        }
    }

    @Override
    public void onCraftShiftClick(EntityPlayer player, ItemStack stack) {
        if (!stack.isEmpty()) {
            player.dropItem(stack, false);
        }
    }

    @Override
    public @NotNull ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(thePlayer);
        NonNullList<ItemStack> nonnulllist = CraftingManager.getRemainingItems(this.craftMatrix, thePlayer.world);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
            ItemStack itemstack1 = nonnulllist.get(i);

            if (!itemstack.isEmpty()) {
                this.craftMatrix.decrStackSize(i, 1, false);
                itemstack = this.craftMatrix.getStackInSlot(i);
            }

            if (!itemstack1.isEmpty()) {
                if (itemstack.isEmpty()) {
                    this.craftMatrix.setSlot(i, itemstack1, false);
                } else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1)) {
                    itemstack1.grow(itemstack.getCount());
                    this.craftMatrix.setSlot(i, itemstack1, false);
                } else if (!thePlayer.inventory.addItemStackToInventory(itemstack1)) {
                    thePlayer.dropItem(itemstack1, false);
                }
            }
        }
        this.craftMatrix.notifyContainer();
        return stack;
    }

    public void updateResult(ItemStack stack) {
        putStack(stack);
        getSyncHandler().forceSyncItem();
    }

    public void setRecipeUsed(IRecipe recipeUsed) {
        this.recipeUsed = recipeUsed;
    }

    public void setCraftMatrix(InventoryCraftingWrapper craftMatrix) {
        this.craftMatrix = craftMatrix;
    }
}
