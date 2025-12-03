package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhantomItemSlot extends ItemSlot implements RecipeViewerGhostIngredientSlot<ItemStack> {

    private PhantomItemSlotSH syncHandler;

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof PhantomItemSlotSH;
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(PhantomItemSlotSH.class);
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
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
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
        if (!this.syncHandler.isPhantom() || !areAncestorsEnabled()) return null;
        ItemStack itemStack = Internal.getIngredientRegistry().getIngredientHelper(ingredient).getCheatItemStack(ingredient);
        return this.syncHandler.isItemValid(itemStack) ? itemStack : null;
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
        return syncHandler(new PhantomItemSlotSH(slot));
    }

    @Override
    public PhantomItemSlot syncHandler(ItemSlotSH syncHandler) {
        setSyncHandler(syncHandler);
        return this;
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return false;
    }
}
