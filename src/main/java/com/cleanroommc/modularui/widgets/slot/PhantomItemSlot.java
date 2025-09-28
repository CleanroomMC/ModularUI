package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhantomItemSlot extends ItemSlot implements RecipeViewerGhostIngredientSlot<ItemStack> {

    private PhantomItemSlotSH syncHandler;

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addRecipeViewerGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, PhantomItemSlotSH.class);
        return this.syncHandler != null && super.isValidSyncHandler(syncHandler);
    }

    @Override
    protected void drawOverlay() {
        if (ModularUI.Mods.JEI.isLoaded() && (ModularUIJeiPlugin.draggingValidIngredient(this) || ModularUIJeiPlugin.hoveringOverIngredient(this))) {
            GlStateManager.colorMask(true, true, true, false);
            drawHighlight(getArea(), isHovering());
            GlStateManager.colorMask(true, true, true, true);
        } else {
            super.drawOverlay();
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        MouseData mouseData = MouseData.create(mouseButton);
        this.syncHandler.syncToServer(PhantomItemSlotSH.SYNC_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        this.syncHandler.syncToServer(PhantomItemSlotSH.SYNC_SCROLL, mouseData::writeToPacket);
        return true;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        // TODO custom drag impl
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        this.syncHandler.updateFromClient(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        if (areAncestorsEnabled() && this.syncHandler.isPhantom()) {
            if (ingredient instanceof EnchantmentData enchantmentData) {
                ingredient = Internal.getIngredientRegistry().getIngredientHelper(enchantmentData).getCheatItemStack(enchantmentData);
            }

            if (ingredient instanceof ItemStack itemStack) {
                return this.syncHandler.isItemValid(itemStack) ? itemStack : null;
            }
        }

        return null;
    }

    @Override
    @NotNull
    public PhantomItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    @Override
    public PhantomItemSlot slot(ModularSlot slot) {
        slot.slotNumber = -1;
        this.syncHandler = new PhantomItemSlotSH(slot);
        super.isValidSyncHandler(this.syncHandler);
        setSyncHandler(this.syncHandler);
        return this;
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return false;
    }
}
